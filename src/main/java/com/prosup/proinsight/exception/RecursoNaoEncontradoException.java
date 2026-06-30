package com.prosup.proinsight.exception;

/**
 * Exceção lançada quando recurso não é encontrado no banco.
 * 
 * Spring retorna: 404 Not Found
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
    
    public RecursoNaoEncontradoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
