package br.projeto.painelpedidos.som;

/**
 * Estrategia (GoF Strategy) de como tocar o alerta sonoro de novos pedidos.
 * Implementacoes nao devem lancar excecao nem bloquear a interface.
 */
public interface IEstrategiaSom {

    void tocar();
}
