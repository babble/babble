// JSLibrary.java

package ed.js;

public interface JSLibrary {
    public Object getFromPath( String path , boolean evalToFunction );
    public java.io.File getRoot();
}
