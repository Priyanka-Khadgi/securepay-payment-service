
package com.securepay.payment.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String paymentId,
        BigDecimal amount,
        String currency,
        String description,
        String status,
        Instant createdAt
) {
}
