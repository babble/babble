// Convert.java

package ed.js.engine;

import java.io.*;
import java.util.*;

import org.mozilla.javascript.*;

import ed.js.*;
import ed.io.*;
import ed.util.*;

public class Convert {

    static boolean D = true;

    public Convert(){
        _className = "anon_" + _id;
    }

    void add( ScriptOrFnNode sn ){
        if ( _it != null )
            throw new RuntimeException( "too late" );

        Node n = sn.getFirstChild();
        while ( n != null ){
            _add( n , sn );
            _append( ";" );
            _append( "\n" );
            n = n.getNext();
        }
    }

    private void _add( Node n ){
        _add( n , null );
    }
    
    private void _add( Node n , ScriptOrFnNode sn ){
        if ( D ){
            if ( sn == null )
                System.out.println( n );
            else
                System.out.println( n.toStringTree( sn ) );
        }

        switch ( n.getType() ){
        case Token.EXPR_RESULT:
            _assertOne( n );
            _add( n.getFirstChild() );
            break;
        case Token.CALL:
            _addCall( n );
            break;
        case Token.NUMBER:
            _append( String.valueOf( n.getDouble() ) );
            break;
        case Token.VAR:
            _addVar( n );
            break;
        case Token.NAME:
            _append( "scope.get( \"" + n.getString() + "\" )" );
            break;
        case Token.SETNAME:
            _addSet( n );
            break;
        default:
            throw new RuntimeException( "can't handle : " + n.getType() + ":" + Token.name( n.getType() ) + ":" + n.getClass().getName() );
        }

    }

    private void _addSet( Node n ){
        _assertType( n , Token.SETNAME );
        Node name = n.getFirstChild();
        _setVar( name.getString() , name.getNext() );
    }
    
    private void _addVar( Node n ){
        _assertType( n , Token.VAR );
        _assertOne( n );
        
        Node name = n.getFirstChild();
        _assertOne( name );
        _setVar( name.getString() , name.getFirstChild() );
    }
    
    private void _addCall( Node n ){
        _assertType( n , Token.CALL );
        Node name = n.getFirstChild();
        String fName = name.getString();
        
        if ( fName.startsWith( "SYSOUT" ) )
            _append( "SYSOUT(" );
        else
            _append( "scope.getFunction(\"" + name.getString() + "\").call( " );
        
        Node param = name.getNext();
        while ( param != null ){
            _add( param );
            param = param.getNext();
            if ( param != null ){
                _append( " , " );
            }
        }

        _append( " ) " );
    }

    private void _setVar( String name , Node val ){
        _append( "scope.put( \"" + name + "\" , " );
        _add( val );
        _append( " , false  ) " );
    }
    
    private void _assertOne( Node n ){
        if ( n.getFirstChild() == null )
            throw new RuntimeException( "no child" );
        if ( n.getFirstChild().getNext() != null )
            throw new RuntimeException( "more than 1 child" );
    }

    private void _assertType( Node n , int type ){
        if ( type != n.getType() )
            throw new RuntimeException( "wrong type" );
    }

    private void _append( String s ){
        _mainJavaCode.append( s );
    }

    public String getClassName(){
        return _className;
    }
    
    String getClassString(){
        StringBuilder buf = new StringBuilder();
        
        buf.append( "package " + _package + ";\n" );
        
        buf.append( "import ed.js.*;\n" );

        buf.append( "public class " ).append( _className ).append( " extends JSFunction {\n" );
        
        buf.append( "\tpublic void call(){\n" );
        
        buf.append( "ed.js.engine.Scope scope = ed.js.engine.Scope.GLOBAL;" );
        
        buf.append( _mainJavaCode );
        
        buf.append( "\t}\n\n" );
        
        buf.append( "\n}\n\n" );
        return buf.toString();
    }

    JSFunction get(){
        if ( _it != null )
            return _it;
        
        try {
            Class c = CompileUtil.compile( _package , getClassName() , getClassString() );
            JSFunction it = (JSFunction)c.newInstance();
            _it = it;
            return _it;
        }
        catch ( Exception e ){
            throw new RuntimeException( e );
        }
    }
    
    final String _className;
    final String _package = "ed.js.gen";
    final int _id = ID++;    
    
    private StringBuilder _mainJavaCode = new StringBuilder();
    private JSFunction _it;
    
    private static int ID = 1;
    
    public static void main( String args[] )
        throws Exception {
        
        CompilerEnvirons ce = new CompilerEnvirons();
        Parser p = new Parser( ce , ce.getErrorReporter() );
        
        String raw = StreamUtil.readFully( new java.io.FileInputStream( args[0] ) );
        ScriptOrFnNode ss = p.parse( raw , args[0] , 0 );

        Convert c = new Convert();
        c.add( ss );

        c.get().call();
    }
    
}
