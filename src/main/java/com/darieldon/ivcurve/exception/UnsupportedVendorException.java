package com.darieldon.ivcurve.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.UNPROCESSABLE_ENTITY,
        reason = "Formato de arquivo não suportado"
)
@Getter
public class UnsupportedVendorException extends RuntimeException{

    private final String fileName;

    public UnsupportedVendorException(String fileName) {
        super("Formato não reconhecido: " + fileName + ". Suportados: Solmetric_Fluke CSV, ENTEC CSV");
        this.fileName = fileName;
    }

    public UnsupportedVendorException(String fileName, Throwable cause) {
        super("Formato não reconhecido: " + fileName, cause);
        this.fileName = fileName;
    }
}
