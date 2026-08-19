package com.banking.notificationservice.service;

import com.banking.notificationservice.dto.AccountResponse;
import com.banking.notificationservice.dto.EmailRequest;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private static final String TITLE = "Banking System";

    public EmailRequest buildDebitEmail(
            AccountResponse sender,
            String receiverAccount,
            String amount) {

        return EmailRequest.builder()
                .to(sender.getEmail())
                .title(TITLE)
                .subject("Transaction Confirmation - Funds Debited")
                .message("""
                        Dear %s,

                        We would like to inform you that your transfer has been completed successfully.

                        Transaction Details
                        ------------------------------
                        Amount           : %s MAD
                        Sender Account   : %s
                        Receiver Account : %s

                        If you did not authorize this transaction, please contact our support immediately.

                        Thank you for choosing Banking System.

                        Regards,
                        Banking System
                        """.formatted(
                        sender.getAccountHolderName(),
                        amount,
                        sender.getAccountNumber(),
                        receiverAccount))
                .build();
    }

    public EmailRequest buildCreditEmail(
            AccountResponse receiver,
            String senderAccount,
            String amount) {

        return EmailRequest.builder()
                .to(receiver.getEmail())
                .title(TITLE)
                .subject("Incoming Transfer Received")
                .message("""
                        Dear %s,

                        A transfer has been credited to your account.

                        Transaction Details
                        ------------------------------
                        Amount         : %s MAD
                        Sender Account : %s

                        The funds are now available.

                        Thank you for choosing Banking System.

                        Regards,
                        Banking System
                        """.formatted(
                        receiver.getAccountHolderName(),
                        amount,
                        senderAccount))
                .build();
    }

    public EmailRequest buildOtpEmail(
            AccountResponse account,
            String amount,
            String reason,
            String otp) {

        return EmailRequest.builder()
                .to(account.getEmail())
                .title(TITLE)
                .subject("Transaction Verification Required")
                .message("""
                        Dear %s,

                        We detected unusual activity on your account.

                        Reason:
                        %s

                        Transaction Amount:
                        %s MAD

                        Your One-Time Password (OTP):

                        %s

                        This code is valid for 5 minutes.

                        If you did not initiate this transaction, please contact the bank immediately.

                        Regards,
                        Banking System Security Team
                        """.formatted(
                        account.getAccountHolderName(),
                        reason,
                        amount,
                        otp))
                .build();
    }

    public EmailRequest buildFraudEmail(
            AccountResponse account,
            String reason) {

        return EmailRequest.builder()
                .to(account.getEmail())
                .title(TITLE)
                .subject("Important Security Notice - Account Restricted")
                .message("""
                        Dear %s,

                        Your account has been temporarily restricted for security reasons.

                        Account Number:
                        %s

                        Reason:
                        %s

                        Please contact your bank immediately.

                        Regards,
                        Banking System Security Team
                        """.formatted(
                        account.getAccountHolderName(),
                        account.getAccountNumber(),
                        reason))
                .build();
    }

    public EmailRequest buildRefundEmail(
            AccountResponse account,
            String amount,
            String reason) {

        return EmailRequest.builder()
                .to(account.getEmail())
                .title(TITLE)
                .subject("Transaction Refund Confirmation")
                .message("""
                        Dear %s,

                        Your transaction has been cancelled.

                        Refunded Amount:
                        %s MAD

                        Reason:
                        %s

                        The amount has been credited back to your account.

                        Thank you for choosing Banking System.

                        Regards,
                        Banking System
                        """.formatted(
                        account.getAccountHolderName(),
                        amount,
                        reason))
                .build();
    }

    public EmailRequest buildPaymentSuccessEmail(
            AccountResponse account,
            String amount,
            String paymentId) {

        return EmailRequest.builder()
                .to(account.getEmail())
                .title(TITLE)
                .subject("Payment Confirmation")
                .message("""
                        Dear %s,

                        Your payment has been processed successfully.

                        Amount:
                        %s MAD

                        Payment Reference:
                        %s

                        Thank you for using Banking System.

                        Regards,
                        Banking System
                        """.formatted(
                        account.getAccountHolderName(),
                        amount,
                        paymentId))
                .build();
    }

    public EmailRequest buildPaymentFailedEmail(
            AccountResponse account,
            String amount) {

        return EmailRequest.builder()
                .to(account.getEmail())
                .title(TITLE)
                .subject("Payment Failed")
                .message("""
                        Dear %s,

                        Unfortunately, your payment could not be completed.

                        Amount:
                        %s MAD

                        Please try again later or contact our support.

                        Regards,
                        Banking System
                        """.formatted(
                        account.getAccountHolderName(),
                        amount))
                .build();
    }
}