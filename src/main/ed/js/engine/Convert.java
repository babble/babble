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
        
        if ( D ){
            Debug.print( sn , 0 );
        }

        State state = new State();

        _addFunctionNodes( sn , state );
        
        if ( D ) System.out.println( "***************" );

        Node n = sn.getFirstChild();
        while ( n != null ){
            if ( n.getType() != Token.FUNCTION ){
                _add( n , sn , state );
                _append( "\n" , n );
            }
            n = n.getNext();
        }

        _append( "return null;" , sn );
    }

    private void _add( Node n , State s ){
        _add( n , null , s );
    }
    
    private void _add( Node n , ScriptOrFnNode sn , State state ){
        
        switch ( n.getType() ){

        case Token.USE_STACK:
            _append( "__tempObject.get( " + state._tempOpNames.pop() + " ) "  , n );
            break;

        case Token.SETPROP_OP:
        case Token.SETELEM_OP:
            _append( "\n { \n" , n );
            
            _append( "JSObject __tempObject = (JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( ";\n" , n );

            
            String tempName = "__temp" + (int)(Math.random() * 10000);
            state._tempOpNames.push( tempName );
            
            _append( "Object " + tempName + " = " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( ";\n" , n );
            
            _append( " __tempObject.set(" , n );
            _append( tempName , n );
            _append( " , " , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " ); \n" , n );

            _append( " } \n" , n );
            
            break;

        case Token.SETPROP:
        case Token.SETELEM:
            _append( "((JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( ").set( " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " ); " , n );
            break;

        case Token.GETPROP:
        case Token.GETELEM:
            _append( "((JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( ").get( " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " )" , n );
            break;

        case Token.EXPR_RESULT:
            _assertOne( n );
            _add( n.getFirstChild() , state );
            _append( ";\n" , n );
            break;
        case Token.CALL:
            _addCall( n , state );
            break;

        case Token.NUMBER:
            String temp = String.valueOf( n.getDouble() );
            if ( temp.endsWith( ".0" ) )
                temp = temp.substring( 0 , temp.length() - 2 );
            _append( temp , n );
            break;
        case Token.STRING:
            _append( "\"" + n.getString() + "\"" , n );
            break;
        case Token.TRUE:
            _append( " true " , n );
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
                _append( ";\n" , n );
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
            _append( ";\n" , n );
            break;
        case Token.RETURN:
            _append( "return " , n );
            if ( n.getFirstChild() != null ){
                _assertOne( n );
                _add( n.getFirstChild() , state );
            }
            else {
                _append( " null " , n );
            }
            _append( ";\n" , n );
            break;

        case Token.NE:
            _append( " ! " , n );
            
        case Token.MUL:
        case Token.ADD:
        case Token.SUB:
        case Token.EQ:
        case Token.GE:
        case Token.BITOR:
        case Token.RSH:
        case Token.LSH:
        case Token.MOD:
            _append( "JS_" , n );
            _append( _2ThingThings.get( n.getType() ) , n );
            _append( "( " , n );
            _add( n.getFirstChild() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) " , n );
            break;

        case Token.IFNE:
            _addIFNE( n , state );
            break;
            
        case Token.LOOP:
            _addLoop( n , state );
            break;

        case Token.EMPTY:
            if ( n.getFirstChild() != null ){
                Debug.printTree( n , 0 );
                throw new RuntimeException( "not really empty" );
            }
            break;

        case Token.LABEL:
            _append( n.getString() + ":" , n );
            break;
        case Token.BREAK:
            _append( "break " + n.getString() + ";\n" , n );
            break;
        case Token.CONTINUE:
            _append( "continue " + n.getString() + ";\n" , n );
            break;
            
        case Token.WHILE:
            _append( "while( JS_evalToBool( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) ) " , n );
            _add( n.getFirstChild().getNext() , state );
            break;
        case Token.FOR:
            _append( "\n for ( " , n );
            _add( n.getFirstChild() , state );
            _append( "  \n " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ; \n" , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " )\n " , n );
            _add( n.getFirstChild().getNext().getNext().getNext() , state );
            
            break;
            
        case Token.TARGET:
            break;
            
        case Token.NOT:
            _assertOne( n );
            _append( " ! JS_evalToBool( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) " , n );
            break;

        case Token.AND:
            _append( " ( " , n );
            Node c = n.getFirstChild();
            while ( c != null ){
                if ( c != n.getFirstChild() )
                    _append( " && " , n );
                _append( " JS_evalToBool( " , n );
                _add( c , state );
                _append( " ) " , n );
                c = c.getNext();
            }
            _append( " ) " , n );
            break;
        default:
            Debug.printTree( n , 0 );
            throw new RuntimeException( "can't handle : " + n.getType() + ":" + Token.name( n.getType() ) + ":" + n.getClass().getName() + " line no : " + n.getLineno() );
        }
        
    }

    private void _addLoop( Node n , State state ){
        _assertType( n , Token.LOOP );
        
        final Node theLoop = n;
        n = n.getFirstChild();
        
        Node nodes[] = null;
        if ( ( nodes = _matches( n , _while1 ) ) != null ){
            Node main = nodes[1];
            Node predicate = nodes[5];
                
            _append( "while ( JS_evalToBool( " , theLoop );
            _add( predicate.getFirstChild() , state );
            _append( " ) ) " , theLoop );
            _add( main , state );
        }
        else if ( ( nodes = _matches( n , _doWhile1 ) ) != null ){
            Node main = nodes[1];
            Node predicate = nodes[3];
            _assertType( predicate , Token.IFEQ );

            _append( "do  \n " , theLoop );
            _add( main , state );
            _append( " \n while ( JS_evalToBool( " , n );
            _add( predicate.getFirstChild() , state );
            _append( " ) );\n " , n );
        }
        else {
            throw new RuntimeException( "what?" );
        }
    }
    
    private void _addIFNE( Node n , State state ){

        _assertType( n , Token.IFNE );

        final Node.Jump theIf = (Node.Jump)n;
        
        _assertOne( n ); // this is the predicate
        Node ifBlock = n.getNext();
        if ( ifBlock.getFirstChild() != null && 
             ifBlock.getFirstChild().getNext() != null )
            throw new RuntimeException( "bad if" );

        _append( "if ( JS_evalToBool( " , n );
        _add( n.getFirstChild() , state );
        _append( " ) )\n" , n );
        _add( ifBlock , state );
        n = n.getNext().getNext();
        
        if ( n.getType() == Token.TARGET ){
            if ( n.getNext() != null )
                throw new RuntimeException( "something is wrong" );
            return;
        }
        
        _assertType( n , Token.GOTO );
        _assertType( n.getNext() , Token.TARGET );
        if ( theIf.target.hashCode() != n.getNext().hashCode() )
            throw new RuntimeException( "hashes don't match" );
        
        n = n.getNext().getNext();

        _append( " else " , n );
        _add( n , state );
        
        _assertType( n.getNext() , Token.TARGET );
        if ( n.getNext().getNext() != null )
            throw new RuntimeException( "something is wrong" );
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
            _append( "; \n scope.getFunction( \"" + name + "\" ).setName( \"" + name + "\" );\n\n" , fn );

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
        
        _append( "new JSFunction( scope , null , " + fn.getParamCount() + "){ \n" , n );
        
        String callLine = "public Object call( final Scope passedIn ";
        String varSetup = "";
        
        for ( int i=0; i<fn.getParamCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            state.addSymbol( foo );
            callLine += " , ";
            callLine += " Object " + foo;
            if ( ! state.useLocalVariable( foo ) ){
                callLine += "INNNNN";
                varSetup += " \nscope.put(\"" + foo + "\"," + foo + "INNNNN , true  );\n ";
            }
            callLine += " ";
        }
        callLine += "){\n" ;
        
        _append( callLine + " final Scope scope = new Scope( \"temp scope\" , _scope ); " + varSetup , n );
        
        //_append( "final ed.js.engine.Scope oldScope = scope;\n final ed.js.engine.Scope scope = oldScope.child();\n" , n );

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
            
            if ( child.getType() == Token.IFNE )
                break;
            
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
        _append( f + ".call( scope " , n );

        Node param = name.getNext();
        while ( param != null ){
            _append( " , " , param );
            _add( param , state );
            param = param.getNext();
        }

        _append( " ) " , n );
    }

    private void _setVar( String name , Node val , State state ){
        _append( "scope.put( \"" + name + "\" , " , val);
        _add( val , state );
        _append( " , false  ) " , val );
    }
    
    private void _assertOne( Node n ){
        if ( n.getFirstChild() == null ){
            Debug.printTree( n , 0 );
            throw new RuntimeException( "no child" );
        }
        if ( n.getFirstChild().getNext() != null ){
            Debug.printTree( n , 0 );
            throw new RuntimeException( "more than 1 child" );
        }
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
        buf.append( "import ed.js.engine.Scope;\n" );

        buf.append( "public class " ).append( _className ).append( " extends JSFunction {\n" );
        
        buf.append( "\tpublic " + _className + "(){\n\t\tsuper(0);\n\t}\n\n" );

        buf.append( "\tpublic Object call( Scope scope ){\n" );
        
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
    
    private final static Map<Integer,String> _2ThingThings = new HashMap<Integer,String>();
    static {
        _2ThingThings.put( Token.ADD , "add" );
        _2ThingThings.put( Token.MUL , "mul" );
        _2ThingThings.put( Token.MUL , "sub" );
        
        _2ThingThings.put( Token.EQ , "eq" );
        _2ThingThings.put( Token.NE , "eq" );
        _2ThingThings.put( Token.GE , "ge" );

        _2ThingThings.put( Token.BITOR , "bitor" );

        _2ThingThings.put( Token.RSH , "rsh" );
        _2ThingThings.put( Token.LSH , "lsh" );
        _2ThingThings.put( Token.MOD , "mod" );

    }

    private static final int _while1[] = new int[]{ Token.GOTO , Token.TARGET , 0 , 0 , Token.TARGET , Token.IFEQ , Token.TARGET };
    private static final int _doWhile1[] = new int[]{ Token.TARGET , 0 , Token.TARGET , Token.IFEQ , Token.TARGET };

    private static Node[] _matches( Node n , int types[] ){
        Node foo[] = new Node[types.length];
        
        for ( int i=0; i<types.length; i++ ){
            foo[i] = n;
            if ( types[i] > 0 && n.getType() != types[i] ) 
                return null;
            n = n.getNext();
        }

        return n == null ? foo : null;
    }
}
