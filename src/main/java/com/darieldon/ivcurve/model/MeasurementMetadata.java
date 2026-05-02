package com.darieldon.ivcurve.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Identifica onde e quando a medição foi feita
 * ENTEC → parque/subcampo/inversor/pvString extraídas do nome do arquivo
 * Solmetric → parque/subcampo do campo "Project File", inversor/pvString do "Array Location"
 * */
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
