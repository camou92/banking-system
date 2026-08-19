package com.banking.paymentservice;

import com.banking.paymentservice.model.Payment;
import com.banking.paymentservice.model.PaymentStatus;
import com.banking.paymentservice.repository.PaymentRepository;
import com.banking.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void handleWebhookShouldCompletePaymentForSucceededIntent() {
        Payment payment = new Payment();
        payment.setId("pay-123");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setAccountNumber("ACC123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("usd");

        when(paymentRepository.findByStripePaymentIntentId("pi_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        object.put("id", "pi_123");
        object.put("latest_charge", "ch_123");

        data.put("object", object);
        payload.put("data", data);
        payload.put("type", "payment_intent.succeeded");

        paymentService.handleWebhook(payload);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        verify(paymentRepository).save(payment);
        verify(kafkaTemplate).send(eq("payment.completed"), eq("pay-123"), any(Map.class));
    }
}
