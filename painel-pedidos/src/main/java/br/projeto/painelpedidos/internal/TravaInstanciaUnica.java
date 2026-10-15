package br.projeto.painelpedidos.internal;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Garante um unico painel aberto por identidade de aplicacao, com uma trava de
 * arquivo. O sistema operacional libera a trava se o processo morrer.
 */
public class TravaInstanciaUnica {

    private final Path arquivo;
    private FileChannel canal;
    private FileLock trava;

    public TravaInstanciaUnica(String identificadorAplicacao) {
        this(Path.of(System.getProperty("java.io.tmpdir")), identificadorAplicacao);
    }

    public TravaInstanciaUnica(Path diretorio, String identificadorAplicacao) {
        String nomeSeguro = identificadorAplicacao.trim().replaceAll("[^A-Za-z0-9_-]", "");
        this.arquivo = diretorio.resolve("painel_pedidos-" + nomeSeguro + ".lock");
    }

    public Path getArquivo() {
        return arquivo;
    }

    /**
     * @return {@code true} se a trava foi obtida; {@code false} se outro painel ja a detem
     */
    public synchronized boolean adquirir() {
        if (trava != null) {
            return false;
        }
        try {
            canal = FileChannel.open(arquivo, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            trava = canal.tryLock();
        } catch (OverlappingFileLockException jaTravadoNestaJvm) {
            trava = null;
        } catch (IOException erro) {
            fecharCanal();
            throw new IllegalStateException("Nao foi possivel criar a trava de instancia unica em " + arquivo, erro);
        }
        if (trava == null) {
            fecharCanal();
            return false;
        }
        return true;
    }

    public synchronized void liberar() {
        try {
            if (trava != null) {
                trava.release();
            }
        } catch (IOException erro) {
            // o fechamento do canal abaixo tambem libera a trava
        } finally {
            trava = null;
            fecharCanal();
        }
    }

    private void fecharCanal() {
        if (canal != null) {
            try {
                canal.close();
            } catch (IOException erro) {
                // nada a fazer
            }
            canal = null;
        }
    }
}
