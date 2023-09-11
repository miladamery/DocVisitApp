package ir.milad.DocVisitApp.extensions.java.util.LinkedList;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.util.LinkedList;

@Extension
public class LinkedListExtensions {

    public static boolean isNotEmpty(@This LinkedList thiz) {
        return !thiz.isEmpty();
    }
}
