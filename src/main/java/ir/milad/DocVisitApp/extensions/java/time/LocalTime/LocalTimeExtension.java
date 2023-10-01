package ir.milad.DocVisitApp.extensions.java.time.LocalTime;

import manifold.ext.rt.api.ComparableUsing;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

@Extension
public class LocalTimeExtension {

    public static Long minus(@This LocalTime thiz, LocalTime dest) {
        return Duration.between(thiz, dest).toMinutes();
    }

    @Extension
    public static LocalTime nowHM() {
        return LocalTime.now().withSecond(0).withNano(0);
    }

    public static Long diffWithNowInMinutes(@This LocalTime thiz) {
        return Duration.between(thiz, LocalTime.nowHM()).toMinutes();
    }

    /**
     * Manifold Operator overloading capability for LocalDateTime.
     * Activates >, <, >=, <=, ==, != for LocalDateTime
     *
     * @param thiz
     * @param that
     * @return
     */
    public static boolean compareToUsing(@This LocalTime thiz, LocalTime that, ComparableUsing.Operator op) {
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
                return Objects.equals(thiz, that);
            case NE:
                return !Objects.equals(thiz, that);

            default:
                throw new IllegalStateException();
        }
    }
}
