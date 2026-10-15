package br.projeto.painelpedidos.contrato;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * Fixa o contrato publico: exatamente estes 14 tipos, nos pacotes do componente oficial, ficam fora
 * de "internal".
 * Falha nomeando o tipo que entrou ou saiu.
 */
class ContratoPublicoTest {

    private static final String BASE = "br.projeto.painelpedidos.";

    static final Set<String> CONTRATO = Set.of(
            BASE + "aplicacao.PainelPedidoFacade",
            BASE + "model.PedidoAbstract",
            BASE + "repositorio.IPedidoRepository",
            BASE + "configuracao.ConfiguracaoPainelPedido",
            BASE + "configuracao.LeitorConfiguracaoPainelPedido",
            BASE + "listener.IVisualizarPedidoListener",
            BASE + "listener.IConfirmarPagamentoListener",
            BASE + "listener.ICriarNovoPedidoListener",
            BASE + "som.IEstrategiaSom",
            BASE + "som.SomTomStrategy",
            BASE + "tempo.IRelogioService",
            BASE + "tempo.RelogioServiceSistema",
            BASE + "view.IPainelPedidoView",
            BASE + "view.PainelPedidoView");

    @Test
    void contratoPublicoTemExatamenteOs14Tipos() {
        Set<String> publicos = new TreeSet<>();
        for (Class<?> classe : ClassesDoComponente.todas()) {
            if (!classe.getName().startsWith(BASE + "internal.") && acessivelDeFora(classe)) {
                publicos.add(classe.getName().replace('$', '.'));
            }
        }

        Set<String> excedentes = new TreeSet<>(publicos);
        excedentes.removeAll(CONTRATO);
        Set<String> faltantes = new TreeSet<>(CONTRATO);
        faltantes.removeAll(publicos);

        assertTrue(excedentes.isEmpty(), "Tipos publicos fora do contrato (mova para internal): " + excedentes);
        assertTrue(faltantes.isEmpty(), "Tipos do contrato ausentes ou nao publicos: " + faltantes);
    }

    @Test
    void assinaturasPublicasSoUsamTiposDoContratoOuDoJdk() throws ClassNotFoundException {
        Set<String> violacoes = new TreeSet<>();
        for (String nome : CONTRATO) {
            Class<?> classe = Class.forName(nome, false, getClass().getClassLoader());
            List<Type> tipos = new ArrayList<>();
            tipos.add(classe.getGenericSuperclass());
            tipos.addAll(Arrays.asList(classe.getGenericInterfaces()));
            for (Constructor<?> construtor : classe.getDeclaredConstructors()) {
                if (visivel(construtor.getModifiers())) {
                    tipos.addAll(Arrays.asList(construtor.getGenericParameterTypes()));
                    tipos.addAll(Arrays.asList(construtor.getGenericExceptionTypes()));
                }
            }
            for (Method metodo : classe.getDeclaredMethods()) {
                if (visivel(metodo.getModifiers()) && !metodo.isSynthetic() && !metodo.isBridge()) {
                    tipos.add(metodo.getGenericReturnType());
                    tipos.addAll(Arrays.asList(metodo.getGenericParameterTypes()));
                    tipos.addAll(Arrays.asList(metodo.getGenericExceptionTypes()));
                }
            }
            for (Field campo : classe.getDeclaredFields()) {
                if (visivel(campo.getModifiers()) && !campo.isSynthetic()) {
                    tipos.add(campo.getGenericType());
                }
            }
            for (Type tipo : tipos) {
                coletarClasses(tipo).stream()
                        .filter(usado -> !permitido(usado))
                        .forEach(usado -> violacoes.add(classe.getSimpleName() + " -> " + usado.getName()));
            }
        }

        assertTrue(violacoes.isEmpty(), "Assinaturas publicas expondo tipos internos: " + violacoes);
    }

    private static boolean visivel(int modificadores) {
        return Modifier.isPublic(modificadores) || Modifier.isProtected(modificadores);
    }

    private static boolean permitido(Class<?> usado) {
        String nome = usado.getName();
        return usado.isPrimitive() || CONTRATO.contains(nome) || nome.startsWith("java.") || nome.startsWith("javax.");
    }

    private static List<Class<?>> coletarClasses(Type tipo) {
        List<Class<?>> classes = new ArrayList<>();
        if (tipo instanceof Class<?> classe) {
            classes.add(classe.isArray() ? classe.getComponentType() : classe);
        } else if (tipo instanceof ParameterizedType parametrizado) {
            classes.addAll(coletarClasses(parametrizado.getRawType()));
            for (Type argumento : parametrizado.getActualTypeArguments()) {
                classes.addAll(coletarClasses(argumento));
            }
        } else if (tipo instanceof GenericArrayType arranjo) {
            classes.addAll(coletarClasses(arranjo.getGenericComponentType()));
        } else if (tipo instanceof WildcardType curinga) {
            for (Type limite : curinga.getUpperBounds()) {
                classes.addAll(coletarClasses(limite));
            }
            for (Type limite : curinga.getLowerBounds()) {
                classes.addAll(coletarClasses(limite));
            }
        } else if (tipo instanceof TypeVariable<?> variavel) {
            for (Type limite : variavel.getBounds()) {
                classes.addAll(coletarClasses(limite));
            }
        }
        return classes;
    }

    private static boolean acessivelDeFora(Class<?> classe) {
        if (classe.isAnonymousClass() || classe.isLocalClass() || classe.isSynthetic()) {
            return false;
        }
        for (Class<?> atual = classe; atual != null; atual = atual.getDeclaringClass()) {
            if (!Modifier.isPublic(atual.getModifiers())) {
                return false;
            }
        }
        return true;
    }
}
