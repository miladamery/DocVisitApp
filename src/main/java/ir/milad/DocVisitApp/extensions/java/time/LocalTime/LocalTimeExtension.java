package ir.milad.DocVisitApp.extensions.java.time.LocalTime;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.time.Duration;
import java.time.LocalTime;

@Extension
public class LocalTimeExtension {

    public static Long minus(@This LocalTime thiz, LocalTime dest) {
        return Duration.between(thiz, dest).toMinutes();
    }

    @Extension
    public static LocalTime nowHM() {
        return LocalTime.now().withSecond(0).withNano(0);
    }
}
