
package com.securepay.payment.service;

import com.securepay.payment.model.PaymentRequest;
import com.securepay.payment.model.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.securepay.payment.exception.IdempotencyConflictException;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void shouldCreatePayment() {
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("250.00"),
                "INR",
                "Online purchase"
        );

        PaymentResponse response =
                paymentService.createPayment("test-key-001", request);

        assertThat(response.paymentId()).startsWith("pay_");
        assertThat(response.amount()).isEqualByComparingTo("250.00");
        assertThat(response.currency()).isEqualTo("INR");
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void shouldReturnOriginalPaymentForSameIdempotencyKey() {
        PaymentRequest request = new PaymentRequest(
                new BigDecimal("250.00"),
                "INR",
                "Online purchase"
        );

        PaymentResponse firstResponse =
                paymentService.createPayment("test-key-002", request);

        PaymentResponse secondResponse =
                paymentService.createPayment("test-key-002", request);

        assertThat(secondResponse.paymentId())
                .isEqualTo(firstResponse.paymentId());
        assertThat(secondResponse.createdAt())
                .isEqualTo(firstResponse.createdAt());
    }

    @Test
    void shouldRejectSameKeyWithDifferentPaymentDetails() {
        PaymentRequest firstRequest = new PaymentRequest(
                new BigDecimal("250.00"),
                "INR",
                "Online purchase"
        );

        PaymentRequest differentRequest = new PaymentRequest(
                new BigDecimal("500.00"),
                "INR",
                "Online purchase"
        );

        paymentService.createPayment("test-key-003", firstRequest);

        assertThatThrownBy(() ->
                paymentService.createPayment(
                        "test-key-003", differentRequest))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessageContaining("different payment details");
    }
}
