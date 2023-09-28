package ir.milad.DocVisitApp.extensions.java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.IndexedConsumer;
import manifold.ext.rt.api.This;

import java.util.stream.Stream;

@Extension
public class StreamExt {

    public static <T> void foreachIndexed(@This Stream<T> stream, IndexedConsumer<T> action) {
        stream.toList().forEachIndexed(action);
    }
}
