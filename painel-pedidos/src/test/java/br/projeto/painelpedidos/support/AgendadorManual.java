package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.internal.presenter.Agendador;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Agendador de teste: guarda as tarefas e as dispara sob comando.
 */
public class AgendadorManual implements Agendador {

    public static final class TarefaManual implements Tarefa {

        public final Duration tempo;
        public final Runnable acao;
        public final boolean periodica;
        public boolean cancelada;

        TarefaManual(Duration tempo, Runnable acao, boolean periodica) {
            this.tempo = tempo;
            this.acao = acao;
            this.periodica = periodica;
        }

        @Override
        public void cancelar() {
            cancelada = true;
        }
    }

    public final List<TarefaManual> tarefas = new ArrayList<>();

    @Override
    public Tarefa agendarUmaVez(Duration atraso, Runnable acao) {
        TarefaManual tarefa = new TarefaManual(atraso, acao, false);
        tarefas.add(tarefa);
        return tarefa;
    }

    @Override
    public Tarefa agendarPeriodico(Duration intervalo, Runnable acao) {
        TarefaManual tarefa = new TarefaManual(intervalo, acao, true);
        tarefas.add(tarefa);
        return tarefa;
    }

    public TarefaManual periodica() {
        return tarefas.stream().filter(t -> t.periodica).findFirst().orElseThrow();
    }

    /** Dispara as tarefas unicas pendentes. */
    public void dispararUnicas() {
        for (TarefaManual tarefa : List.copyOf(tarefas)) {
            if (!tarefa.periodica && !tarefa.cancelada) {
                tarefa.cancelada = true;
                tarefa.acao.run();
            }
        }
    }

    public List<TarefaManual> unicasAtivas() {
        return tarefas.stream().filter(t -> !t.periodica && !t.cancelada).toList();
    }
}
