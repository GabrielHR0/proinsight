package com.prosup.proinsight.validator;

import com.prosup.proinsight.exception.ValidacaoException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador genérico com métodos reutilizáveis.
 * 
 * Spring: @Component faz com que Spring gerenie instância única (singleton).
 * Pode ser injetada em qualquer classe com @Autowired.
 */
@Component
public class RequestValidator {
    

    public void validarStringNaoVazia(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoException("Campo obrigatório: " + nomeCampo);
        }
    }

    public void validarObjetoNaoNulo(Object objeto, String nomeCampo) {
        if (objeto == null) {
            throw new ValidacaoException("Campo obrigatório: " + nomeCampo);
        }
    }
    

    public void validarListaNaoVazia(List<?> lista, String nomeCampo) {
        if (lista == null || lista.isEmpty()) {
            throw new ValidacaoException("Lista não pode estar vazia: " + nomeCampo);
        }
    }
    

    public void validarMinimo(double valor, double minimo, String nomeCampo) {
        if (valor < minimo) {
            throw new ValidacaoException(
                nomeCampo + " deve ser maior ou igual a " + minimo
            );
        }
    }

    public void validarIntervalo(double valor, double minimo, double maximo, String nomeCampo) {
        if (valor < minimo || valor > maximo) {
            throw new ValidacaoException(
                nomeCampo + " deve estar entre " + minimo + " e " + maximo
            );
        }
    }
}
