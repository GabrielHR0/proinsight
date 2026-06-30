package com.prosup.proinsight.exception;

/**
 * Exceção lançada quando validação de entrada falha.
 * 
 * Spring retorna: 400 Bad Request
 */
public class ValidacaoException extends RuntimeException {
    
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
    
    public ValidacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
