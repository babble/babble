package ed.js;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

public class JSCookieJar {
    
    public JSCookieJar() {
        this._cookies = new ArrayList<Cookie>();
    }
    public void addCookie(URL source, Cookie cookie) {
        _cookies.add( cookie );
    }
    public List<Cookie> getCookies(URL requestingUrl) {
        return _cookies;
    }
    
    private final List<Cookie> _cookies;
}
