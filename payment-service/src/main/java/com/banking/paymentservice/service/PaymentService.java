package com.banking.paymentservice.service;

import com.banking.paymentservice.dto.CreatePaymentRequest;
import com.banking.paymentservice.dto.PaymentOrderResponse;
import com.banking.paymentservice.model.Payment;
import com.banking.paymentservice.model.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.secret-key}")
    private String secretKey;

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";

    public PaymentOrderResponse createPaymentOrder(CreatePaymentRequest request) throws StripeException {
        log.info("Creating Stripe payment intent for account: {} amount: {}",
                request.getAccountNumber(), request.getAmount());

        Stripe.apiKey = secretKey;

        long amountInCents = request.getAmount()
                .movePointRight(2)
                .longValueExact();

        Payment payment = new Payment();
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency("usd");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setDescription(request.getDescription());

        Payment saved = paymentRepository.save(payment);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .putMetadata("payment_id", saved.getId())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        log.info("Stripe payment intent created: {}", paymentIntent.getId());

        saved.setStripePaymentIntentId(paymentIntent.getId());
        paymentRepository.save(saved);

        return new PaymentOrderResponse(
                saved.getId(),
                paymentIntent.getId(),
                request.getAmount(),
                "usd",
                publishableKey,
                paymentIntent.getClientSecret(),
                "CREATED"
        );
    }

    public void handleWebhook(Map<String, Object> payload) {
        log.info("Received Stripe webhook: {}", payload.get("type"));

        String eventType = (String) payload.get("type");

        if ("payment_intent.succeeded".equals(eventType)) {
            handlePaymentSuccess(payload);
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            handlePaymentFailure(payload);
        }
    }

    private void handlePaymentSuccess(Map<String, Object> payload) {
        try {
            Map<String, Object> paymentData = extractPaymentData(payload);
            String paymentIntentId = (String) paymentData.get("id");
            String stripePaymentId = (String) paymentData.get("latest_charge");

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found for payment intent: " + paymentIntentId));

            payment.setStripePaymentId(stripePaymentId);
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            String paymentId = java.util.Objects.requireNonNull(payment.getId(), "payment id");

            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", paymentId);
            event.put("accountNumber", payment.getAccountNumber());
            event.put("amount", payment.getAmount());
            event.put("stripePaymentId", stripePaymentId);

            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, paymentId, event);
            log.info("Payment completed: {}", paymentId);

        } catch (Exception e) {
            log.error("Error handling payment success: {}", e.getMessage());
        }
    }

    private void handlePaymentFailure(Map<String, Object> payload) {
        try {
            Map<String, Object> paymentData = extractPaymentData(payload);
            String paymentIntentId = (String) paymentData.get("id");

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException(
                            "Payment not found for payment intent: " + paymentIntentId));

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment failed via Stripe");
            paymentRepository.save(payment);

            String paymentId = java.util.Objects.requireNonNull(payment.getId(), "payment id");

            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", paymentId);
            event.put("accountNumber", payment.getAccountNumber());
            event.put("amount", payment.getAmount());
            event.put("reason", "Payment failed via Stripe");
            kafkaTemplate.send("payment.failed", paymentId, event);

            log.warn("Payment failed: {}", paymentId);

        } catch (Exception e) {
            log.error("Error handling payment failure: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPaymentData(Map<String, Object> payload) {
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        Map<String, Object> object = (Map<String, Object>) data.get("object");
        return object;
    }
}