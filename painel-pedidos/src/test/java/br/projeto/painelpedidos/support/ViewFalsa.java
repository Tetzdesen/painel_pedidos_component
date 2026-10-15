package br.projeto.painelpedidos.support;

import br.projeto.painelpedidos.internal.presenter.CardViewModel;
import br.projeto.painelpedidos.internal.presenter.EventosPainelPedido;
import br.projeto.painelpedidos.internal.presenter.RaiaViewModel;
import br.projeto.painelpedidos.internal.presenter.VisaoPainel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * View passiva de teste: guarda o ultimo estado desenhado.
 */
public class ViewFalsa implements VisaoPainel {

    public EventosPainelPedido eventos;
    public List<String> opcoesPeriodo = List.of();
    public int periodoSelecionado = -1;
    public List<RaiaViewModel> raias = List.of();
    public int renderizacoes;
    public String selecionado = "";
    public boolean visualizarHabilitado;
    public String textoAcao = "";
    public boolean acaoHabilitada;
    public boolean campoDataVisivel;
    public java.time.LocalDate dataDoCampo;
    public boolean respostaConfirmacao = true;
    public final List<String> confirmacoes = new ArrayList<>();
    public final List<String> titulosDeErro = new ArrayList<>();
    public String mensagem = "";
    public final List<String> mensagens = new ArrayList<>();
    public final List<String> erros = new ArrayList<>();
    public final List<String> focos = new ArrayList<>();
    public boolean maximizada;

    @Override
    public void definirEventos(EventosPainelPedido eventos) {
        this.eventos = eventos;
    }

    @Override
    public void exibirOpcoesPeriodo(List<String> opcoes, int indiceSelecionado) {
        opcoesPeriodo = opcoes;
        periodoSelecionado = indiceSelecionado;
    }

    @Override
    public void exibirRaias(List<RaiaViewModel> raias) {
        this.raias = raias;
        renderizacoes++;
    }

    @Override
    public void exibirPedidoSelecionado(String texto) {
        selecionado = texto;
    }

    @Override
    public void habilitarVisualizarPedido(boolean habilitado) {
        visualizarHabilitado = habilitado;
    }

    @Override
    public void exibirAcaoDoSelecionado(String texto, boolean habilitado) {
        textoAcao = texto;
        acaoHabilitada = habilitado;
    }

    @Override
    public void exibirCampoData(boolean visivel, java.time.LocalDate data) {
        campoDataVisivel = visivel;
        dataDoCampo = data;
    }

    @Override
    public boolean confirmar(String titulo, String mensagem) {
        confirmacoes.add(titulo + "|" + mensagem);
        return respostaConfirmacao;
    }

    @Override
    public void exibirMensagem(String mensagem) {
        this.mensagem = mensagem;
        mensagens.add(mensagem);
    }

    @Override
    public void exibirErro(String titulo, String mensagem) {
        titulosDeErro.add(titulo);
        erros.add(mensagem);
    }

    @Override
    public void focarCard(String identificador) {
        focos.add(identificador);
    }

    @Override
    public void maximizar() {
        maximizada = true;
    }

    public List<String> titulos() {
        return raias.stream().map(RaiaViewModel::titulo).toList();
    }

    /** Identificadores dos cards da raia de indice informado (0 = Novo). */
    public List<String> idsDaRaia(int indice) {
        return raias.get(indice).cards().stream().map(CardViewModel::identificador).toList();
    }

    public Optional<CardViewModel> card(String identificador) {
        return raias.stream().flatMap(raia -> raia.cards().stream())
                .filter(card -> card.identificador().equals(identificador)).findFirst();
    }

    public int indiceDaRaiaDo(String identificador) {
        for (int indice = 0; indice < raias.size(); indice++) {
            if (idsDaRaia(indice).contains(identificador)) {
                return indice;
            }
        }
        return -1;
    }
}
