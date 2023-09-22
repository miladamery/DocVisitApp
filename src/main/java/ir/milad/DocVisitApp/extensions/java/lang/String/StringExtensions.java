package ir.milad.DocVisitApp.extensions.java.lang.String;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
public class StringExtensions {

    public static String setCharBeforeAndAfter(@This String str, char ch, int index) {
        if (index < 0 || index >= str.length())
            throw new IndexOutOfBoundsException("invalid index");

        var stringBuilder = new StringBuilder(str);
        stringBuilder.insert(index, ch);
        stringBuilder.insert(index + 2, ch);
        return stringBuilder.toString();
    }
}
