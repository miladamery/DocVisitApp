package ir.milad.DocVisitApp.extensions.java.time.LocalTime;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.Duration;
import java.time.LocalTime;

@Extension
public class LocalTimeExtension {

    public static Long timeIntervalInMinutes(@This LocalTime thiz, LocalTime dest) {
        return Duration.between(thiz, dest).toMinutes();
    }
}
