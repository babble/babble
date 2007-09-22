// JSFunction.java

package ed.js;

import java.io.*;

public abstract class JSFunction extends JSFunctionBase {

    public JSFunction( int num ){
        this( null , num );
    }

    public JSFunction( String name , int num ){
        super( num );
        _name = name;
    }

    protected void SYSOUT( Number n ){
        _sysout.println( n );
    }

    protected void SYSOUT( Object ... os ){
        for ( Object o : os )
            _sysout.print( o + " " );
        _sysout.println();
    }
    
    public void setName( String name ){
        _name = name;
    }

    public String toString(){
        return "JSFunction : " + _name;
    }

    public void setSysOut( PrintStream out ){
        _sysout = out;
    }

    String _name = "NO NAME SET";
    PrintStream _sysout = System.out;
}
