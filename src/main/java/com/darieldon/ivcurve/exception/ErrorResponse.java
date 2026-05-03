package com.darieldon.ivcurve.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String code,
        String message,
        String fileName,
        String path,
        Instant timestamp
) {
}
