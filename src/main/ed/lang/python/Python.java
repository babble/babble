// Python.java

package ed.lang.python;

import java.io.*;

import org.python.core.*;

import ed.js.*;

public class Python {

    static {
        PySystemState.initialize();
    }
    
    public static PyCode compile( File f )
        throws IOException {
        return (PyCode)(Py.compile( new FileInputStream( f ) , f.toString() , "exec" ));
    }


    static Object toJS( PyObject p ){
        if ( p == null )
            return null;
 
        if ( p instanceof PyInteger )
            return ((PyInteger)p).getValue();

        if ( p instanceof PyFloat )
            return ((PyFloat)p).getValue();

        if ( p instanceof PyString )
            return p.toString();

        throw new RuntimeException( "can't convert [" + p.getClass().getName() + "] from py to js" );       
    }
    
    static PyObject toPython( Object o ){
        
        if ( o == null )
            return null;

        if ( o instanceof Integer )
            return new PyInteger( ((Integer)o).intValue() );
        
        if ( o instanceof Number )
            return new PyFloat( ((Number)o).floatValue() );
        
        if ( o instanceof String ||
             o instanceof JSString )
            return new PyString( o.toString() );
        
        // fill in here

        // these should be at the bottom
        if ( o instanceof JSFunction )
            return new PYJSFunctionWrapper( (JSFunction)o );

        if ( o instanceof JSObject )
            return new PYJSObjectWrapper( (JSObject)o );
        
        throw new RuntimeException( "can't convert [" + o.getClass().getName() + "] from js to py" );
    }
}
