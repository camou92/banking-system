package com.banking.notificationservice.service;

import com.banking.notificationservice.client.AccountServiceClient;
import com.banking.notificationservice.dto.AccountResponse;
import com.banking.notificationservice.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final AccountServiceClient accountServiceClient;

    private final EmailService emailService;

    private final EmailTemplateService emailTemplateService;

    /**
     * Transaction completed — debit sender, credit receiver.
     */
    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String, Object> payload) {
        try {
            String senderAccount = (String) payload
                    .get("senderAccountNumber");
            String receiverAccount = (String) payload
                    .get("receiverAccountNumber");
            String amount = payload.get("amount").toString();

            AccountResponse sender = accountServiceClient.getAccountNumber(senderAccount);

            AccountResponse receiver = accountServiceClient.getAccountNumber(receiverAccount);

            emailService.sendEmail(
                    emailTemplateService.buildDebitEmail(
                            sender,
                            receiverAccount,
                            amount));

            emailService.sendEmail(
                    emailTemplateService.buildCreditEmail(
                            receiver,
                            senderAccount,
                            amount));

        } catch (Exception e) {
            log.error("Error sending transaction notification: {}",
                    e.getMessage());
        }
    }


    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(
            @Payload Map<String, Object> payload) {
        try {
            String accountNumber = (String) payload.get("accountNumber");
            String reason = (String) payload.get("reason");

            AccountResponse account = accountServiceClient.getAccountNumber(accountNumber);

            emailService.sendEmail(
                    emailTemplateService.buildFraudEmail(
                            account,
                            reason));

        } catch (Exception e) {
            log.error("Error sending fraud alert: {}", e.getMessage());
        }
    }


    @KafkaListener(topics = "transaction.otp.generated")
    public void consumeOtpGenerated(
            @Payload Map<String, Object> payload) {
        try {
            String accountNumber = (String) payload.get("accountNumber");
            String otp = (String) payload.get("otp");
            String transactionId = (String) payload.get("transactionId");
            String amount = payload.get("amount").toString();
            String reason = (String) payload.get("reason");

            AccountResponse account =
                    accountServiceClient.getAccountNumber(accountNumber);

            emailService.sendEmail(
                    emailTemplateService.buildOtpEmail(
                            account,
                            amount,
                            reason,
                            otp));

        } catch (Exception e) {
            log.error("Error sending OTP notification: {}",
                    e.getMessage());
        }
    }


    @KafkaListener(topics = "transaction.refunded")
    public void consumeTransactionRefunded(
            @Payload Map<String, Object> payload) {
        try {
            String senderAccount = (String) payload
                    .get("senderAccountNumber");
            String amount = payload.get("amount").toString();
            String reason = (String) payload.get("reason");

            AccountResponse account =
                    accountServiceClient.getAccountNumber(senderAccount);

            emailService.sendEmail(
                    emailTemplateService.buildRefundEmail(
                            account,
                            amount,
                            reason));

        } catch (Exception e) {
            log.error("Error sending refund notification: {}",
                    e.getMessage());
        }
    }

    /**
     * Payment completed via Razorpay.
     */
    @KafkaListener(topics = "payment.completed")
    public void consumePaymentCompleted(
            @Payload Map<String, Object> payload) {
        try {
            String accountNumber = (String) payload.get("accountNumber");
            String amount = payload.get("amount").toString();

            AccountResponse account =
                    accountServiceClient.getAccountNumber(accountNumber);

//            emailService.sendEmail(
//                    emailTemplateService.buildPaymentSuccessEmail(
//                            account,
//                            amount,
//                            paymentId));

        } catch (Exception e) {
            log.error("Error sending payment notification: {}",
                    e.getMessage());
        }
    }

    /**
     * Payment failed via Razorpay.
     */
    @KafkaListener(topics = "payment.failed")
    public void consumePaymentFailed(
            @Payload Map<String, Object> payload) {
        try {
            String accountNumber = (String) payload.get("accountNumber");
            String amount = payload.get("amount").toString();

            AccountResponse account =
                    accountServiceClient.getAccountNumber(accountNumber);

            emailService.sendEmail(
                    emailTemplateService.buildPaymentFailedEmail(
                            account,
                            amount));

        } catch (Exception e) {
            log.error("Error sending payment failure notification: {}",
                    e.getMessage());
        }
    }

//    private void sendAlert(String accountNumber,
//                           String subject,
//                           String message) {
//        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
//        log.info("NOTIFICATION SENT");
//        log.info("Account : {}", accountNumber);
//        log.info("Subject : {}", subject);
//        log.info("Message : {}", message);
//        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
//    }
}