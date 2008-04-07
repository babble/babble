package ed.util;

public class HtmlEscape {
    static public String escape( String s ) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\\\"", "&quot;");
        s = s.replaceAll("\\\'", "&apos;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }
};
