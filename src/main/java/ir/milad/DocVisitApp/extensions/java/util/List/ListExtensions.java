package ir.milad.DocVisitApp.extensions.java.util.List;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.util.List;

@Extension
public class ListExtensions {

    public static <E> boolean isNotEmpty(@This List<E> thiz) {
        return !thiz.isEmpty();
    }
}
