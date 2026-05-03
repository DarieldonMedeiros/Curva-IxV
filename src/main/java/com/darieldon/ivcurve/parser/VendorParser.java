package com.darieldon.ivcurve.parser;

import com.darieldon.ivcurve.model.IVCurveReport;
import com.darieldon.ivcurve.model.ModuleConfig;

/**
 * <h1>Contrato do Parser de Vendor</h1>
 *
 * <p>Define o contrato que o parser de vendor deve implementar,
 * especificando os métodos obrigatórios e o fluxo de uso.</p>
 *
 * <h2>Fluxo de Uso</h2>
 * <ol>
 *   <li><b>supports(content):</b> Detecta se este parser entende o arquivo.</li>
 *   <li><b>detectModuleWattage(content):</b> Lê a potência do módulo diretamente do CSV.</li>
 *   <li><b>parse(content, fileName, module):</b> Produz o relatório completo.</li>
 * </ol>
 *
 * <h2>Regras de Detecção de Potência</h2>
 * <ul>
 *   <li><b>ENTEC:</b> Potência calculada como
 *       <code>round(Pm_nom / Mod_ser)</code>.
 *       <br>Exemplo: 20850 / 30 = 695 Wp</li>
 *
 *   <li><b>Solmetric / Fluke:</b> Potência extraída do campo
 *       <i>Module Model</i>.
 *       <br>Exemplo: "CS7N-705TB" → 705 Wp</li>
 * </ul>
 */

public interface VendorParser {

    /**
     * <h1>Validação de Vendor</h1>
     *
     * <p>Verifica se o conteúdo do arquivo pertence ao vendor atual.</p>
     *
     * <h2>Comportamento</h2>
     * <ul>
     *   <li>Retorna <code>true</code> se o conteúdo for reconhecido como pertencente a este vendor.</li>
     *   <li>Retorna <code>false</code> caso contrário.</li>
     * </ul>
     */

    boolean supports(String fileContent);

    /**
     * <h1>Leitura da Potência do Módulo</h1>
     *
     * <p>Lê a potência nominal do módulo diretamente do arquivo CSV,
     * sem realizar o parse completo.</p>
     *
     * <h2>Comportamento</h2>
     * <ul>
     *   <li>Retorna um dos valores suportados: <b>695</b>, <b>700</b> ou <b>705</b>.</li>
     *   <li>Lança <code>RuntimeException</code> caso não seja possível detectar
     *       a potência do módulo.</li>
     * </ul>
     */

    int detectModulePower(String fileContent);

    /**
     * <h1>Parse Completo do CSV</h1>
     *
     * <p>Realiza o parse completo do arquivo CSV utilizando os parâmetros
     * do módulo já identificados e carregados do banco de dados pelo
     * <code>ParserDetectorService</code>.</p>
     *
     * <h2>Comportamento</h2>
     * <ul>
     *   <li>Produz um objeto <code>IVCurveReport</code> com todas as informações
     *       extraídas do arquivo.</li>
     *   <li>O campo <code>validation</code> do relatório retornado será
     *       <code>null</code> após o parse inicial.</li>
     *   <li>Esse campo <code>validation</code> será posteriormente preenchido
     *       pelo <code>ValidationService</code>.</li>
     * </ul>
     */

    IVCurveReport parse(String fileContent, String fileName, ModuleConfig module);
}
