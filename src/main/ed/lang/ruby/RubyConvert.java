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
    
    final static boolean D = true;

    public RubyConvert( File f )
        throws IOException {

        _name = f.toString();
        _lines = new ArrayList<String>();
        _warnings = new NullWarnings();

        DefaultRubyParser p = new DefaultRubyParser();
        p.setWarnings( _warnings );
        
        RubyParserResult r = p.parse( new ParserConfiguration( 1 , true ) ,
                                      new InputStreamLexerSource( f.toString() , new FileInputStream( f ) ,
                                                                  _lines , 1 , false ) );
        _ast = r.getAST();

        _add( _ast , new State() );
    }
    
    void _add( Node node , State state ){
        
        if ( node == null || state == null )
            throw new RuntimeException( "can't be null" );
        
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

        else if ( node instanceof FCallNode ){
            FCallNode f = (FCallNode)node;
            _appned( f.getName() + "(" , node );
            if ( node.childNodes() != null )
                for ( Node c : node.childNodes() )
                    _add( c , state );
            _appned( ")" , node );
        }
        
        else if ( node instanceof CallNode ){
            CallNode f = (CallNode)node;
            

            if ( _isOperator( f ) ){
                _appned( "(" , node );

                for ( int i=0; i<node.childNodes().size(); i++ ){
                    if ( i > 0 )
                        _appned( " " + f.getName() + " " , node );
                    _add( node.childNodes().get(i) , state );
                }

                _appned( ")" , node );
            }
            else {
                _appned( f.getName() + "(" , node );
            
                if ( node.childNodes() != null ){
                    for ( int i=0; i<node.childNodes().size(); i++ ){
                        if ( i > 0 )
                            _appned( " , " , node );
                        _add( node.childNodes().get(i) , state );
                    }
                }

                _appned( ")" , node );
            }



        }
        
        else if ( node instanceof ArrayNode ){
            if ( node.childNodes() == null || 
                 node.childNodes().size() == 0 ||
                 node.childNodes().size() > 1 )
                throw new RuntimeException( "don't know about this yet" );
            
            _add( node.childNodes().get(0) , state );
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

        // --- literals ---

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

        else {
            String msg = "don't yet support : " + node.getClass();
            System.err.println( msg );
            _print( 0 , node );
            throw new RuntimeException( msg );
        }
    }

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
        return _operatorNames.contains( node.getName() );
    }

    public String getJSSource(){
        return _js.toString();
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
        
        if ( D ) System.out.println( js );

        Convert c = new Convert( _name , js , false );
        _func = c.get();
        
        return _func;
    }

    class State {
        
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
    }
}
