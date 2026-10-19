
package com.securepay.payment.service;

import com.securepay.payment.model.PaymentRequest;
import com.securepay.payment.model.PaymentResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.securepay.payment.exception.IdempotencyConflictException;

@Service
public class PaymentService {

    private final ConcurrentHashMap<String, PaymentResponse> payments =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, PaymentRequest> requests =
            new ConcurrentHashMap<>();

    public PaymentResponse createPayment(
            String idempotencyKey,
            PaymentRequest request) {

        PaymentRequest existingRequest =
                requests.putIfAbsent(idempotencyKey, request);

        if (existingRequest != null) {
            if (!existingRequest.equals(request)) {
                throw new IdempotencyConflictException(
                        "Idempotency key was already used with different payment details");
            }

            return payments.get(idempotencyKey);
        }

        PaymentResponse response = new PaymentResponse(
                "pay_" + UUID.randomUUID(),
                request.amount(),
                request.currency(),
                request.description(),
                "PENDING",
                Instant.now()
        );

        payments.put(idempotencyKey, response);

        return response;
    }
}
