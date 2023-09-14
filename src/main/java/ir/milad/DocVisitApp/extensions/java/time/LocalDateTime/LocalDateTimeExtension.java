package ir.milad.DocVisitApp.extensions.java.time.LocalDateTime;

import manifold.ext.rt.api.ComparableUsing;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Extension
public class LocalDateTimeExtension {
    @Extension
    public static LocalDateTime of(LocalTime time) {
        return LocalDateTime.of(LocalDate.now(), time);
    }

    public static Long timeIntervalInMinutes(@This LocalDateTime thiz, LocalTime dest) {
        return Duration.between(thiz.toLocalTime(), dest).toMinutes();
    }

    @Extension
    public static LocalDateTime nowHM() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.nowHM());
    }

    public static Long minus(@This LocalDateTime thiz, LocalDateTime other) {
        return Duration.between(thiz, other).toMinutes();
    }

    /**
     * Manifold Operator overloading capability for LocalDateTime.
     * Activates >, <, >=, <=, ==, != for LocalDateTime
     *
     * @param thiz
     * @param that
     * @return
     */
    public static boolean compareToUsing(@This LocalDateTime thiz, LocalDateTime that, ComparableUsing.Operator op) {
        switch (op) {
            case LT:
                return thiz.isBefore(that);
            case LE:
                return !thiz.isAfter(that);
            case GT:
                return thiz.isAfter(that);
            case GE:
                return !thiz.isBefore(that);
            case EQ:
                return thiz.isEqual(that);
            case NE:
                return !thiz.isEqual(that);

            default:
                throw new IllegalStateException();
        }
    }
}