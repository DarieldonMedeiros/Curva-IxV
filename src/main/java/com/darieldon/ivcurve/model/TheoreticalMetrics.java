package com.darieldon.ivcurve.model;

/**
 * Valores teóricos esperados e os desvios em relação ao medido
 * ENTEC → vocTheoretical e iscTheoretical calculados em STC (1000 W/m², 25 °C) pois os valores medidos já estão corrigidos para STC.
 * Solmetric → calculados nas condições reais de irradiância e temperatura.
 * */
public record TheoreticalMetrics(
        double vocTheoretical, // Voc teórico da String (V)
        double iscTheoretical, // Isc teórico da String (A)
        double vocRatioPercent, // (Voc_med/Voc_teo - 1) * 100 (%) Meta: ±5%
        double iscRatio // Isc_med / Isc_teo. Meta: 0,90 – 1,10
) {
    public static TheoreticalMetrics of (
            double vocMeasured, double iscMeasured,
            double vocTheoretical, double iscTheoretical) {

        double vocRatio = vocTheoretical > 0 ? ((vocMeasured / vocTheoretical) - 1.0) * 100.0 : 0.0;
        double iscRatio = iscTheoretical > 0 ? iscMeasured / iscTheoretical : 0.0;

        return new TheoreticalMetrics(vocTheoretical, iscTheoretical, vocRatio, iscRatio);
    }
}
