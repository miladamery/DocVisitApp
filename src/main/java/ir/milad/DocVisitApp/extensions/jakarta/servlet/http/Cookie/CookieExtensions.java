package ir.milad.DocVisitApp.extensions.jakarta.servlet.http.Cookie;

import jakarta.servlet.http.Cookie;
import manifold.ext.rt.api.Extension;

@Extension
public class CookieExtensions {

    @Extension
    public static Cookie of(String name, String value, int expiry) {
        var cookie = new Cookie(name, value);
        cookie.setMaxAge(expiry);
        cookie.setPath("/");
        return cookie;
    }
}
