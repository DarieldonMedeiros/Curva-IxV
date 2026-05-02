package com.darieldon.ivcurve.model;

/**
 * Valores elétricos da medição
 * Para ENTEC → usa valores corrigidos para STC (Voc_STC, Isc_STC, etc).
 * Para solmetric → utiliza valores medidos nas condições reais
 * EM ambos os casos, a irradiância e temperatura são as condições REAIS (usadas para validar irradiância mínima e calcular teórico Solmetric).
 * */
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
     * Calcula Pmax, FF e performance automaticamente
     *
     * @param pmaxNominalString Potência nominal da STRING inteira em STC (W) = pmaxNominal_por_modulo * nModules *nStrings
     * */
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
