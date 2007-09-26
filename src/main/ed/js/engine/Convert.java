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

        add( p.parse( raw , f.toString() , 0 ) );
    }

    public Convert( ScriptOrFnNode sn ){
        _className = "anon_" + _id;
        add( sn );
    }

    private void add( ScriptOrFnNode sn ){

        
        if ( _it != null )
            throw new RuntimeException( "too late" );
        
        NodeTransformer nf = new NodeTransformer();
        nf.transform( sn );
        
        if ( true ){
            _print( sn , 0 );
            System.exit(0);
        }

        for ( int i=0; i<sn.getFunctionCount(); i++ ){

            FunctionNode fn = sn.getFunctionNode( i );

            String name = fn.getFunctionName();
            if ( name.length() == 0 )
                name = "tempFunc_" + _id + "_" + i;
            
            _functionIdToName.put( i , name );

            if ( D ){
                System.out.println( "***************" );
                System.out.println( i + " : " +  name );
            }

            _setVar( name , fn );
            _append( "\nscope.getFunction( \"" + name + "\" ).setName( \"" + name + "\" );\n\n" , fn );

        }
        if ( D ) System.out.println( "***************" );

        Node n = sn.getFirstChild();
        while ( n != null ){
            _add( n , sn );
            _append( ";\n" , n );
            n = n.getNext();
        }

        _append( "return null;" , sn );
    }

    private void _add( Node n ){
        _add( n , null );
    }
    
    private void _add( Node n , ScriptOrFnNode sn ){
        
        if ( false ){
            
            System.out.println( "------  has base : " + ( sn != null ) );
            
            if ( sn == null ){
                System.out.println( Token.name( n.getType() ) );
            }
            else {
                System.out.println( n.toStringTree( sn ) );
            }

            _printTree( n , 0 );
        
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
            _append( String.valueOf( n.getDouble() ) , n );
            break;
        case Token.STRING:
            _append( "\"" + n.getString() + "\"" , n );
            break;
        case Token.VAR:
            _addVar( n );
            break;
        case Token.GETVAR:
            _append( n.getString() , n );
            break;
        case Token.SETVAR:
            String foo = n.getFirstChild().getString();
            if ( ! _localSymbols.peek().contains( foo ) ){
                _localSymbols.peek().add( foo );
                _append( "Object " , n );
            }
            _append( n.getFirstChild().getString() + " = " , n );
            _add( n.getFirstChild().getNext() );
            _append( ";" , n );
            break;
        case Token.NAME:
            _append( "scope.get( \"" + n.getString() + "\" )" , n );
            break;
        case Token.SETNAME:
            _addSet( n );
            break;
        case Token.FUNCTION:
            _addFunction( n );
            break;
        case Token.BLOCK:
            _addBlock( n );
            break;
        case Token.EXPR_VOID:
            _assertOne( n );
            _add( n.getFirstChild() );
            _append( ";" , n );
            break;
        case Token.RETURN:
            _assertOne( n );
            _append( "return " , n );
            _add( n.getFirstChild() );
            _append( ";" , n );
            break;
        case Token.ADD:
            _append( "JS_add( " , n );
            _add( n.getFirstChild() );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() );
            _append( " ) " , n );
            break;
        default:
            _printTree( n , 0 );
            throw new RuntimeException( "can't handle : " + n.getType() + ":" + Token.name( n.getType() ) + ":" + n.getClass().getName() );
        }

    }
    
    private void _addFunction( Node n ){
        if ( ! ( n instanceof FunctionNode ) ){
            _append( getFunc( n ) , n );
            return;
        }

        FunctionNode fn = (FunctionNode)n;
        _assertOne( n );

        Set<String> mySymbols = new HashSet<String>();
        _localSymbols.push( mySymbols );

        _append( "new JSFunction(" + fn.getParamCount() + "){ \n" , n );
        String callLine = "public Object call(";
        for ( int i=0; i<fn.getParamCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            mySymbols.add( foo );
            if ( i > 0 )
                callLine += " , ";
            callLine += " Object " + foo + " ";
        }
        callLine += "){\n" ;
        
        _append( callLine , n );
        
        _add( n.getFirstChild() );
        _append( "}\n" , n );
        _append( "}\n" , n );

        _localSymbols.pop();
    }
    
    private void _addBlock( Node n ){
        _assertType( n , Token.BLOCK );

        if ( n.getFirstChild() == null )
            return;
        
        // this is weird.  look at bracing0.js
        if ( n.getFirstChild().getNext() == null && 
             n.getFirstChild().getType() == Token.EXPR_VOID &&
             n.getFirstChild().getFirstChild().getNext() == null ){
            _add( n.getFirstChild() );
            return;
        }
        
        _append( "{" , n );
        Node child = n.getFirstChild();
        while ( child != null ){
            _add( child );
            child = child.getNext();
        }
        _append( "}" , n );
        
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

        String f = getFunc( name );
        _append( f + ".call( " , n );

        Node param = name.getNext();
        while ( param != null ){
            _add( param );
            param = param.getNext();
            if ( param != null ){
                _append( " , " , param );
            }
        }

        _append( " ) " , n );
    }

    private void _setVar( String name , Node val ){
        _append( "scope.put( \"" + name + "\" , " , val);
        _add( val );
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

    private String getFunc( Node n ){
        if ( n.getClass().getName().indexOf( "StringNode" ) < 0 ){
            _append( "((JSFunction)" , n);
            _add( n );
            _append( ")" , n );
            return "";
        }

        String name = n.getString();
        if ( name == null || name.length() == 0 ){
            int id = n.getIntProp( Node.FUNCTION_PROP , -1 );
            if ( id == -1 )
                throw new RuntimeException( "no name or id for this thing" );
            name = _functionIdToName.get( id );
            if ( name == null || name.length() == 0 )
                throw new RuntimeException( "no name for this id " );
        }
        
        if ( _localSymbols.size() > 0 && _localSymbols.peek().contains( name ) )
            return "((JSFunction)" + name + ")";
        
        return "scope.getFunction( \"" + name + "\" )";
    }

    public String getClassName(){
        return _className;
    }

    private static boolean _hasString( Node n ){
        return n.getClass().getName().indexOf( "StringNode" ) >= 0;
    }

    private static void _print( ScriptOrFnNode sn , int indent ){
        for ( int i=0; i<sn.getFunctionCount(); i++ ){
            FunctionNode fn = sn.getFunctionNode( i );
            _print( fn , indent );
        }
        _printTree( sn , indent );
    }

    private static void _printTree( Node n , int indent ){
        if ( n == null )
            return;

        for ( int i=0; i<indent; i++ )
            System.out.print( "  " );

        System.out.print( Token.name( n.getType() ) + " [" + n.getClass().getName().replaceAll( "org.mozilla.javascript." , "" ) + "]"  );

        if ( n instanceof FunctionNode )
            System.out.print( " " + ((FunctionNode)n).getFunctionName() );


        if ( n.getType() == Token.FUNCTION ){
            int id = n.getIntProp( Node.FUNCTION_PROP , -17 );
            if ( id != -17 )
                System.out.print( " functionId=" + id );
        }
        
        if ( _hasString( n ) )
            System.out.print( " [" + n.getString() + "]" );
        
        if ( n.getType() == Token.NUMBER )
            System.out.print( " NUMBER:" + n.getDouble() );
        
        //System.out.print( " " + n.toString().replaceAll( "^\\w+ " , "" ) );

        System.out.println();
        
        _printTree( n.getFirstChild() , indent + 1 );
        _printTree( n.getNext() , indent );
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

    JSFunction get(){
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

    private int _currentLineNumber = 0;

    private final Stack<Set<String>> _localSymbols = new Stack<Set<String>>();
    
    private final Map<Integer,String> _functionIdToName = new HashMap<Integer,String>();
    private final Map<Integer,List<Node>> _javaCodeToLines = new TreeMap<Integer,List<Node>>();
    private final StringBuilder _mainJavaCode = new StringBuilder();
    
    private JSFunction _it;
    
    private static int ID = 1;
    
    public static void main( String args[] )
        throws Exception {
        
        for ( String s : args ){
            System.out.println( "-----" );
            System.out.println( s );
            
            Convert c = new Convert( new File( s ) );
            c.get().call();
        }
    }
    
}
