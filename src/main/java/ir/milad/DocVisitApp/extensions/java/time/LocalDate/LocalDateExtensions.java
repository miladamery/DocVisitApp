package ir.milad.DocVisitApp.extensions.java.time.LocalDate;

import manifold.ext.rt.api.ComparableUsing;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.LocalDate;

@Extension
public class LocalDateExtensions {

    /**
     * Manifold Operator overloading capability for LocalDateTime.
     * Activates >, <, >=, <=, ==, != for LocalDateTime
     *
     * @param thiz
     * @param that
     * @return
     */
    public static boolean compareToUsing(@This LocalDate thiz, LocalDate that, ComparableUsing.Operator op) {
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
