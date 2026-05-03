package com.darieldon.ivcurve.service;

import com.darieldon.ivcurve.model.MeasurementMetrics;
import com.darieldon.ivcurve.model.TheoreticalMetrics;
import com.darieldon.ivcurve.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Aplica os 5 critérios de aceitação de uma Medição IxV.
 * Stateless - não acessa banco, não lê arquivo.
 * Recebe apenas MeasurementMetrics e TheoreticalMetrics.
 * Critérios (comuns a ENTEC e Solmetric):
 *   1. Irradiância > 700 W/m²
 *   2. Fill Factor > 0,70
 *   3. Razão Voc entre −5% e +5% → (Voc_med / Voc_teo - 1) × 100
 *   4. Razão Isc entre 0,90 e 1,10 → Isc_med / Isc_teo
 *   5. Desempenho entre 90% e 110% → (Pmax_med / Pmax_nom) × 100* */
@Service
public class ValidationService {

    // Limites configuráveis como constantes - fácil de ajustar.
    private static final double MIN_IRRADIANCE  = 700.0;
    private static final double MIN_FF          = 0.70;
    private static final double MIN_VOC_RATIO   = -5.0;
    private static final double MAX_VOC_RATIO   = 5.0;
    private static final double MIN_ISC_RATIO   = 0.90;
    private static final double MAX_ISC_RATIO   = 1.10;
    private static final double MIN_PERFORMANCE = 90.0;
    private static final double MAX_PERFORMANCE = 110.0;

    public ValidationResult validate(MeasurementMetrics measurement, TheoreticalMetrics theoretical) {

        List<String> violations = new ArrayList<>();
        // ── Critério 1: Irradiância ──────────────────────────────
        // Medições com pouca luz solar não são confiáveis.
        // Abaixo de 700 W/m² os erros de medição são proporcionalmente maiores
        boolean irradianceOK = measurement.irradiance() > MIN_IRRADIANCE;
        if (!irradianceOK) {
            violations.add(String.format("Irradiância %.1f W/m² abaixo do mínimo (%.0f W/m²)",
                    measurement.irradiance(), MIN_IRRADIANCE));
        }

        // ── Critério 2: Fill Factor ──────────────────────────────
        // FF = (Vmpp × Impp) / (Voc × Isc)
        // Representa a "qualidade" da curva. Módulos saudáveis ficam
        // entre 0,70 e 0,85. Abaixo de 0,70 indica degradação.
        boolean ffOK = measurement.ff() > MIN_FF;
        if (!ffOK) {
            violations.add(String.format(
                    "Fill Factor %.4f abaixo do mínimo (%.2f)",
                    measurement.ff(), MIN_FF));
        }

        // ── Critério 3: Razão Voc ────────────────────────────────
        // Compara Voc medido com Voc teórico calculado pelo ModuleConfig
        // usando a fórmula: Voc_teo = N_mod × Voc_nom × (1 + β × (T−25))
        // Desvio acima de ±5% indica strings abertas ou degradação severa.

        boolean vocOK = theoretical.vocRatioPercent() > MIN_VOC_RATIO && theoretical.vocRatioPercent() < MAX_VOC_RATIO;
        if (!vocOK) {
            violations.add(String.format(
                    "Voc fora de ±5%%: desvio = %.2f%% (limite: %.0f%% a +%.0f%%)",
                    theoretical.vocRatioPercent(), MIN_VOC_RATIO, MAX_VOC_RATIO));
        }

        // ── Critério 4: Razão Isc ────────────────────────────────
        // Compara Isc medido com Isc teórico: Isc_teo = N_str × Isc_nom × (G/1000)
        // Fora de 0,90–1,10 indica sombreamento, sujeira ou falha elétrica.
        boolean iscOK = theoretical.iscRatio() > MIN_ISC_RATIO && theoretical.iscRatio() < MAX_ISC_RATIO;
        if (!iscOK) {
            violations.add(String.format(
                    "Isc fora de 90–110%%: razão = %.4f (limite: %.2f a %.2f)",
                    theoretical.iscRatio(), MIN_ISC_RATIO, MAX_ISC_RATIO));
        }

        // ── Critério 5: Desempenho ───────────────────────────────
        // desempenho = (Pmax_medida / Pmax_nominal) × 100
        // Calculado em MeasurementMetrics.of() usando pmaxNominal do ModuleConfig.
        boolean performanceOK = measurement.performance() > MIN_PERFORMANCE && measurement.performance() < MAX_PERFORMANCE;
        if (!performanceOK) {
            violations.add(String.format(
                    "Desempenho %.2f%% fora da faixa (%.0f%%–%.0f%%)",
                    measurement.performance(), MIN_PERFORMANCE, MAX_PERFORMANCE));
        }

        // ── Veredicto final ──────────────────────────────────────
        // APROVADO somente se TODOS os 5 critérios passaram
        String statusFinal = violations.isEmpty() ? "APROVADO" : "REPROVADO";

        return new ValidationResult(
                irradianceOK, ffOK, vocOK, iscOK, performanceOK, statusFinal, violations
        );
    }
}
