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

    public Convert( File f )
        throws IOException {
        
        _className = f.toString().replaceAll(".*/(.*?)","").replaceAll( "[^\\w]+" , "_" );

        String raw = StreamUtil.readFully( f );
        
        CompilerEnvirons ce = new CompilerEnvirons();
        Parser p = new Parser( ce , ce.getErrorReporter() );

        init( p.parse( raw , f.toString() , 0 ) );
    }

    public Convert( ScriptOrFnNode sn ){
        _className = "anon_" + _id;
        init( sn );
    }

    private void init( ScriptOrFnNode sn ){
        
        if ( _it != null )
            throw new RuntimeException( "too late" );
        
        NodeTransformer nf = new NodeTransformer();
        nf.transform( sn );
        
        if ( false ){
            Debug.print( sn , 0 );
            System.exit(0);
        }

        State state = new State();

        _addFunctionNodes( sn , state );
        
        if ( D ) System.out.println( "***************" );

        Node n = sn.getFirstChild();
        while ( n != null ){
            _add( n , sn , state );
            _append( ";\n" , n );
            n = n.getNext();
        }

        _append( "return null;" , sn );
    }

    private void _add( Node n , State s ){
        _add( n , null , s );
    }
    
    private void _add( Node n , ScriptOrFnNode sn , State state ){
        
        switch ( n.getType() ){
        case Token.EXPR_RESULT:
            _assertOne( n );
            _add( n.getFirstChild() , state );
            break;
        case Token.CALL:
            _addCall( n , state );
            break;
        case Token.NUMBER:
            _append( String.valueOf( n.getDouble() ) , n );
            break;
        case Token.STRING:
            _append( "\"" + n.getString() + "\"" , n );
            break;
        case Token.VAR:
            _addVar( n , state );
            break;
        case Token.GETVAR:
            if ( state.useLocalVariable( n.getString() ) ){
                _append( n.getString() , n );
                break;
            }
        case Token.NAME:
            _append( "scope.get( \"" + n.getString() + "\" )" , n );
            break;
        case Token.SETVAR:
            final String foo = n.getFirstChild().getString();
            if ( state.useLocalVariable( foo ) ){
                if ( state.addSymbol( foo ) )
                    _append( "Object " , n );
                _append( foo + " = " , n );
                _add( n.getFirstChild().getNext() , state );
                _append( ";" , n );
            }
            else {
                _setVar( foo , 
                         n.getFirstChild().getNext() ,
                         state );
            }
            break;

        case Token.SETNAME:
            _addSet( n , state );
            break;
        case Token.FUNCTION:
            _addFunction( n , state );
            break;
        case Token.BLOCK:
            _addBlock( n , state );
            break;
        case Token.EXPR_VOID:
            _assertOne( n );
            _add( n.getFirstChild() , state );
            _append( ";" , n );
            break;
        case Token.RETURN:
            _assertOne( n );
            _append( "return " , n );
            _add( n.getFirstChild() , state );
            _append( ";" , n );
            break;
        case Token.ADD:
            _append( "JS_add( " , n );
            _add( n.getFirstChild() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) " , n );
            break;
        default:
            Debug.printTree( n , 0 );
            throw new RuntimeException( "can't handle : " + n.getType() + ":" + Token.name( n.getType() ) + ":" + n.getClass().getName() );
        }

    }
    
    private void _addFunctionNodes( ScriptOrFnNode sn , State state ){
        for ( int i=0; i<sn.getFunctionCount(); i++ ){
            
            FunctionNode fn = sn.getFunctionNode( i );

            String name = fn.getFunctionName();
            if ( name.length() == 0 )
                name = "tempFunc_" + _id + "_" + i;
            
            state._functionIdToName.put( i , name );
            
            if ( D ){
                System.out.println( "***************" );
                System.out.println( i + " : " +  name );
            }

            _setVar( name , fn , state );
            _append( "\nscope.getFunction( \"" + name + "\" ).setName( \"" + name + "\" );\n\n" , fn );

        }
    }
    
    private void _addFunction( Node n , State state ){
        if ( ! ( n instanceof FunctionNode ) ){
            _append( getFunc( n , state ) , n );
            return;
        }

        FunctionNode fn = (FunctionNode)n;
        _assertOne( n );

        state = state.child();
        
        _append( "new JSFunction(" + fn.getParamCount() + "){ \n" , n );
        
        String callLine = "public Object call(";
        String varSetup = "";
        
        for ( int i=0; i<fn.getParamCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            state.addSymbol( foo );
            if ( i > 0 )
                callLine += " , ";
            callLine += " Object " + foo;
            if ( ! state.useLocalVariable( foo ) ){
                callLine += "INNNNN";
                varSetup += " \nscope.put(\"" + foo + "\"," + foo + "INNNNN , true  );\n ";
            }
            callLine += " ";
        }
        callLine += "){\n" ;
        
        _append( callLine + varSetup , n );
        
        _addFunctionNodes( fn , state );

        _add( n.getFirstChild() , state );
        _append( "}\n" , n );
        _append( "}\n" , n );

    }
    
    private void _addBlock( Node n , State state ){
        _assertType( n , Token.BLOCK );

        if ( n.getFirstChild() == null )
            return;
        
        // this is weird.  look at bracing0.js
        if ( n.getFirstChild().getNext() == null && 
             n.getFirstChild().getType() == Token.EXPR_VOID &&
             n.getFirstChild().getFirstChild().getNext() == null ){
            _add( n.getFirstChild() , state );
            return;
        }
        
        _append( "{" , n );
        Node child = n.getFirstChild();
        while ( child != null ){
            _add( child , state );
            child = child.getNext();
        }
        _append( "}" , n );
        
    }
    
    private void _addSet( Node n , State state ){
        _assertType( n , Token.SETNAME );
        Node name = n.getFirstChild();
        _setVar( name.getString() , name.getNext() , state );
    }
    
    private void _addVar( Node n , State state ){
        _assertType( n , Token.VAR );
        _assertOne( n );
        
        Node name = n.getFirstChild();
        _assertOne( name );
        _setVar( name.getString() , name.getFirstChild() , state );
    }
    
    private void _addCall( Node n , State state ){
        _assertType( n , Token.CALL );
        Node name = n.getFirstChild();

        String f = getFunc( name , state );
        _append( f + ".call( " , n );

        Node param = name.getNext();
        while ( param != null ){
            _add( param , state );
            param = param.getNext();
            if ( param != null ){
                _append( " , " , param );
            }
        }

        _append( " ) " , n );
    }

    private void _setVar( String name , Node val , State state ){
        _append( "scope.put( \"" + name + "\" , " , val);
        _add( val , state );
        _append( " , false  ); " , val );
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

    private void _append( String s , Node n ){
        _mainJavaCode.append( s );
        
        int numLines = 0;
        for ( int i=0; i<s.length(); i++ )
            if ( s.charAt( i ) == '\n' )
                numLines++;
        
        final int start = _currentLineNumber;
        final int end = _currentLineNumber + numLines;
        
        for ( int i=start; i<end; i++ ){
            List<Node> l = _javaCodeToLines.get( i );
            if ( l == null ){
                l = new ArrayList<Node>();
                _javaCodeToLines.put( i , l );
            }
            l.add( n );
        }

        _currentLineNumber = end;
    }

    private String getFunc( Node n , State state ){
        if ( n.getClass().getName().indexOf( "StringNode" ) < 0 ){
            _append( "((JSFunction)" , n);
            _add( n , state );
            _append( ")" , n );
            return "";
        }

        String name = n.getString();
        if ( name == null || name.length() == 0 ){
            int id = n.getIntProp( Node.FUNCTION_PROP , -1 );
            if ( id == -1 )
                throw new RuntimeException( "no name or id for this thing" );
            name = state._functionIdToName.get( id );
            if ( name == null || name.length() == 0 )
                throw new RuntimeException( "no name for this id " );
        }
        
        if ( state.hasSymbol( name ) )
            return "((JSFunction)" + name + ")";
        
        return "scope.getFunction( \"" + name + "\" )";
    }

    public String getClassName(){
        return _className;
    }

    String getClassString(){
        StringBuilder buf = new StringBuilder();
        
        buf.append( "package " + _package + ";\n" );
        
        buf.append( "import ed.js.*;\n" );

        buf.append( "public class " ).append( _className ).append( " extends JSFunction {\n" );
        
        buf.append( "\tpublic " + _className + "(){\n\t\tsuper(0);\n\t}\n\n" );

        buf.append( "\tpublic Object call(){\n" );
        
        buf.append( "final ed.js.engine.Scope scope = getScope();\n\n" );
        
        buf.append( _mainJavaCode );
        
        buf.append( "\n\n\t}\n\n" );
        
        buf.append( "\n}\n\n" );
        return buf.toString();
    }

    public JSFunction get(){
        if ( _it != null )
            return _it;
        
        try {
            Class c = CompileUtil.compile( _package , getClassName() , getClassString() );
            JSFunction it = (JSFunction)c.newInstance();
            _it = it;
            return _it;
        }
        catch ( RuntimeException re ){
            throw re;
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeException( e );
        }
    }
    
    final String _className;
    final String _package = "ed.js.gen";
    final int _id = ID++;    

    // these 3 variables should only be use by _append
    private int _currentLineNumber = 0;    
    private final Map<Integer,List<Node>> _javaCodeToLines = new TreeMap<Integer,List<Node>>();
    private final StringBuilder _mainJavaCode = new StringBuilder();
    
    private JSFunction _it;
    
    private static int ID = 1;
    
}
