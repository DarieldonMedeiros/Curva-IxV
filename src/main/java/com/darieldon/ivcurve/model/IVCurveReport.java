package com.darieldon.ivcurve.model;

import java.util.List;

public record IVCurveReport(
        MeasurementMetadata metadata,
        MeasurementMetrics metrics,
        TheoreticalMetrics theoretical,
        ValidationResult validation,
        List<IVPoint> ivPoints
) {
    public Vendor vendor (){
        return metadata.vendor();
    }

    public String status() {
        return validation().statusFinal();
    }

    public String moduleName() {
        return metadata.parque();
    }
}
