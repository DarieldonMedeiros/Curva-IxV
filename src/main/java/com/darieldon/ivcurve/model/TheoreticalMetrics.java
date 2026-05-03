package com.darieldon.ivcurve.model;

/**
 * <h1>Valores Teóricos e Desvios</h1>
 *
 * <p>Define os valores elétricos teóricos esperados da STRING e
 * calcula os desvios em relação aos valores medidos.</p>
 *
 * <h2>Origem dos Dados</h2>
 * <ul>
 *   <li><b>ENTEC:</b> Voc e Isc teóricos são calculados em STC
 *       (1000 W/m², 25 °C), pois os valores medidos já estão
 *       corrigidos para STC.</li>
 *
 *   <li><b>Solmetric:</b> Voc e Isc teóricos são calculados
 *       diretamente nas condições reais de irradiância e temperatura.</li>
 * </ul>
 *
 * <p>Esses valores permitem avaliar a conformidade da medição
 * com o comportamento esperado do sistema fotovoltaico.</p>
 */

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
