package ed.util;

/** @expose */
public class HtmlEscape {

    /** Escapes special HTML characters in a string.  Replaces &amp;, &quot;, &apos;, &lt;, and &gt;.
     * @param s string to escape
     * @return escaped string
     */
    static public String escape( String s ) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\\\"", "&quot;");
        s = s.replaceAll("\\\'", "&apos;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }
};
