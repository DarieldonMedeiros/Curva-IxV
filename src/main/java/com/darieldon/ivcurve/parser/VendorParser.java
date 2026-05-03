package com.darieldon.ivcurve.parser;

import com.darieldon.ivcurve.model.IVCurveReport;
import com.darieldon.ivcurve.model.ModuleConfig;

/**
 * Contrato que o parser de vendor deve implementar.
 * Fluxo de uso:
 *  1. supports(content) → detecta se este parser entende o arquivo
 *  detectModuleWattage(content) → Lê a potência do módulo direto do CSV
 *  parse(content, fileName, module) → produz o relatório completo
 * ENTEC: power = round(Pm_nom / Mod_ser) → ex: 20850/30 = 695
 * Solmetric/Fluke: power extraído de "Module Model" → ex: "CS7N-705TB" → 705
 * */
public interface VendorParser {

    /** Retorna true se o conteúdo do arquivo pertence a este Vendor. */
    boolean supports(String fileContent);

    /**
     * Lê a potência do módulo direto do CSV sem fazer o parse completo.
     * Retorna 695, 700 ou 705.
     * Lança RuntimeException se não conseguir detectar.
     * */
    int detectModulePower(String fileContent);

    /**
     * Faz o parse completo do CSV usando os parâmetros do módulo já
     * identificado e carregado do banco de dados pelo ParserDetectorService.
     * O campo validation do IVCurveReport retornado é null -
     * será preenchido pelo ValidationService após o parse
     * */
    IVCurveReport parse(String fileContent, String fileName, ModuleConfig module);
}
