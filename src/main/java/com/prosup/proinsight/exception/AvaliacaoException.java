package com.prosup.proinsight.exception;

/**
 * Exceção genérica para erros durante avaliação.
 * 
 * Spring retorna: 500 Internal Server Error
 */
public class AvaliacaoException extends RuntimeException {
    
    public AvaliacaoException(String mensagem) {
        super(mensagem);
    }
    
    public AvaliacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
