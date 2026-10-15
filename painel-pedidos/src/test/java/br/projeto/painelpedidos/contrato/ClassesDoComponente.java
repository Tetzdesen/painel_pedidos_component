package br.projeto.painelpedidos.contrato;

import br.projeto.painelpedidos.aplicacao.PainelPedidoFacade;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Lista as classes compiladas do componente (target/classes), sem as de teste.
 */
final class ClassesDoComponente {

    private ClassesDoComponente() {
    }

    static List<Class<?>> todas() {
        try {
            Path raiz = Path.of(PainelPedidoFacade.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (Stream<Path> arquivos = Files.walk(raiz)) {
                return arquivos.filter(arquivo -> arquivo.toString().endsWith(".class"))
                        .map(arquivo -> raiz.relativize(arquivo).toString())
                        .map(relativo -> relativo.substring(0, relativo.length() - ".class".length())
                                .replace('/', '.').replace('\\', '.'))
                        .map(ClassesDoComponente::carregar)
                        .toList();
            }
        } catch (IOException erro) {
            throw new UncheckedIOException(erro);
        } catch (URISyntaxException erro) {
            throw new IllegalStateException(erro);
        }
    }

    private static Class<?> carregar(String nome) {
        try {
            return Class.forName(nome, false, ClassesDoComponente.class.getClassLoader());
        } catch (ClassNotFoundException erro) {
            throw new IllegalStateException(erro);
        }
    }
}
