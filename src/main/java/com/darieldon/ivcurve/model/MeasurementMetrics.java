package com.darieldon.ivcurve.model;

/**
 * <h1>Valores Elétricos da Medição</h1>
 *
 * <p>Define como os valores elétricos são interpretados conforme a origem dos dados:</p>
 *
 * <ul>
 *   <li><b>ENTEC:</b> Utiliza valores corrigidos para STC
 *       (ex.: Voc_STC, Isc_STC, Vm_STC, Im_STC).</li>
 *
 *   <li><b>Solmetric:</b> Utiliza valores medidos diretamente
 *       nas condições reais de operação.</li>
 * </ul>
 *
 * <p><b>Observação:</b> Em ambos os casos, a irradiância e a temperatura
 * correspondem sempre às condições reais, usadas para:</p>
 * <ul>
 *   <li>Validar a irradiância mínima</li>
 *   <li>Calcular os valores teóricos no caso do Solmetric</li>
 * </ul>
 */

public record MeasurementMetrics(
        double voc,         // Voc da string (V)
        double isc,         // Isc da string (A)
        double vmpp,        // Vmpp da string (V)
        double impp,        // Impp da string (A)
        double pmax,        // Pmax medida = vmpp * impp (W)
        double irradiance,  //Irradiância real medida (W/m²)
        double temperature, // Temperatura real medida (°C)
        double ff,          // Fator de forma = pmax / (voc * isc)
        double performance  // Desempenho = pmax / pmaxNominalString * 100 (%)
) {

    /**
     * <h1>Cálculo Automático de Pmax, FF e Performance</h1>
     *
     * <p>Esta função realiza o cálculo automático da potência máxima (Pmax),
     * do fator de forma (FF) e do desempenho da string.</p>
     *
     * @param pmaxNominalString Potência nominal da STRING inteira em STC (W).
     *                          Calculada como:
     *                          <code>pmaxNominal_por_modulo × nModules × nStrings</code>
     */
    public static MeasurementMetrics of(
            double voc, double isc, double vmpp, double impp,
            double irradiance, double temperature,
            double pmaxNominalString) {

        double pmax = vmpp * impp;
        double ff = (voc > 0 & isc > 0) ? pmax / (voc * isc) : 0.00;
        double performance = pmaxNominalString > 0 ? (pmax / pmaxNominalString) * 100.0 : 0.0;

        return new MeasurementMetrics(
                voc, isc, vmpp, impp, pmax, irradiance, temperature, ff, performance);

    }
}
