package br.projeto.painelpedidos.som;

import java.awt.Toolkit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Alerta padrao: um tom senoidal de frequencia e duracao configuraveis.
 * Toca em uma thread propria; sem linha de audio disponivel, tenta o beep do
 * sistema e, se nem isso for possivel, fica em silencio.
 */
public final class SomTomStrategy implements IEstrategiaSom {

    private static final float TAXA_AMOSTRAGEM = 44_100f;

    private final int frequencia;
    private final int duracaoMs;

    public SomTomStrategy(int frequencia, int duracaoMs) {
        if (frequencia <= 0) {
            throw new IllegalArgumentException("Informe uma frequencia de som positiva");
        }
        if (duracaoMs <= 0) {
            throw new IllegalArgumentException("Informe uma duracao de som positiva");
        }
        this.frequencia = frequencia;
        this.duracaoMs = duracaoMs;
    }

    @Override
    public void tocar() {
        Thread thread = new Thread(this::tocarTom, "painel-pedidos-som");
        thread.setDaemon(true);
        thread.start();
    }

    private void tocarTom() {
        try {
            AudioFormat formato = new AudioFormat(TAXA_AMOSTRAGEM, 8, 1, true, false);
            try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                linha.open(formato);
                linha.start();
                byte[] amostras = gerarAmostras();
                linha.write(amostras, 0, amostras.length);
                linha.drain();
            }
        } catch (Exception | LinkageError erroAudio) {
            tocarBeepDoSistema();
        }
    }

    private byte[] gerarAmostras() {
        int quantidade = (int) (TAXA_AMOSTRAGEM * duracaoMs / 1000);
        byte[] amostras = new byte[quantidade];
        for (int indice = 0; indice < quantidade; indice++) {
            double angulo = 2.0 * Math.PI * indice * frequencia / TAXA_AMOSTRAGEM;
            amostras[indice] = (byte) (Math.sin(angulo) * 100);
        }
        return amostras;
    }

    private void tocarBeepDoSistema() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception | LinkageError semSom) {
            // Sem audio e sem beep (ex.: servidor headless): o alerta fica silencioso.
        }
    }
}
