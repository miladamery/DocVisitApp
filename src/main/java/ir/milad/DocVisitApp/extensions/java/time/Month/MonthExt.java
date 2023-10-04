package ir.milad.DocVisitApp.extensions.java.time.Month;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.Month;

@Extension
public class MonthExt {

    public static FrenchMonth toFrench(@This Month thiz) {
        return switch (thiz) {
            case JANUARY -> FrenchMonth.JANVIER;
            case FEBRUARY -> FrenchMonth.FÉVRIER;
            case MARCH -> FrenchMonth.MARS;
            case APRIL -> FrenchMonth.AVRIL;
            case MAY -> FrenchMonth.MAI;
            case JUNE -> FrenchMonth.JUIN;
            case JULY -> FrenchMonth.JUILLET;
            case AUGUST -> FrenchMonth.AOÛT;
            case SEPTEMBER -> FrenchMonth.SEPTEMBRE;
            case OCTOBER -> FrenchMonth.OCTOBRE;
            case NOVEMBER -> FrenchMonth.NOVEMBER;
            case DECEMBER -> FrenchMonth.DÉCEMBRE;
        };
    }
}
