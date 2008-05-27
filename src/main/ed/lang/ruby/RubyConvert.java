// RubyConvert.java

package ed.lang.ruby;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.jruby.ast.*;
import org.jruby.ast.types.*;
import org.jruby.common.*;
import org.jruby.parser.*;
import org.jruby.lexer.yacc.*;

import ed.lang.*;
import ed.js.*;
import ed.js.engine.*;
import ed.appserver.templates.*;
import static ed.lang.ruby.Ruby.*;

public class RubyConvert extends ed.MyAsserts {
    
    final static boolean OUTPUT_FILE = Boolean.getBoolean( "DEBUG.RUBYF" ) || true;
    final static boolean APPEND_LINES = Boolean.getBoolean( "DEBUG.RUBYL" );
    
    final static boolean DD = Boolean.getBoolean( "DEBUG.RUBY" );

    final static String JS_TRUE = "if ( 5 == 5 ) ";
    final static String JS_R = JS_TRUE + " return ";
    final boolean D;

    public static class TemplateImpl implements TemplateConverter {

        public Result convert( Template t ){
            if ( ! t.getName().endsWith( ".rb" ) )
                return null;
            
            try {
                final RubyConvert rc = new RubyConvert( t.getName() , t.getContent() );
                final String jsSource = rc.getJSSource();
                return new TemplateConverter.Result( new Template( t.getName().replaceAll( ".rb$" , "_rb.js" ) , jsSource , Language.RUBY ) , rc._lineMapping );
            }
            catch ( IOException ioe ){
                throw new RuntimeException( "couldn't convert : " + t.getName() , ioe );
            }
        }
    }
    
    public RubyConvert( File f )
        throws IOException {
        this( f.toString() , new FileInputStream( f ) );
    }

    public RubyConvert( String name , String code )
        throws IOException {
        this( name , new ByteArrayInputStream( code.getBytes() ) );
    }
    
    public RubyConvert( String name , InputStream in )
        throws IOException {

        D = DD && ! name.contains( "src/main/ed/lang/ruby" );

        _name = name;
        _lines = new ArrayList<String>();
        _warnings = new NullWarnings();

        DefaultRubyParser p = new DefaultRubyParser();
        p.setWarnings( _warnings );
        
        try {
            RubyParserResult r = p.parse( new ParserConfiguration( 1 , true ) ,
                                          new InputStreamLexerSource( _name , in ,
                                                                      _lines , 1 , false ) );
            _ast = r.getAST();
        }
        catch ( SyntaxException se ){
            throw new RuntimeException( "can't compile [" + name + "] " + se + " : " + se.getPosition() , se );
        }
        if ( D ) _print( 0 , _ast );
        _add( _ast , new State() );
    }
    
    void _add( Node node , State state ){
        
        if ( node == null || state == null )
            return;
        
        // --- blocking ----

        if ( node instanceof RootNode ){
            _assertOne( node );
            _add( node.childNodes().get(0) , state );
        }

        else if ( node instanceof BlockNode ){
            _addBlock( node , state );
            /*
            _append( "{\n" , node );
            if ( node.childNodes() != null ){
                for ( int i=0; i<node.childNodes().size(); i++ ){
                    Node c = node.childNodes().get(i);
                    _add( c , state );
                    _append( "\n;\n" , node );
                }
            }
            _append( "\n}\n" , node );
            */            
        }

        else if ( node instanceof NewlineNode ){
            _assertOne( node );
            final Node c = node.childNodes().get(0);
            _add( c , state );
            _append( "\n" , node );
        }

        else if ( node instanceof ReturnNode ){
            if ( node.childNodes().size() == 0 ){
                _append( RUBY_RETURN + "()" , node );
            }
            else {
                _assertOne( node );
                _append( RUBY_RETURN + "( " , node );
                _add( node.childNodes().get(0), state );
                _append( " ) " , node );
            }
        }

        else if ( node instanceof ForNode ){

            ForNode fn = (ForNode)node;
            
            _add( fn.getIterNode() , state );
            _append( ".each( " , fn );
            _addIterBlock( fn , state );
            _append( " ) " , fn );
        }

        else if ( node instanceof IterNode ){
            _addIterBlock( (IterNode)node , state );
        }

        else if ( node instanceof YieldNode ){
            _append( "arguments[ arguments.length - 1 ]" , node );
            YieldNode yn = (YieldNode)node;
            Node a = yn.getArgsNode();
            _addArgs( node , a instanceof ArrayNode ? a.childNodes() : yn.childNodes() , state );
        }
        
        else if ( node instanceof CaseNode ){
            CaseNode cn = (CaseNode)node;

            String name = "caseTemp" + (int)(Math.random() * 10000);

            _append( "var " + name + " = " , cn );
            _add( cn.getCaseNode() , state );
            _append( ";\n" , cn );
            
            Node when = cn.getFirstWhenNode();
            boolean first = true;
            while ( when != null ){

                if ( first )
                    first = false;
                else
                    _append( " else " , when );
                
                if  ( when instanceof WhenNode ){
                    WhenNode w = (WhenNode)when;
                    
                    _append( "if( " , when );
                    
                    final Node expr = w.getExpressionNodes();

                    if ( expr instanceof ArrayNode && 
                         expr.childNodes().size() == 1 && 
                         expr.childNodes().get(0) instanceof DotNode ){
                        _add( expr , state );
                        _append( ".contains( " + name + " ) " , when );
                    }
                    else {
                        _append( name + " == " , when );
                        _add( expr , state );
                    }
                    _append( " ){ " , when );
                    _addBlock( w.getBodyNode() , state );
                    _append( "\n}\n" , when );
                    
                    when = w.getNextCase();
                    continue;
                }
                
                _append( " { " , when );
                _addBlock( when , state );
                _append( " } " , when );
                break;
            }
            
        }

        else if ( node instanceof BeginNode ){
            BeginNode bn = (BeginNode)node;
            _assertOne( bn );
            if ( bn.getBodyNode() instanceof RescueNode )
                _addRescueNode( (RescueNode)bn.getBodyNode() , state , true );
            else
                _add( bn.getBodyNode() , state );
        }

        else if ( node instanceof RescueNode ){
            RescueNode rn = (RescueNode)node;
            _addRescueNode( rn , state , false );
        }

        else if ( node instanceof EnsureNode ){
            if ( state._ensure != null )
                throw new RuntimeException( "uh oh, don't handle this" );
            EnsureNode en = (EnsureNode)node;
            state._ensure = en.getEnsureNode();
            _add( en.getBodyNode() , state );
        }

        // --- function stuff ---

        else if ( node instanceof FCallNode ){
            FCallNode f = (FCallNode)node;
            
            if ( ( f.getArgsNode() == null || f.getArgsNode().childNodes() == null || f.getArgsNode().childNodes().size() == 0 ) &&
                 f.getIterNode() == null ){
                // no args
                final String funcName = _getFuncName( f );
                _append( Ruby.RUBY_V_CALL + "(" + funcName + ", \"" + funcName + "\" , this )" , f );
            }
            else if ( f.getName().equals( "JSRAW" ) ){
                if ( f.getArgsNode().childNodes().size() != 1 )
                    throw new RuntimeException( "bad JSRAW" );
                if ( ! ( f.getArgsNode().childNodes().get(0) instanceof StrNode ) )
                    throw new RuntimeException("bad JSRAW" );
                _append( ((StrNode)(f.getArgsNode().childNodes().get(0))).getValue().toString() , f );
            }
            else {
                _append( _getFuncName( f )  , node );
                
                boolean first = true;
                
                if ( state._className != null ){
                    _append( ".call(" + state._className + ".prototype " , node );
                    first = false;
                }
                else
                    _append( "(" , node );
                
                if ( f.getArgsNode() != null && f.getArgsNode().childNodes() != null ){
                    for ( Node temp : f.getArgsNode().childNodes() ){
                        _addOneArg( temp , state , first ? " " : " , " );
                        first = false;
                    }
                }
                
                if ( f.getIterNode() != null ){
                    if ( ! first )
                        _append( " , " , f );
                    _add( f.getIterNode() , state );
                }
            
                _append( ")" , node );    
            }
            
        }
        
        
        else if ( node instanceof CallNode ){
            _addCall( (CallNode)node , state );
        }
        
        else if ( node instanceof MethodDefNode ){

            MethodDefNode dn = (MethodDefNode)node;
            
            if ( state._className != null ){
                _append( state._className + "." + ( ( dn instanceof DefsNode || state._module ) ? "" : "prototype." ) , node );
            }
            final String funcName = _mangleFunctionName( dn.getName() );
            _append( funcName + " = function(" , node );
            
            state = state.child();
            state._curMethod = funcName;
            
            ArgsNode an = dn.getArgsNode();
            if ( an != null ){
                
                int num = 0;
                
                if ( an.getArgs() != null ){
                    for ( int i=0; i<an.getArgs().size(); i++ ){
                        ArgumentNode a = (ArgumentNode)an.getArgs().get(i);
                        if ( num++ > 0 )
                            _append( " , " , a );
                        _append( _getName( a ) , a );
                    }
                }

                if ( an.getOptArgs() != null ){
                    for ( int i=0; i<an.getOptArgs().size(); i++ ){
                        LocalAsgnNode a = (LocalAsgnNode)an.getOptArgs().get(i);
                        if ( num++ > 0 )
                            _append( " , " , a );
                        _append( _getName( a ) + " = " , a );
                        _add( a.getValueNode() , state );
                    }
                }

                if ( an.getBlockArgNode() != null ){
                    _append( " , " + _getName( an.getBlockArgNode() ) , an );
                }
                
            }


            final Node body = dn.getBodyNode();
            
            _append( " ){\n" , node );
            _append( "try { \n " , node );
            _append( "var __last__ = null;\n" , node );

            if ( body == null ){
            }
            else if ( body instanceof BlockNode ){
                _addBlock( body , state , false );
            }
            else {
                if ( _isReturnable( body ) ){
                    _append( JS_R , body );
                }
                _add( body , state );
                _append( ";" , body );
            }
            _append( "\n return __last__; \n" , node );
            _append( " } catch ( ezzz if __risReturnThing( ezzz ) ){ return ezzz.getReturn(); } " , node );
            _append( " }\n " , node );
        }

        else if ( node instanceof VCallNode ){
            _assertNoChildren( node );
            VCallNode vcn = (VCallNode)node;
            final String funcName = _getFuncName( vcn );
            _append( Ruby.RUBY_V_CALL + "( " + funcName + " , \"" + funcName + "\" , this )" , node );
        }

        else if ( node instanceof BlockPassNode ){
            BlockPassNode bp = (BlockPassNode)node;
            _add( bp.getBodyNode() , state );
        }
        
        else if ( node instanceof ZSuperNode ){
            if ( state._curMethod != null ){
                _append( "( __last__ = this.getSuper()." + state._curMethod + "( ) )" , node );
            }
        }

        else if ( node instanceof SuperNode ){
            SuperNode sn = (SuperNode)node;
            
            if ( sn.getArgsNode() == null )
                throw new RuntimeException( "shouldn't this be a Z, not a regular?" );

            if ( sn.getIterNode() != null )
                throw new RuntimeException( "what?" );

            _append( "this.__proto__.constructor.call( this " , sn );
            for ( Node foo : sn.getArgsNode().childNodes() ){
                _append( " , " , sn );
                _add( foo , state );
            }
            _append( " )" , sn );
        }

        // --- class stuff ---
        
        else if ( node instanceof ClassNode ){
            // complicated enough to warrant own method
            _addClass( (ClassNode)node , state );
        }

        else if ( node instanceof ModuleNode ){
            _addModule( (ModuleNode)node , state );
        }

        else if ( node instanceof InstAsgnNode ){
            _assertOne( node );
            InstAsgnNode lan = (InstAsgnNode)node;
            _append( "this." + _mangleVarName( lan.getName().substring(1) ) + " = " , node );
            _add( node.childNodes().get( 0 ) , state );
            _append( "" , node );            
        }

        else if ( node instanceof InstVarNode ){
            _assertNoChildren( node );
            InstVarNode lvn = (InstVarNode)node;
            _append( "this." + _mangleVarName( lvn.getName().substring(1) ) , node );
        }
        
        else if ( node instanceof SelfNode ){
            _append( "this" , node );
        }

        else if ( node instanceof OpAsgnNode ){
            OpAsgnNode oan = (OpAsgnNode)node;
            _append( "__rvarset( " , oan );
            _add( oan.getReceiverNode() , state );
            _append( " , \"" + oan.getVariableName() + "\" , ( " , oan );
            _add( oan.getReceiverNode() , state );
            _append( "." + oan.getVariableName() + " " + oan.getOperatorName() , oan );
            _add( oan.getValueNode() , state );
            _append( " ) ) " , oan );
        }

        // --- looping ---

        
        else if ( node instanceof NextNode ){
            _append( "return true" , node );
        }

        else if ( node instanceof RedoNode ){
            _append( "return -111" , node );
        }

        else if ( node instanceof BreakNode ){
            if ( state._whileCount > 0 )
                _append( "break" , node );
            else
                _append( "return 0" , node );
        }

        else if ( node instanceof WhileNode ){
            
            state._whileCount++;
            
            _append( "while ( " , node );
            _add( node.childNodes().get(0) , state );
            _append( " ){ \n " , node );
            _add( node.childNodes().get(1) , state );
            _append( "\n } \n " , node );

            state._whileCount--;
        }

        else if ( node instanceof IfNode ){
            IfNode ifn = (IfNode)node;

            if ( ! _handleTurnary( ifn , state ) ){
                _append( "if ( " , ifn );
                if ( ifn.getCondition() != null )
                    _add( ifn.getCondition() , state );
                _append( " ){ \n " , ifn );
                if ( ifn.getThenBody() != null )
                    _addBlock( ifn.getThenBody() , state );
                _append( " } \n " , ifn );
                if ( ifn.getElseBody() != null ){
                    _append( " else { \n " , ifn );
                    _addBlock( ifn.getElseBody() , state );
                    _append( " } \n " , ifn );
                }
            }
        }

        else if ( node instanceof DefinedNode ){
            _append( "Ruby.defined( " , node );
            _add( ((DefinedNode)node).getExpressionNode() , state );
            _append( " ) " , node );
        }

        // --- operators ---

        else if ( node instanceof NotNode ){
            _assertOne( node );
            _append( " ! ( " , node );
            _add( node.childNodes().get(0), state );
            _append( " ) " , node );
        }

        else if ( node instanceof OpAsgnOrNode ){
            OpAsgnOrNode op = (OpAsgnOrNode)node;
            _add( op.getFirstNode() , state );
            _append( " = " , node );
            _append( " ( " , node );
            _add( op.getFirstNode() , state );
            _append( " || " , node );
            _add( op.getSecondNode().childNodes().get(0) , state );
            _append( " ) " , node );
        }

        else if ( node instanceof OpAsgnAndNode ){
            OpAsgnAndNode op = (OpAsgnAndNode)node;
            _add( op.getFirstNode() , state );
            _append( " = " , node );
            _append( " ( " , node );
            _add( op.getFirstNode() , state );
            _append( " ? " , node );
            _add( op.getSecondNode().childNodes().get(0) , state );
            _append( " : " , node );
            _add( op.getFirstNode() , state );
            _append( " ) " , node );
        }

        else if ( node instanceof AndNode ){
            AndNode a = (AndNode)node;
            _append( " __rand( " , a );
            _add( a.getFirstNode() , state );
            _append( " , " , a );
            _add( a.getSecondNode() , state );
            _append( " ) " , a );
        }

        else if ( node instanceof OrNode ){
            OrNode a = (OrNode)node;
            _append( " ( " , a );
            _add( a.getFirstNode() , state );
            _append( " || " , a );
            _add( a.getSecondNode() , state );
            _append( " ) " , a );
        }

        else if ( node instanceof Match3Node ){
            Match3Node mn = (Match3Node)node;

            _add( mn.getReceiverNode() , state );
            _append( ".__rmatch( " , mn );
            _add( mn.getValueNode() , state );
            _append( ")" , mn );

        }

        else if ( node instanceof Match2Node ){
            Match2Node mn = (Match2Node)node;

            _add( mn.getReceiverNode() , state );
            _append( ".__rmatch( " , mn );
            _add( mn.getValueNode() , state );
            _append( ")" , mn );

        }

        else if ( node instanceof NthRefNode ){
            NthRefNode nth = (NthRefNode)node;
            _append( "(RegExp.last().getLast()[" + nth.getMatchNumber() + "])" , nth );
        }

        else if ( node instanceof BackRefNode ){
            BackRefNode nth = (BackRefNode)node;
            _append( "(RegExp.last().getLast()[0])" , nth );
        }

        // --- vars ---

        else if ( node instanceof GlobalVarNode ){
            _append( _mangleVarName( (GlobalVarNode)node ) , node );
        }

        else if ( node instanceof ArgumentNode ){
            _append( _getName( ((ArgumentNode)node) ) , node );
        }

        else if ( node instanceof LocalAsgnNode ){
            _assertOne( node );
            LocalAsgnNode lan = (LocalAsgnNode)node;
            _addLocal( _getName( lan ) , node.childNodes().get(0) , state );
        }

        else if ( node instanceof GlobalAsgnNode ){
            _assertOne( node );
            GlobalAsgnNode gan = (GlobalAsgnNode)node;
            _append(  _getName( gan ) + " = " , gan );
            _add( gan.getValueNode() , state );
        }
        
        else if ( node instanceof LocalVarNode ){
            _assertNoChildren( node );
            LocalVarNode lvn = (LocalVarNode)node;
            _append( _getName( lvn ) , node );
        }

        else if ( node instanceof DVarNode ){
            _assertNoChildren( node );
            DVarNode lvn = (DVarNode)node;
            _append( _getName( lvn ) , node );
        }

        else if ( node instanceof ConstNode ){
            _assertNoChildren( node );
            ConstNode lvn = (ConstNode)node;
            _append( _getName( lvn )  , node );
        }
        
        else if ( node instanceof DStrNode ){
            _append( " ( " , node );
            for ( int i=0; i<node.childNodes().size(); i++ ){
                if ( i > 0 )
                    _append( " + " , node );
                _add( node.childNodes().get(i) , state );
            }
            _append( " ) " , node );
        }

        else if ( node instanceof EvStrNode ){
            if( node.childNodes().size() > 0 ){
                _assertOne( node );
                _append( "__revstr( " , node );
                _add( node.childNodes().get(0).childNodes().get(0) , state );
                _append( ") " , node );
            }
        }

        else if ( node instanceof Colon2Node ){
            _assertOne( node );
            Colon2Node cn = (Colon2Node)node;
            _add( node.childNodes().get(0) , state );
            _append( "." + _getName( cn ) , node );
        }

        else if ( node instanceof Colon3Node ){
            Colon3Node cn = (Colon3Node)node;
            _append( _getName( cn ) , node );
        }
        
        else if ( node instanceof AttrAssignNode ){
            AttrAssignNode aan = (AttrAssignNode)node;
            
            if ( D ){
                System.out.println( "name:" + aan.getName() );
                System.out.println( "args:" + aan.getArgsNode() );
                System.out.println( "reci:" + aan.getReceiverNode() );
            }
                                

            final String name = aan.getName().trim();
            
            
            if ( name.equals( "[]=" ) ){

                _append( " ( " , aan );
                _add( aan.getReceiverNode() , state );
                
                _append( "[" , aan );
                _add( aan.getArgsNode().childNodes().get(0) , state );
                _append( "]" , aan );
                
                _append( " = " , aan );
                _add( aan.getArgsNode().childNodes().get(1) , state );
                
                _append( " ) " , aan );
            }
            else {
                _append( "__rvarset( " , aan );
                _add( aan.getReceiverNode() , state );
                _append( " , " , aan );
                _append( "\"" + name.substring( 0 , name.length() - 1 ) + "\" , " , aan );
                _add( aan.getArgsNode() , state );
                _append( " ) " , aan );
            }

        }

        else if ( node instanceof OpElementAsgnNode ){
            OpElementAsgnNode op = (OpElementAsgnNode)node;
            
            _append( "(" , op );

            _add( op.getReceiverNode() , state );
            _append( "[" , op );
            _add( op.getArgsNode() , state );
            _append( "]" , op );
                        
            _append( "=" , op );

            _add( op.getReceiverNode() , state );
            _append( "[" , op );
            _add( op.getArgsNode() , state );
            _append( "]" , op );

            _append( op.getOperatorName()  , op );
            _add( op.getValueNode() , state );

            _append( ")" , op );
        }

        else if ( node instanceof ClassVarNode ){
            if ( state._className != null )
                _append( state._className , node );
            else 
                _append( "this.__constructor__" , node );

            _append( "." + ((ClassVarNode)node).getName().substring(2) , node );
        }

        else if ( node instanceof ClassVarAsgnNode ){
            ClassVarAsgnNode cva = (ClassVarAsgnNode)node;
            
            if ( state._className != null )
                _append( state._className , node );
            else 
                _append( "this.__constructor__" , node );
            
            _append( "." + cva.getName().substring(2) , node );
            _append( " = " , node );
            _add( cva.getValueNode() , state );
        }
        
        else if ( node instanceof MultipleAsgnNode ){
            MultipleAsgnNode man = (MultipleAsgnNode)node;

            _append( "__rtoarray( " , man );
            _add( man.getValueNode() , state );
            _append( ").__multiAssignment(" , man );
            
            boolean first = true;
            for ( Node temp : man.getHeadNode().childNodes() ){

                if ( first )
                    first = false;
                else
                    _append( " , " , temp );
                
                if ( temp instanceof LocalAsgnNode ){
                    _append( " scope " , temp );
                    _append( " , " , temp );
                    _append( "\"" + ((LocalAsgnNode)temp).getName() + "\"" , temp );
                }
                else if ( temp instanceof DAsgnNode ){
                    _append( " scope " , temp );
                    _append( " , " , temp );
                    _append( "\"" + ((DAsgnNode)temp).getName() + "\"" , temp );
                }
                else if ( temp instanceof AttrAssignNode ){
            
                    AttrAssignNode aan = (AttrAssignNode)temp;
                    
                    _add( aan.getReceiverNode() , state );
                    _append( " , " , aan );
                    
                    String name = aan.getName();
                    name = name.substring( 0 , name.length() - 1 );
                    if ( name.equals( "[]" ) ){
                        _append( " ( " , aan );
                        _add( aan.getArgsNode().childNodes().get(0) , state );
                        _append( " ).toString() " , aan );
                    }
                    else
                        _append( " \"" + name + "\" " , aan );
                    
                }
                else if ( temp instanceof InstAsgnNode ){
                    InstAsgnNode ian = (InstAsgnNode)temp;
                    _append( " this , \"" + ian.getName().substring(1) + "\" "  , ian );
                }
                else {
                    throw new RuntimeException( "don't know how to do a multi-assign to : " + temp + " : " + temp.getPosition() );
                }
            }
            
            _append( " )" , man );
        }
        
        else if ( node instanceof DAsgnNode ){
            DAsgnNode dn = (DAsgnNode)node;
            _addLocal( dn.getName() , dn.getValueNode() , state );
        }
        
        else if ( node instanceof ConstDeclNode ){
            ConstDeclNode cdn = (ConstDeclNode)node;
            _append( cdn.getName() + " = " , cdn );
            _add( cdn.getValueNode() , state );
        }

        // --- literals ---

        else if ( node instanceof DotNode ){
            DotNode dn = (DotNode)node;
            _append( Ruby.RUBY_RANGE + "(" , dn );
            _add( dn.getBeginNode() , state );
            _append( " , " , dn );
            _add( dn.getEndNode() , state );
            _append( ")" , dn );
        }

        else if ( node instanceof ToAryNode ){
            _append( "__rtoarray(" , node );
            _add( ((ToAryNode)node).getValue() , state );
            _append( ")" , node );
        }

        else if ( node instanceof NilNode ){
            _append( " null " , node );
        }
        
        else if ( node instanceof ZArrayNode ){
            _append( "(new Array())" , node );
        }

        else if ( node instanceof ArrayNode ){
            
            if ( node.childNodes() == null || 
                 node.childNodes().size() == 0 ){}
            else if ( node.childNodes().size() > 1 ){ 
                _append( "[" , node );
                for ( int i=0; i<node.childNodes().size(); i++){
                    if ( i > 0 )
                        _append( " , " , node );
                    _add( node.childNodes().get(i) , state );
                }
                _append( "]" , node );
            }
            else {
                _add( node.childNodes().get(0) , state );
            }
        }

        else if ( node instanceof TrueNode ){
            _assertNoChildren( node );
            _append( ((TrueNode)node).getName() , node );
       } 

        else if ( node instanceof FalseNode ){
            _assertNoChildren( node );
            _append( ((FalseNode)node).getName() , node );
        }

        else if ( node instanceof FixnumNode ){
            _assertNoChildren( node );
            _append( String.valueOf( ((FixnumNode)node).getValue() ) , node );
        }

        else if ( node instanceof FloatNode ){
            _assertNoChildren( node );
            _append( String.valueOf( ((FloatNode)node).getValue() ) , node );
        }

        else if ( node instanceof StrNode ){
            _assertNoChildren( node );
            _append( "\"" , node );
            _append( _escape( ((StrNode)node).getValue().toString() ) , node );
            _append( "\"" , node );
        }

        else if ( node instanceof SymbolNode ){
            _assertNoChildren( node );
            _append( "\"" , node );
            _append( _escape( ((SymbolNode)node).getName() ) , node );
            _append( "\"" , node );
        }

        else if ( node instanceof HashNode ){
            _assertOne( node );
            _assertType( node.childNodes().get(0) , ArrayNode.class );
            
            _append( RUBY_BUILD_HASH + "( " , node );
            List<Node> lst = node.childNodes().get(0).childNodes();
            for ( int i=0; i<lst.size(); i+=2 ){
                if ( i > 0 )
                    _append( " , " , node );
                
                _add( lst.get(i) , state );
                _append( " , " , node );
                _add( lst.get(i+1) , state );
                
            }
            _append( " ) " , node );
        }
        
        else if ( node instanceof RegexpNode ){
            RegexpNode rn = (RegexpNode)node;
            _append( "/" + rn.getValue().toString().replaceAll( "\n" , "\\\\n" ) + "/" , rn );
            _append( _getRegexpOptions( rn.getOptions() ) , rn );
        }

        else if ( node instanceof DRegexpNode ){
            DRegexpNode dr = (DRegexpNode)node;
            _append( "( new RegExp( " , dr );
            _addArgs( dr , dr.childNodes() , state , " + " );
            _append( " , \"" + _getRegexpOptions( dr.getOptions() ) + "\" ) ) " , dr );
        }

        // --- end ---

        else {
            String msg = "don't yet support : " + node.getClass();
            msg += "\t" + node.getPosition();
            System.err.println( msg );
            _print( 0 , node );
            throw new RuntimeException( msg );
        }
    }

    // ---  code generation types ---

    void _addBlock( final Node n , final State state ){
        _addBlock( n , state , n instanceof BlockNode );
    }
        
    void _addBlock( final Node n , final State state , final boolean useBrackets ){
        
        if ( useBrackets )
            _append( "{\n" , n );
        
        Node last = n;
        if ( last != null ){
            if ( n instanceof BlockNode ){
                int i=0;
                for ( i=0; i<n.childNodes().size()-1; i++ ){
                    last = n.childNodes().get(i);
                    
                    if ( last == null )
                        continue;
                    
                    //_append( "// " + last.getClass().getName() + "\n" , last );
                    //_append( "// " + last.childNodes().get(0) + "\n" , last );

                    boolean r = _isReturnable( last );
                    if ( r )
                        _append( "__last__ = ( " , last );
                    _add( last , state );
                    if ( r )
                        _append( " ) " , last );
                    _append( ";\n" , last );
                }
                last = n.childNodes().get( i );
            }
            
            boolean r = _isReturnable( last );
            if ( r )
                _append( "__last__ = ( " , last );
            
            _add( last , state );
            
            if ( r )
                _append( " ) " , last );
            
            _append( ";" , last );
        }

        if ( useBrackets )
            _append( "\n}\n" , n );
    }

    String _getRegexpOptions( int options ){
        String s = "";
        if ( ( options & ReOptions.RE_OPTION_IGNORECASE  ) > 0 )
            s += "i";
        return s;
    }
    
    boolean _handleTurnary( IfNode ifn , State state ){
        if ( _badTurnaryNode( ifn.getThenBody() ) ){
            if ( D ) System.out.println( "bad turnary : " + ifn.getThenBody() );
            return false;
        }

        if ( _badTurnaryNode( ifn.getElseBody() ) ){
            if ( D ) System.out.println( "bad turnary : " + ifn.getElseBody() );
            return false;
        }

        _append( "( (" , ifn );
        _add( ifn.getCondition() , state );
        _append( " ) ? ( " , ifn );

        if ( ifn.getThenBody() == null )
            _append( "null" , ifn );
        else
            _add( ifn.getThenBody() , state );
        
        _append( " ) : ( " , ifn );
        
        if ( ifn.getElseBody() == null )
            _append( "null" , ifn );
        else
            _add( ifn.getElseBody() , state );

        _append( " ) ) " , ifn );
        
        return true;
        
    }

    boolean _badTurnaryNode( Node n ){
        if ( n == null )
            return false;
        
        if ( ! _isSingleStatement( n ) )
            return true;
        
        return false;
    }

    boolean _isReturnable( Node n ){
        
        //if ( _searchFor( n , ReturnNode.class ) != null ) return false;
        //if ( n instanceof ReturnNode ) return false;
        
        if ( n instanceof NewlineNode ) 
            n = n.childNodes().get(0);
        
        final boolean b = _isSingleStatement( n );
        if ( D ) System.out.println( "isReturnable: " + n + "\t" + b );
        return b;
    }
    
    boolean _isSingleStatement( Node n ){
        if ( n == null )
            return false;
        
        if ( n instanceof IfNode ){
            IfNode ifn = (IfNode)n;
            return
                _isSingleStatement( ifn.getElseBody() ) &&
                _isSingleStatement( ifn.getThenBody() );
        }
        
        if ( n instanceof NotNode 
             || n instanceof NewlineNode 
             )
            return _isSingleStatement( n.childNodes().get(0) );
        
        if ( n instanceof BinaryOperatorNode ){
            return true;
        }
        
        if ( n instanceof LocalAsgnNode )
            return _isSingleStatement( ((LocalAsgnNode)n).getValueNode() );

        if ( n instanceof DAsgnNode )
            return _isSingleStatement( ((DAsgnNode)n).getValueNode() );
        
        if ( n instanceof CallNode ||
             n instanceof FCallNode ||
             n instanceof VCallNode )
            return true;
        
        if ( n instanceof TrueNode || 
             n instanceof NilNode ||
             n instanceof MultipleAsgnNode ||
             n instanceof ArrayNode ||
             n instanceof DStrNode ||
             n instanceof FalseNode )
            return true;

        if ( n instanceof Match3Node ||
             n instanceof Match2Node )
            return true;
        
        if ( n instanceof BlockNode  
             || n instanceof DefnNode
             || n instanceof NewlineNode
             || n instanceof BeginNode
             || n instanceof BreakNode
             || n instanceof ZSuperNode
             || n instanceof NextNode
             || n instanceof RedoNode
             )
            return false;
        
        
        return n.childNodes() == null || n.childNodes().size() <= 1;
    }
    
    void _addRescueNode( RescueNode rn , State state , boolean fromBegin ){

        Node en = state._ensure;
        state._ensure = null;

        if ( ! fromBegin )
            _append( "(function(){" , rn );
        
        _append( "try {\n" , rn );
        _add( rn.getBodyNode() , state );
        if ( rn.getElseNode() != null )
            _add( rn.getElseNode() , state );
        _append( "\n}\n" , rn );
        
        RescueBodyNode rb = rn.getRescueNode();
        while ( rb != null ){
            
            final String name = "name" + (int)(Math.random() * 12312312);
            
            _append( "catch( " + name  , rb );
            
            _append( " if ! __risReturnThing( " + name + " )" , rb );
            if ( rb.getExceptionNodes() != null ){
                _append( " && " + Ruby.RUBY_RESCURE_INSTANCEOF + "( " + name + " , " , rb );
                _add( rb.getExceptionNodes() , state );
                _append( " ) " , rb );
            }
            _append( " ){\n" , rb );

            if ( ! fromBegin && _isReturnable( rb.getBodyNode() ) ) _append( JS_R , rn );
            _addBlock( rb.getBodyNode() , state );
            _append( "\n}\n" , rn );
            
            rb = rb.getOptRescueNode();
        }
        
        if ( en != null ){
            _append( "finally {\n" , en );
            _add( en , state );
            _append( "\n}\n" , en );
        }

        if ( ! fromBegin )
            _append( "}() )" , rn );

    }

    void _addLocal( String name , Node val , State state ){

        if ( _isSingleStatement( val ) ){
            _append( "scope.put( \"" + name + "\" , " , val );
            _add( val , state );
            _append( " , true )" , val );
            return;
        }
        
        _add( val , state );
        _append( "scope.put( \"" + name + "\" , __last__  , true )" , val );
        
    }

    void _addIterBlock( IterNode it , State state ){
        _append( "function(" , it );
        if ( it.getVarNode() != null ){
            final Node var = it.getVarNode();
            if ( var instanceof DAsgnNode )
                _append( ((DAsgnNode)var).getName() , it );
            else if ( var instanceof ConstDeclNode )
                _append( ((ConstDeclNode)var).getName() , it );
            else if ( var instanceof LocalAsgnNode )
                _append( ((LocalAsgnNode)var).getName() , it );
            else if ( var instanceof MultipleAsgnNode ){

                boolean first = true;
                for ( Node temp : ((MultipleAsgnNode)var).getHeadNode().childNodes() ){
                    
                    if ( ! first )
                        _append( " , " , temp );
                    first = false;
                    
                    _append( ((INameNode)temp).getName() , temp );
                }
            }
            else
                throw new RuntimeException( "don't know what to do with : " + var + " : " + var.getPosition() );
        }
        _append( " ){ \n" , it );
        if ( it.getBodyNode() instanceof NewlineNode && 
             _isSingleStatement( it.getBodyNode() ) )
            _append( "return " , it );
        _add( it.getBodyNode() , state.child() );
        _append( "\nreturn __last__;\n" , it );
        _append( " }" , it );
    }

    void _addClass( ClassNode cn , State state ){
        
        final String name = 
            ( state._className == null ? "" : state._className + "." ) 
            + cn.getCPath().getName();
        
        state = state.child();
        state._className = name;

        _assertType( cn.childNodes().get(0) , Colon2Node.class );
        
        state._classInit = _findClassInit( cn );
        
        // constructor
        if ( ! _isBuiltIn( name ) ){
            _append( name + " = " + Ruby.RUBY_DEFINE_CLASS + "( " + name + " , " , cn );
            
            if ( state._classInit == null ){
                _append( " function(){ if ( this.__proto__ && this.__proto__.constructor && this.__proto__ != this && ( arguments.length == 0 || arguments[arguments.length-1] != 1542143 ) ){ arguments.push( 1542143 ); this.__proto__.constructor.apply( this , arguments ); } }" , cn );
            }
            else {
                _append( " function" , state._classInit );
                if ( state._classInit.getArgsNode() == null || 
                     state._classInit.getArgsNode().getArgs() == null )
                    _append( "()" , state._classInit );
                else
                    _addArgs( state._classInit , state._classInit.getArgsNode().getArgs().childNodes() , state );
                _append( "{\n" , state._classInit );
                _add( state._classInit.getBodyNode() , state.child() );
                _append( "\n}\n" , state._classInit );
            }
            _append( " )\n" , cn );
            
            
            if ( cn.getSuperNode() != null ){
                _append( "\n" + name + ".prototype = new " , cn );
                _add( cn.getSuperNode() , state );
                _append( "();\n" , cn );
            }
        }
        
        for ( Node c : cn.childNodes() )
            _addClassPiece( c , state );
    }

    void _addModule( ModuleNode mn , State state ){
        
        final String name = 
            ( state._className == null ? "" : state._className + "." ) 
            + mn.getCPath().getName();
        
        state = state.child();
        state._className = name;
        state._classInit = null;
        state._module = true;

        _append( "if ( ! " + name + " ){ " + name + " = {}; }\n" , mn );
        
        for ( Node c : mn.childNodes() )
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

    Node _searchFor( final Node where , final Class toFind ){
        return _searchForBFS( where , new NodeMatcher(){
                public boolean matches( Node n ){
                    if ( n == null )
                        return false;
                    return toFind.isAssignableFrom( n.getClass() );
                }
            } );
    }
    
    Node _searchForBFS( final Node where , final NodeMatcher nm ){
        if ( where == null )
            return null;
        
        List<Node> toSearch = new LinkedList<Node>();
        toSearch.add( where );
        
        while ( toSearch.size() > 0 ){
            Node n = toSearch.remove(0);
            if ( nm.matches( n ) )
                return n;
            
            if ( n.childNodes() != null )
                toSearch.addAll( n.childNodes() );
        }
        
        return null;
    }

    interface NodeMatcher {
        boolean matches( Node n );
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
            _append( ";\n" , n );
            return;
        }
        
        if ( n instanceof MethodDefNode ){
            MethodDefNode dn = (MethodDefNode)n;
            if ( dn != state._classInit )
                _add( dn , state );
            return;
        }
        
        if ( n instanceof AliasNode ){
            AliasNode an = (AliasNode)n;
            _append( state._className + ".prototype." + _mangleFunctionName( an.getNewName() ) + " = " + 
                     state._className + ".prototype." + _mangleFunctionName( an.getOldName() ) + ";\n" , n );
            return;
        }

        if ( n instanceof ConstDeclNode ){
            ConstDeclNode cdn = (ConstDeclNode)n;
            _append( state._className + "." + cdn.getName() + " = " , n );
            _add( cdn.childNodes().get(0) , state );
            _append( ";\n" , n );
            return;
        }

        if ( n instanceof ClassVarDeclNode ){
            ClassVarDeclNode dn = (ClassVarDeclNode)n;
            _append( state._className + "." + dn.getName().substring(2) + " = " , dn );
            _add( dn.getValueNode() , state );
            return;
        }

        if ( n instanceof SClassNode ){
            SClassNode s = (SClassNode)n;
            _assertType( s.getReceiverNode() , SelfNode.class );
            _add( s.getBodyNode() , state );
            return;
        }
        
        if ( n instanceof BeginNode ||
             n instanceof RescueNode ||
             n instanceof FCallNode || 
             n instanceof CallNode || 
             n instanceof VCallNode ||
             n instanceof ModuleNode ||
             n instanceof IfNode ||
             n instanceof LocalAsgnNode ||
             n instanceof AttrAssignNode ||
             n instanceof OpAsgnOrNode ||
             n instanceof ClassNode ){
            _add( n , state );
            return;
        }
        
        _print( 0 , n );
        throw new RuntimeException( "don't know about class piece : " + n.getClass() + " " + n.getPosition() );
        
    }

    void _addCall( CallNode call , State state ){
        if ( call.getName().equals( "<=>" ) ){
            _append( "Math.posOrNeg( " , call );
            _add( call.childNodes().get(0) , state );
            _append( "-" , call );
            _add( call.childNodes().get(1) , state );
            _append( " ) " , call );
            return;
        }

        if ( _isOperator( call ) ){
            _addArgs( call , call.childNodes() , state , " " + call.getName() + " " );
            return;
        }

        if ( D ){
            System.err.println( "CallNode : " + call.getName() );
            System.err.println( "\t iter : " + call.getIterNode() );
            System.err.println( "\t self : " + call.getReceiverNode() );
            System.err.println( "\t args : " + call.getArgsNode() );
        }

        if ( call.getName().equals( "new" ) ){
            _append( Ruby.RUBY_NEW + "( " , call );
            _add( call.getReceiverNode() , state );

            if ( call.getArgsNode() != null && 
                 call.getArgsNode().childNodes() != null ){

                for ( Node temp : call.getArgsNode().childNodes() ){
                    _append( " , " , call );
                    _add( temp , state );
                }

            }

            _append( " ) " , call );
            return;
        }
        
        Node self = call.getReceiverNode();
        Node args = call.getArgsNode();
        Node iter = call.getIterNode();
        
        if ( self == null )
            throw new RuntimeException("self should never be null" );
        
        // no params
        if ( ( args == null || args.childNodes().size() == 0 ) 
             && iter == null ){
            _append( Ruby.RUBY_CV_CALL + "( " , call);
            state.appendClassNameIfNeeded( self );
            _add( self , state );
            _append( " , " , call );
            _append( "\"" + _getFuncName( call ) + "\"" , call );
            _append( " ) " , call );
            return;
        }
        
        state.appendClassNameIfNeeded( self );
        _add( self , state );
        _append( "." + _getFuncName( call ) , call );
        _append( "(" , call );        
        
        int num = 0;
        
        if ( args != null ){
            for ( Node temp : args.childNodes() ){
                if ( num++ > 0 )
                    _append( " , " , call );
                _add( temp , state );
            }
        }

        if ( iter != null ){
            if ( num++ > 0 )
                _append( " , " , call );
            _add( iter , state );
        }
            
        _append( ")" , call );

    }

    // ---- add args ----

    void _addArgs( final Node where , List<Node> lst , final State state ){
        _addArgs( where , lst , state , " , " );
    }

    void _addArgs( final Node where , final List<Node> lst , final State state , final String sep ){
        _append( "(" , where );

        if ( lst != null )
            for ( int i=0; i<lst.size(); i++ )
                _addOneArg( lst.get( i ) , state , i > 0 ? sep : "" );

        _append( ")" , where );
    }

    void _addOneArg( final Node arg , final State state , final String sep ){
        _append( sep , arg );
        _add( arg , state );
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
            throw new RuntimeException( "need exactly 1 child : " + n.getPosition() );
        
    }
    
    void _assertType( Node n , Class c ){
        _assertType( n , c , null );
    }
    
    void _assertType( Node n , Class c , Node toDisplay ){
        if ( n != null && c.isAssignableFrom( n.getClass() ) )
            return;
        if ( toDisplay != null )
            _print( 0 , toDisplay );
        
        String msg = n + " is not an instanceof " + c;
        {
            Node line = n != null ? n : toDisplay;
            if ( line != null )
                msg += " line: " + line.getPosition();
        }
        throw new RuntimeException( msg );
    }

    // ---  utility stuff ---

    void _print( int space , Node n ){
        for ( int i=0; i<space; i++ )
            System.out.print( " " );
        
        System.out.print( n );
        if ( n instanceof INameNode )
            System.out.print( " " + ((INameNode)n).getName() + " " );
        System.out.println();
        
        for ( Node c : n.childNodes() )
            _print( space + 1 , c );
        
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

    boolean _isBuiltIn( String name ){
        return name.equals( "String" );
    }

    boolean _isOperator( CallNode node ){
        return _operatorNames.contains( node.getName() );
    }

    static String _getFuncName( INameNode node ){
        String name = node.getName();
        
        if ( name.equals( "include" ) )
            return Ruby.RUBY_INCLUDE;
        
        return _mangleFunctionName( name );
    }
    
    static String _getName( INameNode n ){
        return _mangleVarName( n.getName() );
    }

    static String _mangleVarName( INameNode n ){
        return _mangleVarName( n.getName() );
    }

    static String _mangleVarName( String name ){
        return _mangleName( name );
    }

    static String _mangleFunctionName( String name ){

        if ( name.equals( "puts" ) )
            return "__puts__";

        {
            final String blah = Ruby._nameMapping.get( name );
            if ( blah != null )
                return blah;
        }
        
        return _mangleName( name );
    }
    
    static String _mangleName( String name ){

        for ( int i=0; i<_functionReplacements.length; i++){
            String v[] = _functionReplacements[i];
            if ( name.contains( v[0] ) )
                name = name.replaceAll( Pattern.quote( v[0] ) , v[1] );
        }
        
        if ( _specialNames.contains( name ) )
            return "__" + name;

        return name;
    }
    static String[][] _functionReplacements = new String[][]{ 
        new String[]{ "~" , "_t_" } ,
        new String[]{ "!" , "_ex_" } ,
        new String[]{ "=" , "_eq_" } ,
        new String[]{ ">" , "_gt_" } ,
        new String[]{ "<" , "_lt_" } ,
        new String[]{ "[" , "_lb_" } ,
        new String[]{ "]" , "_rb_" } ,
        new String[]{ "?" , "_q_" } 
    };

    public String getJSSource(){
        String js = _js.toString();
        if ( OUTPUT_FILE ) {
            File blah = new File( "/tmp/jxp/ruby/" );
            blah.mkdirs();
            blah = new File( blah , _name.replaceAll( "^.*/" , "/" ) );
            try {
                FileOutputStream fout = new FileOutputStream( blah );
                fout.write( js.getBytes() );
                fout.close();
            }
            catch ( IOException ioe ){
                ioe.printStackTrace();
            }
        }
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
        
        Convert c = new Convert( _name , js , false , Language.RUBY );
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
            State s = new State( this );
            s._lastClass = _className;
            if ( s._lastClass == null )
                s._lastClass = _lastClass;
            return s;
        }
        
        boolean appendClassNameIfNeeded( Node n ){
            if ( _className == null )
                return false;
            
            if ( ! ( n instanceof INameNode  ) )
                return false;
            
            if ( n instanceof CallNode )
                return appendClassNameIfNeeded( ((CallNode)n).getReceiverNode() );
            
            _append( _className + "." , n );
            return true;
        }

        final State _parent;
        String _lastClass;
        String _className;
        DefnNode _classInit;
        boolean _module = false;
        
        int _whileCount = 0;
        Node _ensure;
        String _curMethod;
    }

    void _append( String s , Node where ){

        for ( int i=0; i<s.length(); i++ ){
            final char c = s.charAt( i );

            _lineMapping.put( _line , where.getPosition().getStartLine() );

            if ( s.charAt( i ) != '\n' ){
                _js.append( c );
                continue;
            }
            
            if ( APPEND_LINES )
                _js.append( "\t\t// " + where.getPosition().getStartLine() + " -->> " + _line  );
            
            _js.append( "\n" );
            _line++;
        }
    }

    private int _line = 1;
    private Map<Integer,Integer> _lineMapping = new TreeMap<Integer,Integer>();

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
        _operatorNames.add( "==" );
        _operatorNames.add( "|" );
        _operatorNames.add( "&" );
        _operatorNames.add( "%" );
    }

    static final Set<String> _specialNames = new HashSet<String>();
    static {
        _specialNames.add( "send" );
        _specialNames.add( "include" );
        _specialNames.add( "extend" );
        _specialNames.add( "default" );
        _specialNames.add( "delete" );

        _specialNames.add( "char" );
        _specialNames.add( "int" );
        _specialNames.add( "float" );
        _specialNames.add( "double" );
        _specialNames.add( "short" );
        _specialNames.add( "var" );
    }

}
