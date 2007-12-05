// Cookie.java

package ed.net.httpserver;

public class Cookie {
    public Cookie( String name , String value ){
        _name = name;
        _value = value;
    }

    final String _name;
    final String _value;
}
