package br.projeto.painelpedidos.internal.swing;

import br.projeto.painelpedidos.internal.presenter.Agendador;
import java.time.Duration;
import javax.swing.Timer;

/**
 * Agendador baseado em {@link javax.swing.Timer}: as acoes rodam na thread do Swing.
 */
public class AgendadorSwing implements Agendador {

    @Override
    public Tarefa agendarUmaVez(Duration atraso, Runnable acao) {
        return iniciar(atraso, acao, false);
    }

    @Override
    public Tarefa agendarPeriodico(Duration intervalo, Runnable acao) {
        return iniciar(intervalo, acao, true);
    }

    private Tarefa iniciar(Duration tempo, Runnable acao, boolean repetir) {
        int milissegundos = (int) Math.min(Integer.MAX_VALUE, tempo.toMillis());
        Timer timer = new Timer(milissegundos, evento -> acao.run());
        timer.setRepeats(repetir);
        timer.start();
        return timer::stop;
    }
}
