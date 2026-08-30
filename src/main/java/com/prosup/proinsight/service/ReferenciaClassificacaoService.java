package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.NivelReferenciaResponse;
import com.prosup.proinsight.api.dto.response.ReferenciaClassificacaoResponse;
import com.prosup.proinsight.domain.enums.Sexo;
import com.prosup.proinsight.domain.model.ClassificacaoLegivel;
import com.prosup.proinsight.domain.model.composite.Component;
import com.prosup.proinsight.domain.model.composite.Composite;
import com.prosup.proinsight.domain.model.composite.classes.NivelImc;
import com.prosup.proinsight.domain.model.composite.classes.NivelVo2Max;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaIdade;
import com.prosup.proinsight.domain.model.composite.tabelas.TabelaSexo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolve a referência de classificação de uma tabela para o cliente
 * (sexo + faixa etária), retornando todas as faixas de níveis. Compartilhado
 * entre a listagem de histórico e o resultado imediato da avaliação.
 */
@Service
public class ReferenciaClassificacaoService {

    public ReferenciaClassificacaoResponse extrair(Component raiz, Sexo sexo, Integer idade) {
        if (!(raiz instanceof Composite composite)) {
            return null;
        }

        List<NivelImc> niveisImc = new ArrayList<>();
        List<TabelaSexo> tabelasSexo = new ArrayList<>();
        for (Component child : composite.getChildren()) {
            if (child instanceof NivelImc nivel) {
                niveisImc.add(nivel);
            } else if (child instanceof TabelaSexo tabela) {
                tabelasSexo.add(tabela);
            }
        }

        if (!niveisImc.isEmpty()) {
            List<NivelReferenciaResponse> niveis = ordenarPorMinimo(niveisImc.stream()
                .map(nivel -> toReferencia(nivel.getClassificacao(), nivel.getMin(), nivel.getMax(),
                    nivel.getTipoMin() != null ? nivel.getTipoMin().name() : null,
                    nivel.getTipoMax() != null ? nivel.getTipoMax().name() : null))
                .collect(Collectors.toList()));
            return niveis.isEmpty() ? null
                : new ReferenciaClassificacaoResponse(null, null, null, niveis);
        }

        if (sexo == null) {
            return null;
        }
        for (TabelaSexo tabela : tabelasSexo) {
            if (tabela.getSexo() != sexo) {
                continue;
            }
            TabelaIdade faixa = selecionarFaixaEtaria(tabela, idade);
            if (faixa == null) {
                return null;
            }
            List<NivelReferenciaResponse> niveis = ordenarPorMinimo(faixa.getChildren().stream()
                .filter(NivelVo2Max.class::isInstance)
                .map(nivel -> {
                    NivelVo2Max n = (NivelVo2Max) nivel;
                    return toReferencia(n.getClassificacao(), n.getMin(), n.getMax(),
                        n.getTipoMin() != null ? n.getTipoMin().name() : null,
                        n.getTipoMax() != null ? n.getTipoMax().name() : null);
                })
                .collect(Collectors.toList()));
            if (niveis.isEmpty()) {
                return null;
            }
            return new ReferenciaClassificacaoResponse(
                sexo.name(), faixa.getIdadeMin(), faixa.getIdadeMax(), niveis);
        }
        return null;
    }

    private static TabelaIdade selecionarFaixaEtaria(TabelaSexo tabela, Integer idade) {
        List<TabelaIdade> faixas = new ArrayList<>();
        for (Component child : tabela.getChildren()) {
            if (child instanceof TabelaIdade faixa) {
                faixas.add(faixa);
            }
        }
        if (faixas.isEmpty()) {
            return null;
        }
        if (idade != null) {
            for (TabelaIdade faixa : faixas) {
                if (idade >= faixa.getIdadeMin() && idade <= faixa.getIdadeMax()) {
                    return faixa;
                }
            }
        }
        TabelaIdade maisProxima = null;
        long melhorDistancia = Long.MAX_VALUE;
        for (TabelaIdade faixa : faixas) {
            long distancia;
            if (idade == null) {
                distancia = 0;
            } else if (idade < faixa.getIdadeMin()) {
                distancia = faixa.getIdadeMin() - idade;
            } else if (idade > faixa.getIdadeMax()) {
                distancia = idade - faixa.getIdadeMax();
            } else {
                distancia = 0;
            }
            if (distancia < melhorDistancia) {
                melhorDistancia = distancia;
                maisProxima = faixa;
            }
        }
        return maisProxima;
    }

    private static List<NivelReferenciaResponse> ordenarPorMinimo(List<NivelReferenciaResponse> niveis) {
        if (niveis.size() < 2) {
            return niveis;
        }
        List<NivelReferenciaResponse> ordenados = new ArrayList<>(niveis);
        ordenados.sort(Comparator.comparing(NivelReferenciaResponse::min,
            Comparator.nullsFirst(Comparator.naturalOrder())));
        return List.copyOf(ordenados);
    }

    private static NivelReferenciaResponse toReferencia(String classificacao, Double min, Double max,
                                                       String tipoMin, String tipoMax) {
        return new NivelReferenciaResponse(
            classificacao,
            ClassificacaoLegivel.humanizar(classificacao),
            min,
            max,
            tipoMin,
            tipoMax
        );
    }
}
