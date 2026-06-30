package com.prosup.proinsight.exception;

/**
 * Exceção lançada quando regra de negócio é violada.
 * 
 * Spring retorna: 422 Unprocessable Entity
 */
public class RegraNeggocioException extends RuntimeException {
    
    public RegraNeggocioException(String mensagem) {
        super(mensagem);
    }
    
    public RegraNeggocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
