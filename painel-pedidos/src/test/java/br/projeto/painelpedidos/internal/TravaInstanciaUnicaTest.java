package br.projeto.painelpedidos.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TravaInstanciaUnicaTest {

    @TempDir
    Path diretorio;

    @Test
    void segundaAberturaComAMesmaIdentidadeEBloqueada() {
        TravaInstanciaUnica primeira = new TravaInstanciaUnica(diretorio, "meu-sistema-de-delivery");
        TravaInstanciaUnica segunda = new TravaInstanciaUnica(diretorio, "meu-sistema-de-delivery");

        assertThat(primeira.adquirir(), is(true));
        assertThat(segunda.adquirir(), is(false));
        primeira.liberar();
    }

    @Test
    void liberarPermiteReabrir() {
        TravaInstanciaUnica primeira = new TravaInstanciaUnica(diretorio, "app");
        assertThat(primeira.adquirir(), is(true));
        primeira.liberar();

        TravaInstanciaUnica reaberta = new TravaInstanciaUnica(diretorio, "app");
        assertThat(reaberta.adquirir(), is(true));
        reaberta.liberar();
    }

    @Test
    void identidadesDiferentesConvivem() {
        TravaInstanciaUnica a = new TravaInstanciaUnica(diretorio, "loja-a");
        TravaInstanciaUnica b = new TravaInstanciaUnica(diretorio, "loja-b");

        assertThat(a.adquirir(), is(true));
        assertThat(b.adquirir(), is(true));
        a.liberar();
        b.liberar();
    }

    @Test
    void nomePadraoIgualAoDoComponenteOficial() {
        TravaInstanciaUnica trava = new TravaInstanciaUnica("meu-sistema-de-delivery");

        assertThat(trava.getArquivo(), is(Path.of(System.getProperty("java.io.tmpdir"),
                "painel_pedidos-meu-sistema-de-delivery.lock")));
    }

    @Test
    void nomeDoArquivoDerivaDaIdentidadeDeFormaSegura() {
        TravaInstanciaUnica trava = new TravaInstanciaUnica(diretorio, "minha loja/../x");

        assertThat(trava.getArquivo().getFileName().toString(), endsWith("painel_pedidos-minhalojax.lock"));
        assertThat(trava.getArquivo().getParent(), is(diretorio));
    }
}
