package br.projeto.painelpedidos.tempo;

import java.time.LocalDateTime;

/**
 * Fonte de "agora" usada pelo painel (SLA, transicoes e filtros de periodo).
 */
public interface IRelogioService {

    LocalDateTime agora();
}
