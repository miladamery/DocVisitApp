package ir.milad.DocVisitApp.extensions.java.time.DayOfWeek;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.DayOfWeek;

@Extension
public class DayOfWeekExt {

    public static FrenchDayOfWeek toFrench(@This DayOfWeek thiz) {
        return switch (thiz) {
            case SUNDAY -> FrenchDayOfWeek.DIMANCHE;
            case MONDAY -> FrenchDayOfWeek.LUNDI;
            case TUESDAY -> FrenchDayOfWeek.MARDI;
            case WEDNESDAY -> FrenchDayOfWeek.MERCREDI;
            case THURSDAY -> FrenchDayOfWeek.JEUDI;
            case FRIDAY -> FrenchDayOfWeek.VENDREDI;
            case SATURDAY -> FrenchDayOfWeek.SAMEDI;
        };
    }
}
