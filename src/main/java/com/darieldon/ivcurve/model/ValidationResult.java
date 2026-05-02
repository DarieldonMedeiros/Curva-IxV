package com.darieldon.ivcurve.model;

import java.util.List;

public record ValidationResult(
        boolean irradianceOK,
        boolean ffOK,
        boolean vocOK,
        boolean iscOK,
        boolean performanceOK,
        String statusFinal,
        List<String> violations
) {
    public boolean isApproved() {
        return "APROVADO".equals(statusFinal);
    }

    public long passedCount() {
        long passed = 0;

        if (irradianceOK) passed++;
        if (ffOK) passed++;
        if (vocOK) passed++;
        if (iscOK) passed++;
        if (performanceOK) passed++;

        return passed;
    }
}
