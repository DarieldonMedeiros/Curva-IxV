package com.darieldon.ivcurve.service;

import com.darieldon.ivcurve.exception.UnsupportedVendorException;
import com.darieldon.ivcurve.model.IVCurveReport;
import com.darieldon.ivcurve.model.ModuleConfig;
import com.darieldon.ivcurve.model.ValidationResult;
import com.darieldon.ivcurve.parser.VendorParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.util.List;

/**
 * <h1>Detecção de Vendor e Orquestração do Parse</h1>
 *
 * <p>Responsável por detectar o vendor do arquivo CSV, resolver o módulo
 * via banco de dados e orquestrar o processo de parse e validação.</p>
 *
 * <h2>Fluxo de Execução</h2>
 * <ol>
 *   <li>Tenta ler o arquivo em UTF-8. Caso nenhum parser reconheça,
 *       tenta novamente em ISO-8859-1 (ENTEC frequentemente utiliza
 *       ISO-8859-1 devido a caracteres acentuados).</li>
 *
 *   <li>Para o parser que reconhecer o arquivo:
 *     <ol type="a">
 *       <li><code>parser.detectModulePower(content)</code> → retorna 695, 700 ou 705</li>
 *       <li><code>moduleService.findByPower(power)</code> → obtém <code>ModuleConfig</code> do banco</li>
 *       <li><code>parser.parse(content, fileName, module)</code> → gera <code>IVCurveReport</code> sem validação</li>
 *       <li><code>validationService.validate(metrics, theoretical)</code> → produz <code>ValidationResult</code></li>
 *       <li>Monta e retorna o <code>IVCurveReport</code> final completo</li>
 *     </ol>
 *   </li>
 *
 *   <li>Se nenhum parser reconhecer o arquivo:
 *       lança <code>UnsupportedVendorException</code> (HTTP 422).</li>
 * </ol>
 */

@Service
@RequiredArgsConstructor
public class ParserDetectorService {

    private final List<VendorParser> parsers;
    private final ValidationService validationService;
    private final ModuleService moduleService;

    /**
     * <h1>Detecção, Parse e Validação do CSV</h1>
     *
     * <p>Executa o processo completo de leitura e validação de um arquivo CSV,
     * sem necessidade de seleção manual de módulo — todas as informações
     * são extraídas diretamente do arquivo.</p>
     *
     * <h2>Comportamento</h2>
     * <ul>
     *   <li>Detecta automaticamente o vendor do arquivo.</li>
     *   <li>Realiza o parse completo do conteúdo CSV.</li>
     *   <li>Valida os resultados obtidos com base nos parâmetros teóricos
     *       e nas condições reais de operação.</li>
     * </ul>
     *
     * <p>O fluxo é totalmente automatizado, garantindo que o módulo seja
     * identificado e validado sem intervenção manual.</p>
     */

    public IVCurveReport detect(MultipartFile file) {

        String name = file.getOriginalFilename();
        for (String encoding : List.of("UTF-8", "ISO-8859-1")) {
            String content = readAs(file, encoding);

            for (VendorParser vendorParser : parsers) {
                if (vendorParser.supports(content)) {
                    // Detecta a potência do módulo direto do CSV
                    int power = vendorParser.detectModulePower(content);

                    // Busca o ModuleConfig correspondente no banco
                    ModuleConfig module = moduleService.findByPower(power);

                    // Parse completo (sem validação - campo validation = null)
                    IVCurveReport raw = vendorParser.parse(content, name, module);

                    // Validação separada, após o parse
                    ValidationResult validationResult = validationService.validate(raw.metrics(), raw.theoretical());

                    return new IVCurveReport(
                            raw.metadata(), raw.metrics(),
                            raw.theoretical(), validationResult, raw.ivPoints());
                }
            }
        }
        throw new UnsupportedVendorException(name);
    }

    private String readAs(MultipartFile file, String encoding){
        try {
            return new String(file.getBytes(), Charset.forName(encoding));
        } catch (Exception e) {
            throw new UnsupportedVendorException(file.getOriginalFilename(), e);
        }
    }
}
