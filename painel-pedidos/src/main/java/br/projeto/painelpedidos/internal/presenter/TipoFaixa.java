package br.projeto.painelpedidos.internal.presenter;

/**
 * Aparencia das faixas de estado do card.
 */
public enum TipoFaixa {
    /** "Aguardando pagamento" (amarelo). */
    ALERTA,
    /** "Pagamento confirmado" (verde). */
    SUCESSO,
    /** Informacao neutra de cancelado/credito (cinza). */
    NEUTRA,
    /** Rotulo "Prioridade" (vermelho). */
    PRIORIDADE
}
