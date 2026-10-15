package br.projeto.painelpedidos.internal;

import br.projeto.painelpedidos.som.IEstrategiaSom;

/**
 * Estrategia usada quando SOM_ATIVO=false: nao emite som.
 */
public class SomSilenciosoStrategy implements IEstrategiaSom {

    @Override
    public void tocar() {
        // silencioso por definicao
    }
}
