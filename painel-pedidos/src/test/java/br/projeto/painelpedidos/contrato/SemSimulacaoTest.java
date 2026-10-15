package br.projeto.painelpedidos.contrato;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import br.projeto.painelpedidos.model.PedidoAbstract;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * O componente nao gera, simula nem semeia pedidos: os dados sao sempre do consumidor.
 */
class SemSimulacaoTest {

    private static final Pattern NOMES_PROIBIDOS =
            Pattern.compile(".*(simul|seed|semead|mock|fake|gerador|demo|exemplo).*");

    @Test
    void naoHaClassesDeSimulacaoSeedOuMock() {
        List<String> suspeitas = ClassesDoComponente.todas().stream()
                .map(Class::getSimpleName)
                .filter(nome -> NOMES_PROIBIDOS.matcher(nome.toLowerCase(Locale.ROOT)).matches())
                .toList();

        assertThat(suspeitas, empty());
    }

    @Test
    void naoHaPedidoConcretoNoComponente() {
        List<String> concretos = ClassesDoComponente.todas().stream()
                .filter(PedidoAbstract.class::isAssignableFrom)
                .filter(classe -> !Modifier.isAbstract(classe.getModifiers()))
                .map(Class::getName)
                .toList();

        assertThat(concretos, empty());
    }
}
