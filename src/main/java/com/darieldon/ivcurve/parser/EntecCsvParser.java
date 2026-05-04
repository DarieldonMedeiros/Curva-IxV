package com.darieldon.ivcurve.parser;

import com.darieldon.ivcurve.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h1>Parser para arquivos ENTEC</h1>
 *
 * <p>Os arquivos ENTEC possuem 201 linhas no total, organizadas da seguinte forma:</p>
 *
 * <h2>Estrutura do Arquivo</h2>
 * <ul>
 *   <li><b>Linha 0 (cabeçalho):</b><br>
 *       V(V); I(A); P(W); V_STC(V); I_STC(A); P_STC(W); Pm_STC(W); Voc_STC(V);
 *       Isc_STC(A); Vm_STC(V); Im_STC(A); FF_STC; Data; Hora; Versão do Firmware
 *   </li>
 *
 *   <li><b>Linha 1 (primeiro ponto IV + resumo STC):</b><br>
 *       Exemplo: 0.00;27.780;0.00;0.00;19.061;0.00;20710.59;1417.18;19.061;
 *       1156.76;17.904;0.767;12/03/26;11:00:31;6.5
 *   </li>
 *
 *   <li><b>Linhas 2–13 (pares de linhas = rótulo + valor):</b>
 *     <ul>
 *       <li>Par 1 (linhas 2–3): Pm(W); Voc(V); Isc(A); Vm(V); Im(A); FF → valores medidos</li>
 *       <li>Par 2 (linhas 4–5): Pm_nom(W); Voc_nom(V); Isc_nom(A); ...; Mod_ser; Mod_par → nominais + configuração</li>
 *       <li>Par 3 (linhas 6–7): G(W/m²); Tc(°C); Beta Mod(%) → condições ambientais</li>
 *       <li>Par 4 (linhas 8–9): Tc_Cel(°C); PT1000_X → sensores de temperatura</li>
 *       <li>Par 5 (linhas 10–11): G_front; G_back → irradiância frontal/traseira</li>
 *       <li>Par 6 (linhas 12–13): Nome do Módulo → identificação</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Linhas 14–200:</b><br>
 *       Dados da curva IV (186 linhas), com 6 colunas:
 *       V; I; P; V_STC; I_STC; P_STC
 *   </li>
 * </ul>
 *
 * <h2>Estratégia de Uso dos Valores</h2>
 * <ul>
 *   <li>Curva IV exibida → colunas 3 e 4 (V_STC e I_STC), corrigidas para STC</li>
 *   <li>Voc, Isc, Vm, Im → resumo STC da linha 1 (colunas 7–10)</li>
 *   <li>FF → coluna 11 da linha 1 (FF_STC) ou calculado</li>
 *   <li>Irradiância real → G(W/m²) da linha 7, coluna 6</li>
 *   <li>Temperatura real → Tc(°C) da linha 7, coluna 7</li>
 *   <li>Valores teóricos (STC): Voc_nom × nMod, Isc_nom × nStr (25 °C, 1000 W/m²)</li>
 * </ul>
 *
 * <h2>Detecção de Módulo</h2>
 * <p>Exemplo: Pm_nom / Mod_ser = 20850 / 30 = 695 → módulo de 695 Wp</p>
 *
 * <h2>Metadados do Nome de Arquivo</h2>
 * <p>Formato do nome:</p>
 * <ul>
 *   <li>SES_INV-{PARQUE}_{SUBCAMPO}_{INVERSOR}-{PV}.csv (underscores substituem pontos)</li>
 *   <li>SES.INV-{PARQUE}.{SUBCAMPO}.{INVERSOR}-{PV}.csv (formato original)</li>
 * </ul>
 *
 * <p>Exemplo: <code>SES_INV-4_2_01-001.csv</code> → parque=4, subcampo=2, inversor=01, pv=001</p>
 */

@Component
public class EntecCsvParser implements VendorParser{

    private static final String ENTEC_HEADER_SIGNATURE = "V(V)";

    @Override
    public boolean supports(String content) {

        // O cabeçalho ENTEC sempre começa com V(V) e usa ponto e vírgula
        String firstLine = content.split("\\r?\\n")[0].trim();
        return firstLine.startsWith(ENTEC_HEADER_SIGNATURE)
                && firstLine.contains(";")
                & firstLine.contains("Pm_STC");
    }

    /**
     * <h1>Detecção da Potência (Power) do Módulo</h1>
     *
     * <p>Detecta a potência nominal do módulo calculando
     * <code>Pm_nom / Mod_ser</code> diretamente a partir do CSV.</p>
     *
     * <h2>Exemplo em Arquivo Real</h2>
     * <ul>
     *   <li><b>Linha 4 (índice 4):</b> contém os rótulos, incluindo
     *       <code>"Pm_nom(W)"</code> e <code>"Mod_ser"</code></li>
     *   <li><b>Linha 5 (índice 5):</b>
     *       <code>20850;1431.00;18.440;1194.00;17.470;0.790;30;1;66</code></li>
     *   <li>Valores extraídos:
     *       <ul>
     *         <li><code>pmNom = 20850</code></li>
     *         <li><code>modSer = 30</code></li>
     *       </ul>
     *   </li>
     *   <li>Cálculo:
     *       <pre>20850 / 30 = 695 W</pre>
     *   </li>
     * </ul>
     *
     * <p>Esse valor é utilizado para identificar o módulo correspondente
     * no banco de dados.</p>
     */
    @Override
    public int detectModulePower(String content) {
        String[] lines = content.split("\\r?\\n");
        // Varre procurando a linha de rótulos que contém Pm_nom(W)
        for (int i = 0; i < lines.length - 1; i ++) {
            String[] cols = lines[i].split(";");
            if (cols.length > 6 && cols[6].trim().equals("Pm_nom(W)")) {
                // Próxima linha contém os valores
                String[] values = lines[i + 1].split(";");
                if (values.length > 13) {
                    try {
                        double pmNom = Double.parseDouble(values[6].trim());

                        int modSer = Integer.parseInt(values[12].trim());

                        return (int) Math.round(pmNom / modSer);
                    } catch(NumberFormatException ignored) {}
                }
            }
        }
        throw new RuntimeException("Não foi possível detectar a potência do módulo no arquivo ENTEC");
    }

    @Override
    public IVCurveReport parse(String content, String fileName, ModuleConfig moduleConfig) {

        String[] lines = content.split("\\r?\\n");

        if (lines.length < 2) {
            throw new RuntimeException("Arquivo ENTEC inválido (menos de 2 linhas): " + fileName);
        }

        // 1. Lê o mapa de metadados
        Map<String, String> metadata = parseMetadata(lines);

        // 2. Valores STC (da linha 1, cols 6 - 14)
        // Estes são os valores do ponto de operação corrigidos para STC
        // pelo próprio equipamento ENTEC.
        double pm = d(metadata, "Pm(W)");
        double pmSTC = d(metadata, "Pm_STC(W)");
        double voc = d(metadata, "Voc(V)");
        double isc = d(metadata, "Isc(A)");
        double vm = d(metadata, "Vm(V)");
        double im = d(metadata, "Im(A)");
        double ff = d(metadata, "FF");

        // 3. Condições reais (G e Tc da linha 7)
        double irradiance = d(metadata, "G(W/m2)");
        double temperature = d(metadata, "Tc(°C)");
        int modSer = (int) d(metadata, "Mod_ser");
        int modPar = (int) d(metadata, "Mod_par");

        // Usa nModules e nStrings do CSV para sobrescrever do módulo, se divergentes
        // (garante consistência mesmo se o banco tiver config diferente)
        int effectiveNMod = modSer > 0 ? modSer : moduleConfig.nModules();
        int effectiveNStr = modPar > 0 ? modPar : moduleConfig.nStrings();

        // 4. Teórico em STC
        // Como os valores medidos já estão em STC, o teórico também é em STC:
        //   Voc_teo_STC = nMod × Voc_nom = 30 × 47,7 = 1431,0 V
        //   Isc_teo_STC = nStr × Isc_nom = 1 × 18,44 = 18,44 A
        double vocTeo = effectiveNMod * moduleConfig.vocNominal();
        double iscTeo = effectiveNStr * moduleConfig.iscNominal();

        // 5. Potência nominal da String
        // pmaxNominalString = Pmax_módulo × nMod × nStr = 695 × 30 × 1 = 20850 W
        double pmaxNomString = moduleConfig.pmaxNominal() * effectiveNMod * effectiveNStr;

        // 6. Monta MeasurementMetrics
        // Nota: MeasurementMetrics.of() calcula pmax = vm * im e desempenho
        // internamente. Passamos o ff diretamente sobrescrevendo após a criação.
        // Usamos a versão com ff explícito para ser fiel ao valor ENTEC.
        double pmaxCalc = vm * im;
        // Se o CSV fornece FF diretamente, utilizamos ele (mais preciso)
        // Caso esteja zerado, calculamos FF = Pmax / (Voc * Isc)
        double ffFinal = ff > 0 ? ff : (voc > 0 && isc > 0) ? pmaxCalc / (voc * isc) : 0.0;
        double performance = pmaxNomString > 0 ? (pmSTC / pmaxNomString) * 100.0 : 0.0;

        MeasurementMetrics measurement = new MeasurementMetrics(voc, isc, vm, im, pm, irradiance, temperature, ffFinal, performance);

        // 7. Monta ThereticalMetrics
        TheoreticalMetrics theoretical = TheoreticalMetrics.of(voc, isc, vocTeo, iscTeo);

        // 8. Curva IxV
        List<IVPoint> ivPoints = parseIVCurve(lines);

        // 9. Metadados do nome de arquivo
        MeasurementMetadata meta = parseFilenameMetadata(fileName, metadata);

        return new IVCurveReport(meta, measurement, theoretical, null, ivPoints);
    }

    /**
     * <h1>Parse de Metadados ENTEC</h1>
     *
     * <p>Constrói um mapa chave→valor a partir das seções de metadados
     * presentes em arquivos ENTEC.</p>
     *
     * @param lines linhas do CSV contendo cabeçalho e metadados
     * @return mapa de rótulos e valores extraídos
     */
    private Map<String, String> parseMetadata (String[] lines) {
        Map<String, String> metadata = new LinkedHashMap<>();

        // 1. Processa o cabeçalho inicial (Linha 0 e 1)
        parseInitialHeader(lines, metadata);

        // 2. Processa os blocos de pares (Linha 2 em diante)
        parseMetadataBlocks(lines, metadata);

        return metadata;
    }

    /**
     * <h1>Processamento do Cabeçalho Inicial</h1>
     *
     * <p>Processa as duas primeiras linhas do arquivo ENTEC
     * (linha 0 = cabeçalho, linha 1 = valores STC) e adiciona
     * os pares chave→valor ao mapa de metadados.</p>
     *
     * @param lines linhas do CSV
     * @param metadata mapa de metadados a ser preenchido
     */
    private void parseInitialHeader(String[] lines, Map<String, String> metadata) {

        if (lines.length < 2) return;

        String[] headers = lines[0].split(";");
        String[] values = lines[1].split(";");

        addValuesToMap(metadata, headers, values, 6);
    }

    /**
     * <h1>Processamento dos Blocos de Metadados</h1>
     *
     * <p>Itera pelas linhas 2–13 do arquivo ENTEC, interpretando pares
     * alternados de rótulo/valor. Adiciona os pares ao mapa de metadados.</p>
     *
     * @param lines linhas do CSV
     * @param metadata mapa de metadados a ser preenchido
     */
    private void parseMetadataBlocks(String[] lines, Map<String, String> metadata) {
        String[] currentLabels = null;

        // Limita o loop entre a linha 2 e 13 ou o fim do array
        int maxLine = Math.min(lines.length, 14);

        for (int i = 2; i < maxLine; i ++) {
            String[] cols = lines[i].split(";");
            if (cols.length <=6 || cols[6].trim().isEmpty()) break;

            if (isLabel(cols[6].trim())) {
                currentLabels = extractLabels(cols);
            }
            else if (currentLabels != null) {
                addValuesToMap(metadata, currentLabels, cols, 6);
                currentLabels = null; // Reset para o próximo par
            }
        }
    }

    /**
     * <h1>Extração de Rótulos</h1>
     *
     * <p>Extrai os rótulos de metadados a partir das colunas
     * de uma linha do CSV, iniciando no índice 6.</p>
     *
     * @param cols colunas da linha atual
     * @return array de rótulos extraídos
     */
    private String[] extractLabels(String[] cols) {
        String[] labels = new String[cols.length - 6];
        for (int c = 6; c < cols.length; c ++) {
            labels[c - 6] = cols[c].trim();
        }
        return labels;
    }

    /**
     * <h1>Adição de Valores ao Mapa</h1>
     *
     * <p>Adiciona pares chave→valor ao mapa de metadados,
     * alinhando os rótulos com os valores correspondentes.</p>
     *
     * @param metadata mapa de metadados a ser preenchido
     * @param keys array de rótulos
     * @param values array de valores
     * @param startIndex índice inicial das colunas relevantes
     */
    private void addValuesToMap(Map<String, String> metadata, String[] keys, String[] values, int startIndex) {
        int maxCol = Math.min(keys.length + startIndex, values.length);

        for (int c = startIndex; c < maxCol; c++) {
            String key = (startIndex == 0) ? keys[c] : keys[c - startIndex];
            String value = values[c].trim();

            if(!key.isEmpty() && !value.isEmpty()) {
                metadata.put(key, value);
            }
        }
    }

    /**
     * <h1>Detecção de Rótulos no CSV</h1>
     *
     * <p>Verifica se uma string parece ser um rótulo (contém letras)
     * em vez de um valor numérico.</p>
     *
     * <h2>Exemplos</h2>
     * <ul>
     *   <li><b>Rótulos:</b> "Pm(W)", "G(W/m2)", "Tc(°C)", "Nome do Modulo"</li>
     *   <li><b>Valores:</b> "20850", "1447.03", "39.39", "-0.260", "CAN-695"</li>
     * </ul>
     *
     * <h2>Atenção</h2>
     * <p>Strings como <code>"CAN-695"</code> seriam detectadas como valores
     * por conter dígitos. No entanto, como aparecem imediatamente após o
     * rótulo <code>"Nome do Modulo"</code>, são tratadas corretamente pela
     * lógica de pares rótulo/valor.</p>
     */
    private boolean isLabel (String s) {
        // Tem letras, mas não é um número simples (inteiro ou decimal)
        return s.matches(".*[a-zA-Z()/°].*") && !s.matches("^-?\\d+\\.?\\d*$");
    }

    /**
     * <h1>Extração da Curva IV Corrigida para STC</h1>
     *
     * <p>Extrai os pontos da curva IxV corrigida para STC a partir das
     * colunas 3 e 4 do arquivo CSV.</p>
     *
     * <h2>Estrutura das Colunas</h2>
     * <ul>
     *   <li><b>Coluna 0–2:</b> valores medidos nas condições reais de campo.</li>
     *   <li><b>Coluna 3:</b> tensão corrigida para STC (<code>V_STC(V)</code>).</li>
     *   <li><b>Coluna 4:</b> corrente corrigida para STC (<code>I_STC(A)</code>).</li>
     * </ul>
     *
     * <h2>Finalidade</h2>
     * <p>A curva corrigida para STC é utilizada porque permite comparação
     * consistente entre medições realizadas em dias diferentes.</p>
     *
     * <h2>Detalhes</h2>
     * <ul>
     *   <li>Total de pontos extraídos: <b>200</b> (linhas 1–200 do arquivo).</li>
     * </ul>
     *
     * @param lines linhas do CSV contendo os dados da curva IxV
     * @return lista de pontos da curva corrigida para STC
     */
    private List<IVPoint> parseIVCurve(String[] lines) {
        List<IVPoint> points = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String[] cols = lines[i].split(";");
            if (cols.length >= 5) {
                try {
                    double vSTC = Double.parseDouble(cols[3].trim());
                    double iSTC = Double.parseDouble(cols[4].trim());
                    points.add(IVPoint.of(vSTC, iSTC));
                } catch (NumberFormatException ignored) {}
            }
        }
        return points;
    }

    /**
     * <h1>Extração de Identificadores do Arquivo ENTEC</h1>
     *
     * <p>Extrai os identificadores de parque, subcampo, inversor e PV
     * a partir do nome do arquivo CSV gerado pelo ENTEC.</p>
     *
     * <h2>Padrões Aceitos</h2>
     * <ul>
     *   <li><code>SES_INV-{PARQUE}_{SUBCAMPO}_{INVERSOR}-{PV}.csv</code></li>
     *   <li><code>SES.INV-{PARQUE}.{SUBCAMPO}.{INVERSOR}-{PV}.csv</code></li>
     * </ul>
     *
     * <h2>Exemplos</h2>
     * <ul>
     *   <li><code>"SES_INV-4_2_01-001.csv"</code> → parque=4, subcampo=2, inversor=01, pv=001</li>
     *   <li><code>"SES.INV-4.2.01-001.csv"</code> → idem</li>
     * </ul>
     *
     * <h2>Regex Utilizada</h2>
     * <pre>
     * SES[_.]INV-(\w+)[_.](\w+)[_.](\w+)-(\w+)\.csv
     * </pre>
     *
     * @param fileName nome do arquivo CSV
     * @return objeto contendo parque, subcampo, inversor e PV extraídos
     */

    private MeasurementMetadata parseFilenameMetadata (String fileName, Map<String, String> metadata) {

        String parque   = "—";
        String subcampo = "—";
        String inversor = "—";
        String pvStr    = "—";

        // Remove o path se vier com o diretório
        String baseName = fileName.replaceAll(".*[\\\\/]", "").replaceAll("\\.csv$", "");

        Pattern p = Pattern.compile(
                "SES[_.]INV-(\\w+)[_.](\\w+)[_.](\\w+)-(\\w+)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(baseName);

        if (m.find()) {
            parque = m.group(1);
            subcampo = m.group(2);
            inversor = m.group(3);
            pvStr = m.group(4);
        }

        // Data e hora da linha 1, cols 12-13 do CSV
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        try {
            String rawDate = metadata.getOrDefault("Data", "").trim();
            String rawTime = metadata.getOrDefault("Hora", "").trim();

            // Data no formato DD/MM/YYYY
            date = LocalDate.parse(rawDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            time = LocalTime.parse(rawTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception ignored) {}

        return new MeasurementMetadata(
                date, time, parque, subcampo, inversor, pvStr, fileName, Vendor.ENTEC);
    }

    private double d(Map<String, String> m, String key) {
        try {
            return Double.parseDouble(
                    m.getOrDefault(key, "0")
                            .trim()
                            .replace(",",".")
                            .replaceAll("[^\\d.\\-]", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
