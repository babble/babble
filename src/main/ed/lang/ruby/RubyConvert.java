// RubyConvert.java

package ed.lang.ruby;

import java.io.*;
import java.util.*;

import org.jruby.ast.*;
import org.jruby.common.*;
import org.jruby.parser.*;
import org.jruby.lexer.yacc.*;

import ed.js.*;
import ed.js.engine.*;

public class RubyConvert extends ed.MyAsserts {
    
    final static boolean D = Boolean.getBoolean( "DEBUG.RUBY" ) || true;

    public RubyConvert( File f )
        throws IOException {
        this( f.toString() , new FileInputStream( f ) );
    }

    public RubyConvert( String name , InputStream in )
        throws IOException {

        _name = name;
        _lines = new ArrayList<String>();
        _warnings = new NullWarnings();

        DefaultRubyParser p = new DefaultRubyParser();
        p.setWarnings( _warnings );
        
        RubyParserResult r = p.parse( new ParserConfiguration( 1 , true ) ,
                                      new InputStreamLexerSource( _name , in ,
                                                                  _lines , 1 , false ) );
        _ast = r.getAST();
        if ( D ) _print( 0 , _ast );
        _add( _ast , new State() );
    }
    
    void _add( Node node , State state ){
        
        if ( node == null || state == null )
            throw new RuntimeException( "can't be null" );
        
        // --- blocking ----

        if ( node instanceof RootNode ){
            _assertOne( node );
            _add( node.childNodes().get(0) , state );
        }

        else if ( node instanceof BlockNode ){
            _appned( "{\n" , node );
            if ( node.childNodes() != null )
                for ( Node c : node.childNodes() )
                    _add( c , state );
            _appned( "\n}\n" , node );
        }

        else if ( node instanceof NewlineNode ){
            _assertOne( node );
            _add( node.childNodes().get(0) , state );
            _appned( ";\n" , node );
        }

        else if ( node instanceof ReturnNode ){
            _assertOne( node );
            _appned( "return " , node );
            _add( node.childNodes().get(0), state );
        }

        // --- function stuff ---

        else if ( node instanceof FCallNode ){
            FCallNode f = (FCallNode)node;

            if ( f.getArgsNode() == null || 
                 f.getArgsNode().childNodes() == null || 
                 f.getArgsNode().childNodes().size() == 0 ){
                // no args
                _appned( Ruby.RUBY_V_CALL + "(" + _getFuncName( f ) + ")" , f );
            }
            else { 
                // has args
                _appned( _getFuncName( f )  , node );
                _addArgs( f , f.getArgsNode().childNodes() , state );
            }
        }

        
        else if ( node instanceof CallNode ){
            _addCall( (CallNode)node , state );
        }
        
        else if ( node instanceof DefnNode ){

            DefnNode dn = (DefnNode)node;
            if ( dn.childNodes().size() != 3 )
                throw new RuntimeException( "DefnNode should only have 3 children" );
            
            if ( state._className != null )
                _appned( state._className + ".prototype." , node );
            _appned( dn.getName() + " = function(" , node );
            
            ArgsNode an = dn.getArgsNode();
            if ( an != null && an.getArgs() != null ){
                for ( int i=0; i<an.getArgs().size(); i++ ){
                    ArgumentNode a = (ArgumentNode)an.getArgs().get(i);
                    if ( i > 0 )
                        _appned( " , " , a );
                    _appned( a.getName() , a );
                }
            }

            _appned( " ){\n" , node );
            _add( dn.childNodes().get( 2 ) , state );
            _appned( " \n}\n " , node );
        }

        else if ( node instanceof VCallNode ){
            _assertNoChildren( node );
            VCallNode vcn = (VCallNode)node;
            _appned( Ruby.RUBY_V_CALL + "( " + vcn.getName() + ")" , node );
        }

        // --- class stuff ---
        
        else if ( node instanceof ClassNode ){
            // complicated enough to warrant own method
            _addClass( (ClassNode)node , state );
        }

        else if ( node instanceof InstAsgnNode ){
            _assertOne( node );
            InstAsgnNode lan = (InstAsgnNode)node;
            _appned( "this." + lan.getName().substring(1) + " = " , node );
            _add( node.childNodes().get( 0 ) , state );
            _appned( "" , node );            
        }

        else if ( node instanceof InstVarNode ){
            _assertNoChildren( node );
            InstVarNode lvn = (InstVarNode)node;
            _appned( "this." + lvn.getName().substring(1) , node );
        }
        
        // --- looping ---

        else if ( node instanceof WhileNode ){
            _assertType( node.childNodes().get(0) , NewlineNode.class );
            _appned( "while ( " , node );
            _add( node.childNodes().get(0).childNodes().get(0) , state );
            _appned( " ){ \n " , node );
            _add( node.childNodes().get(1) , state );
            _appned( "\n } \n " , node );
        }

        // --- vars ---

        else if ( node instanceof LocalAsgnNode ){
            _assertOne( node );
            LocalAsgnNode lan = (LocalAsgnNode)node;
            _appned( "var " + lan.getName() + " = " , node );
            _add( node.childNodes().get( 0 ) , state );
            _appned( "" , node );
        }
        
        else if ( node instanceof LocalVarNode ){
            _assertNoChildren( node );
            LocalVarNode lvn = (LocalVarNode)node;
            _appned( lvn.getName() , node );
        }

        else if ( node instanceof ConstNode ){
            _assertNoChildren( node );
            ConstNode lvn = (ConstNode)node;
            _appned( lvn.getName() , node );
        }
        
        else if ( node instanceof DStrNode ){
            _appned( " ( " , node );
            for ( int i=0; i<node.childNodes().size(); i++ ){
                if ( i > 0 )
                    _appned( " + " , node );
                _add( node.childNodes().get(i) , state );
            }
            _appned( " ) " , node );
        }

        else if ( node instanceof EvStrNode ){
            _assertOne( node );
            _add( node.childNodes().get(0).childNodes().get(0) , state );
        }
        
        // --- literals ---

        else if ( node instanceof ArrayNode ){
            
            if ( node.childNodes() == null || 
                 node.childNodes().size() == 0 ){}
            else if ( node.childNodes().size() > 1 ){ 
                _print( 0 , node );
                throw new RuntimeException( "don't know about this yet" );
            }
            else {
                _add( node.childNodes().get(0) , state );
            }
        }

        else if ( node instanceof FixnumNode ){
            _assertNoChildren( node );
            _appned( String.valueOf( ((FixnumNode)node).getValue() ) , node );
        }

        else if ( node instanceof StrNode ){
            _assertNoChildren( node );
            _appned( "\"" , node );
            _appned( _escape( ((StrNode)node).getValue().toString() ) , node );
            _appned( "\"" , node );
        }

        else if ( node instanceof SymbolNode ){
            _assertNoChildren( node );
            _appned( "\"" , node );
            _appned( _escape( ((SymbolNode)node).getName() ) , node );
            _appned( "\"" , node );
        }

        else if ( node instanceof HashNode ){
            _assertOne( node );
            _assertType( node.childNodes().get(0) , ArrayNode.class );
            
            _appned( " { " , node );
            List<Node> lst = node.childNodes().get(0).childNodes();
            for ( int i=0; i<lst.size(); i+=2 ){
                if ( i > 0 )
                    _appned( " , " , node );
                
                _add( lst.get(i) , state );
                _appned( " : " , node );
                _add( lst.get(i+1) , state );
                
            }
            _appned( " } " , node );
        }
        

        // --- end ---

        else {
            String msg = "don't yet support : " + node.getClass();
            System.err.println( msg );
            _print( 0 , node );
            throw new RuntimeException( msg );
        }
    }

    // ---  code generation types ---

    void _addClass( ClassNode cn , State state ){
        
        final String name = cn.getCPath().getName();
        
        state = state.child();
        state._className = name;

        _assertType( cn.childNodes().get(0) , Colon2Node.class );
        
        state._classInit = _findClassInit( cn );
        
        // constructor
        if ( state._classInit == null ){
            _appned( name + " = function(){};" , cn );
        }
        else {
            _appned( name + " = function(" , state._classInit );
            
            _appned( "){\n" , state._classInit );
            _add( state._classInit.childNodes().get(2) , state );
            _appned( "\n}\n" , state._classInit );
        }

        if ( cn.getSuperNode() != null ){
            _appned( "\n" + name + ".prototype = new " , cn );
            _add( cn.getSuperNode() , state );
            _appned( "();\n" , cn );
        }
        
        for ( Node c : cn.childNodes() )
            _addClassPiece( c , state );
    }

    DefnNode _findClassInit( Node n ){
        for ( Node c : n.childNodes() ){

            if ( c instanceof DefnNode ){
                DefnNode dn = (DefnNode)c;
                if ( dn.getName().equals( "initialize" ) )
                    return dn;
            }

            if ( c instanceof BlockNode || 
                 c instanceof NewlineNode ){
                DefnNode ret = _findClassInit( c );
                if ( ret != null )
                    return ret;
            }
        }    

        return null;
    }

    void _addClassPiece( Node n , State state ){
        if ( n instanceof Colon2Node || // meta class info
             n instanceof ConstNode  // inheritance
             )
            return;

        if ( n instanceof BlockNode ){
            for ( Node c : n.childNodes() )
                _addClassPiece( c , state );
            return;
        }

        if ( n instanceof NewlineNode ){
            _addClassPiece( n.childNodes().get(0) , state );
            _appned( ";" , n );
            return;
        }
        
        if ( n instanceof DefnNode ){
            DefnNode dn = (DefnNode)n;
            if ( dn != state._classInit )
                _add( dn , state );
            return;
        }
        
        if ( n instanceof FCallNode ){
            FCallNode f = (FCallNode)n;
            _appned( _getFuncName( f ) + ".call( " + state._className + ".prototype " , f );
            if ( f.getArgsNode() != null && f.getArgsNode().childNodes() != null ){
                for ( int i=0; i<f.getArgsNode().childNodes().size(); i++ ){
                    _appned( " , " , f );
                    _add( f.childNodes().get(i) , state );
                }
            }
            _appned( ")" , f );
            return;
        }

        throw new RuntimeException( "don't know about class piece : " + n.getClass() );

    }

    void _addCall( CallNode call , State state ){
            
        if ( call.getName().equals( "[]" ) ){
            _add( call.childNodes().get(0) , state );
            _appned( "[" , call );
            _add( call.childNodes().get(1) , state );
            _appned( "]" , call );
            return;
        }

        if ( _isOperator( call ) ){
            _addArgs( call , call.childNodes() , state , " " + call.getName() + " " );
            return;
        }
        
        if ( call.getName().equals( "new" ) ){
            _appned( " new " , call );
            _add( call.childNodes().get(0) , state );
            _addArgs( call , call.childNodes().get(1).childNodes() , state );
            return;
        }
        
        // normal function call
        if ( call.childNodes().get(0) instanceof ArrayNode ){
            _appned( call.getName() , call );
            _addArgs( call , call.childNodes() , state );
            return;
        }

        // class method call
        if ( call.childNodes().size() > 1 ){

            _assertType( call.childNodes().get(1) , ArrayNode.class );
            
            Node self = call.childNodes().get(0);
            Node args = call.childNodes().get(1);
            
            if ( args.childNodes().size() > 0 ){
                _add( call.childNodes().get(0) , state );
                _appned( "." + call.getName() , call );
                _addArgs( call , call.childNodes().get(1).childNodes() , state );
            }
            else {
                _appned( Ruby.RUBY_CV_CALL + "( " , call);
                _add( self , state );
                _appned( " , " , call );
                _appned( "\"" + call.getName() + "\"" , call );
                _appned( " ) " , call );
            }
            return;
        }
        
        // no-args
        _appned( Ruby.RUBY_V_CALL + "(" , call );
        _add( call.childNodes().get(0) , state );
        _appned( "." + call.getName() , call );
        _appned( ")" , call );

    }

    // ---- add args ----

    void _addArgs( final Node where , List<Node> lst , final State state ){
        _addArgs( where , lst , state , " , " );
    }

    void _addArgs( final Node where , final List<Node> lst , final State state , final String sep ){
        _appned( "(" , where );

        if ( lst != null ){
            for ( int i=0; i<lst.size(); i++ ){
                if ( i > 0 )
                    _appned( sep , where );
                _add( lst.get( i ) , state );
            }
        }

        _appned( ")" , where );
    }

    // ---  asserts  ---

    void _assertNoChildren( Node n ){
        if ( n.childNodes() != null && n.childNodes().size() > 0 )
            throw new RuntimeException( "has children but shouldn't" );
    }
    
    void _assertOne( Node n ){
        if ( n == null )
            throw new RuntimeException( "can't be null" );
        if ( n.childNodes() == null || n.childNodes().size() != 1 )
            throw new RuntimeException( "need exactly 1 child" );
        
    }
    
    void _assertType( Node n , Class c ){
        if ( n != null && c.isAssignableFrom( n.getClass() ) )
            return;
        throw new RuntimeException( n + " is not an instanceof " + c );
    }

    // ---  utility stuff ---

    void _print( int space , Node n ){
        for ( int i=0; i<space; i++ )
            System.out.print( " " );
        
        System.out.println( n );
        for ( Node c : n.childNodes() )
            _print( space + 1 , c );
        
    }
    
    void _appned( String s , Node where ){
        _js.append( s );
    }

    String _escape( String s ){
        StringBuilder buf = new StringBuilder( s.length() );
        for ( int i=0; i<s.length(); i++ ){
            char c = s.charAt( i );
            if(c == '\\')
                buf.append("\\\\");
            else if(c == '"')
                buf.append("\\\"");
            else if(c == '\n')
                buf.append("\\n");
            else if(c == '\r')
                buf.append("\\r");
            else 
                buf.append(c);
        }
        return buf.toString();
    }

    boolean _isOperator( CallNode node ){
        if ( node.getName().equals( "[]" ) )
            throw new RuntimeException( "array thing" );
        return _operatorNames.contains( node.getName() );
    }

    String _getFuncName( FCallNode node ){
        final String name = node.getName();
        
        if ( name.equals( "puts" ) )
            return "print";
        
        return name;
    }

    public String getJSSource(){
        String js = _js.toString();
        if ( D ) System.out.println( js );
        return js;
    }

    public JSFunction get()
        throws IOException {
        return getFunc();
    }

    public JSFunction getFunc()
        throws IOException {
        if ( _func != null )
            return _func;
        
        final String js = getJSSource();
        
        Convert c = new Convert( _name , js , false );
        _func = c.get();
        
        return _func;
    }

    class State {
        
        State(){
            this( null );
        }
        
        State( State parent ){
            _parent = parent;
        }

        State child(){
            return new State( this );
        }
        
        final State _parent;
        String _className;
        DefnNode _classInit;
    }

    final String _name;
    final List<String> _lines;
    final IRubyWarnings _warnings;
    final Node _ast;
    
    final StringBuilder _js = new StringBuilder();
    final Map<Integer,ISourcePosition> _lineMap = new TreeMap<Integer,ISourcePosition>();

    private JSFunction _func;

    static final Set<String> _operatorNames = new HashSet<String>();
    static {
        _operatorNames.add( "+" );
        _operatorNames.add( "-" );
        _operatorNames.add( "*" );
        _operatorNames.add( "/" );
        _operatorNames.add( ">" );
        _operatorNames.add( ">=" );
        _operatorNames.add( "<" );
        _operatorNames.add( "<=" );
    }
}
