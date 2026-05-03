package com.darieldon.ivcurve.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <h1>Identificação da Medição</h1>
 *
 * <p>Este componente identifica onde e quando a medição foi realizada,
 * conforme a origem dos dados:</p>
 *
 * <ul>
 *   <li><b>ENTEC:</b> As informações de parque, subcampo, inversor e pvString
 *       são extraídas diretamente do nome do arquivo.</li>
 *
 *   <li><b>Solmetric:</b>
 *     <ul>
 *       <li>Parque e subcampo → obtidos do campo <i>Project File</i></li>
 *       <li>Inversor e pvString → obtidos do campo <i>Array Location</i></li>
 *     </ul>
 *   </li>
 * </ul>
 */

public record MeasurementMetadata(
        LocalDate measurementDate,
        LocalTime measurementTime,
        String parque,
        String subcampo,
        String inversor,
        String pvString,
        String fileName,
        Vendor vendor
) {
    public String formatedDateTime(){
        return measurementDate + " " + measurementTime;
    }
}
