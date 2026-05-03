package com.darieldon.ivcurve.parser;

import org.springframework.stereotype.Component;

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
}
