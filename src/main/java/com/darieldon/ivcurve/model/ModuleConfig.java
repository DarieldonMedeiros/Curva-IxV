package com.darieldon.ivcurve.model;
/**
 * <h1>Parâmetros Nominais de Módulo Fotovoltaico</h1>
 *
 * <p>Define os parâmetros nominais de <b>um módulo fotovoltaico</b> em
 * condições padrão de teste (STC: 1000 W/m², 25 °C).</p>
 *
 * <h2>Módulos Suportados</h2>
 * <ul>
 *   <li><b>CS7N-695TB:</b> Voc = 47.7 V, Isc = 18.44 A,
 *       β = −0.26 %/°C, Pmax = 695 W</li>
 *   <li><b>CS7N-700TB:</b> Voc = 47.9 V, Isc = 18.49 A,
 *       β = −0.26 %/°C, Pmax = 700 W</li>
 *   <li><b>CS7N-705TB:</b> Voc = 48.1 V, Isc = 18.54 A,
 *       β = −0.26 %/°C, Pmax = 705 W</li>
 * </ul>
 *
 * <p>Para todos os modelos acima:</p>
 * <ul>
 *   <li>nModules = 30</li>
 *   <li>nStrings = 1</li>
 * </ul>
 *
 * <h2>Parâmetros</h2>
 * @param vocNominal   Tensão de circuito aberto (Voc) de 1 módulo em STC (V). Ex: 47,7
 * @param iscNominal   Corrente de curto-circuito (Isc) de 1 string (= 1 módulo) em STC (A). Ex: 18.44
 * @param betaPercent  Coeficiente de temperatura da Voc (%/°C). Ex: -0.26
 * @param nModules     Número de módulos em série. Ex: 30
 * @param nStrings     Número de strings em paralelo. Ex: 1
 * @param pmaxNominal  Potência nominal de 1 módulo em STC (W). Ex: 695
 */

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
     * <h1>Voc Teórico da String</h1>
     *
     * <p>Calcula a tensão de circuito aberto (Voc) teórica da
     * <b>STRING completa</b> em uma temperatura T.</p>
     *
     * <h2>Fórmula</h2>
     * <pre>
     * Voc_teórico = nMod × Voc_nom × (1 + beta_decimal × (T − 25))
     * </pre>
     *
     * <h2>Exemplo</h2>
     * <p>Para um módulo de 695 Wp, 30 em série, T = 39,39 °C:</p>
     * <pre>
     * 1431.0 × (1 + (−0.0026) × 14.39)
     * = 1431.0 × 0.9626
     * = 1377.6 V
     * </pre>
     */
    public double calcVocTheoreticalString(double temperatureCelsius) {
        return nModules * vocNominal * (1.0 + betaDecimal() * (temperatureCelsius - 25.0));
    }

    /**
     * <h1>Voc Teórico em STC</h1>
     *
     * <p>Calcula a tensão de circuito aberto (Voc) teórica da
     * <b>STRING completa</b> em condições padrão de teste (STC: 25 °C).</p>
     *
     * <p>Este valor é utilizado para comparação quando o arquivo CSV
     * já fornece valores corrigidos para STC.</p>
     */
    public double calcVocStringAtStc() {
        return nModules * vocNominal;
    }

    /**
     * <h1>Isc Teórico da String</h1>
     *
     * <p>Calcula a corrente de curto-circuito (Isc) teórica da
     * <b>STRING completa</b> em função da irradiância G.</p>
     *
     * <h2>Fórmula</h2>
     * <pre>
     * Isc_teórico = nStr × Isc_nom × (G / 1000)
     * </pre>
     *
     * <h2>Exemplo</h2>
     * <p>Para um módulo de 695 Wp, 1 string, G = 1447 W/m²:</p>
     * <pre>
     * 1 × 18.44 × (1447 / 1000)
     * = 1 × 18.44 × 1.447
     * = 26.68 A
     * </pre>
     */

    public double calcIscTheoreticalString(double irradianceWm2) {
        return nStrings * iscNominal * (irradianceWm2/1000.0);
    }

    /**
     * <h1>Isc teórico da STRING em STC (1000 W/m²).</h1>
     */
    public double calcIscStringAtStc() {
        return nStrings * iscNominal;
    }

    /**
     * <h1>Potência nominal total da STRING em STC (W).</h1>
     * Ex: 695W × 30 × 1 = 20850 W
     */
    public double pmaxNominalString() {
        return pmaxNominal * nModules * nStrings;
    }
}
