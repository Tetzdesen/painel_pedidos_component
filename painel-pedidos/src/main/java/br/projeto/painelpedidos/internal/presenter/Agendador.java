package br.projeto.painelpedidos.internal.presenter;

import java.time.Duration;

/**
 * Abstrai os temporizadores do painel (monitor de SLA e fim do destaque), para
 * que o Presenter seja testado sem Swing e sem esperar o tempo passar.
 */
public interface Agendador {

    interface Tarefa {

        void cancelar();
    }

    Tarefa agendarUmaVez(Duration atraso, Runnable acao);

    Tarefa agendarPeriodico(Duration intervalo, Runnable acao);
}
