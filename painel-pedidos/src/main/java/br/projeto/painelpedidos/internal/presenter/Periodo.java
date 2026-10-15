package br.projeto.painelpedidos.internal.presenter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Opcoes do filtro "Periodo", na ordem do componente oficial, relativas ao dia
 * corrente do relogio.
 */
public enum Periodo {
    HOJE,
    ULTIMOS_7_DIAS,
    ULTIMOS_30_DIAS,
    TODOS,
    DATA_ESPECIFICA;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String rotulo(LocalDate hoje) {
        return switch (this) {
            case HOJE -> "Hoje (" + DATA.format(hoje) + ")";
            case ULTIMOS_7_DIAS -> "Últimos 7 dias";
            case ULTIMOS_30_DIAS -> "Últimos 30 dias";
            case TODOS -> "Todos";
            case DATA_ESPECIFICA -> "Data específica";
        };
    }

    /**
     * @param dataEspecifica usada somente em {@link #DATA_ESPECIFICA}
     */
    public boolean contem(LocalDate data, LocalDate hoje, LocalDate dataEspecifica) {
        return switch (this) {
            case HOJE -> data.equals(hoje);
            case ULTIMOS_7_DIAS -> !data.isBefore(hoje.minusDays(6)) && !data.isAfter(hoje);
            case ULTIMOS_30_DIAS -> !data.isBefore(hoje.minusDays(29)) && !data.isAfter(hoje);
            case TODOS -> true;
            case DATA_ESPECIFICA -> data.equals(dataEspecifica);
        };
    }

    public boolean exigeData() {
        return this == DATA_ESPECIFICA;
    }
}
