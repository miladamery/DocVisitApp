package ir.milad.DocVisitApp.extensions.java.time.LocalDateTime;

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

  public static boolean isAfterOrEqualTo(@This LocalDateTime thiz, LocalDateTime other) {
    return thiz.isAfter(other) || thiz.isEqual(other);
  }

  public static Long timeIntervalInMinutes(@This LocalDateTime thiz, LocalTime dest) {
    return Duration.between(thiz.toLocalTime(), dest).toMinutes();
  }
}