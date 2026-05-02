package com.darieldon.ivcurve.model;
/**
 * Parâmetros nominais de UM módulo fotovoltaico em STC (1000 W/m², 25 °C)
 * Módulos suportados:
 *  CS7N-695TB: vocNominal = 47.7V, iscNominal = 18.44A, betaPercent = −0.26%, pmaxNominal = 695W
 *  CS7N-700TB: vocNominal = 47.9V, iscNominal = 18.49A, betaPercent = −0.26%, pmaxNominal = 700W
 *  CS7N-705TB: vocNominal = 48.1V, iscNominal = 18.54A, betaPercent = −0.26%, pmaxNominal = 705W
 *  Para todos os modelos acima: nModules = 30, nStrings = 1
 *
 * @param vocNominal   Voc de 1 módulo em STC (V). Ex: 47.7
 * @param iscNominal   Isc de 1 string (= 1 módulo) em STC (A). Ex: 18.44
 * @param betaPercent  Coeficiente temperatura de Voc (%/°C). Ex: -0.26
 * @param nModules     Módulos em série. Ex: 30
 * @param nStrings     Strings em paralelo. Ex: 1
 * @param pmaxNominal  Potência nominal de 1 módulo em STC (W). Ex: 695
 * */
public record ModuleConfig(
        String name,
        double vocNominal,
        double iscNominal,
        double betaPercent,
        int nModules,
        int nStrings,
        double pmaxNominal
) {
    /**Beta em decimal (Ex.: -0.26% -> -0.0026) */
    public double betaDecimal() {
        return betaPercent/100.0;
    }

    /**
     * Voc teórico da STRING completa na temperatura T.
     * Fórmula: nMod × Voc_nom × (1 + beta_decimal × (T − 25))
     * Exemplo (695Wp, 30s, T=39.39°C):
     *   1431.0 × (1 + (−0.0026) × 14.39) = 1431.0 × 0.9626 = 1377.6 V
     */
    public double calcVocTheoreticalString(double temperatureCelsius) {
        return nModules * vocNominal * (1.0 + betaDecimal() * (temperatureCelsius - 25.0));
    }

    /**
     * Voc teórico da STRING em STC (25 °C).
     * Usado para comparação quando o CSV já fornece valores corrigidos para STC.
     */
    public double calcVocStringAtStc() {
        return nModules * vocNominal;
    }

    /**
     * Isc teórico da STRING nas condições de irradiância G.
     * Fórmula: nStr × Isc_nom × (G / 1000)
     * Exemplo (695Wp, 1 string, G=1447 W/m²):
     *   1 × 18,44 × (1447 / 1000) = 1 × 18,44 × 1.447 = 26,68 A
     */
    public double calcIscTheoreticalString(double irradianceWm2) {
        return nStrings * iscNominal * (irradianceWm2/1000.0);
    }

    /**
     * Isc teórico da STRING em STC (1000 W/m²).
     */
    public double calcIscStringAtStc() {
        return nStrings * iscNominal;
    }

    /**
     * Potência nominal total da STRING em STC (W).
     * Ex: 695W × 30 × 1 = 20850 W
     */
    public double pmaxNominalString() {
        return pmaxNominal * nModules * nStrings;
    }
}
