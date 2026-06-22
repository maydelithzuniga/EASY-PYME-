package com.easymype.backend.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public final class PeriodoUtils {
    private PeriodoUtils() {}

    public static TipoPeriodo detectarTipoPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        LocalDate desde = inicio.toLocalDate();
        LocalDate hasta = fin.toLocalDate();

        boolean esMensual = desde.equals(desde.withDayOfMonth(1))
                && hasta.equals(desde.withDayOfMonth(desde.lengthOfMonth()))
                && desde.getMonth() == hasta.getMonth()
                && desde.getYear() == hasta.getYear();

        if (esMensual) return TipoPeriodo.MENSUAL;

        int mesInicioTrimestre = ((desde.getMonthValue() - 1) / 3) * 3 + 1;
        LocalDate inicioTrimestreEsperado = LocalDate.of(desde.getYear(), mesInicioTrimestre, 1);
        LocalDate finTrimestreEsperado = inicioTrimestreEsperado.plusMonths(3).minusDays(1);

        boolean esTrimestral = desde.equals(inicioTrimestreEsperado) && hasta.equals(finTrimestreEsperado);
        return esTrimestral ? TipoPeriodo.TRIMESTRAL : TipoPeriodo.PERSONALIZADO;
    }

    public static LocalDateTime[] calcularPeriodoAnterior(LocalDateTime inicio, LocalDateTime fin, TipoPeriodo tipo) {
        LocalDate desde = inicio.toLocalDate();
        LocalDate hasta = fin.toLocalDate();

        return switch (tipo) {
            case MENSUAL -> {
                YearMonth mesAnterior = YearMonth.from(desde).minusMonths(1);
                yield new LocalDateTime[]{
                        mesAnterior.atDay(1).atStartOfDay(),
                        mesAnterior.atEndOfMonth().atTime(23, 59, 59)
                };
            }
            case TRIMESTRAL -> {
                LocalDate inicioAnterior = desde.minusMonths(3);
                LocalDate finAnterior = inicioAnterior.plusMonths(3).minusDays(1);
                yield new LocalDateTime[]{
                        inicioAnterior.atStartOfDay(),
                        finAnterior.atTime(23, 59, 59)
                };
            }
            case PERSONALIZADO -> {
                long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
                yield new LocalDateTime[]{
                        desde.minusDays(dias).atStartOfDay(),
                        desde.minusDays(1).atTime(23, 59, 59)
                };
            }
        };
    }

    public static BigDecimal calcularVariacionPorcentual(BigDecimal actual, BigDecimal anterior) {
        actual = actual != null ? actual : BigDecimal.ZERO;
        anterior = anterior != null ? anterior : BigDecimal.ZERO;

        if (anterior.compareTo(BigDecimal.ZERO) == 0) {
            return actual.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(100);
        }

        return actual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
