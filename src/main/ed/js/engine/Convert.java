// Convert.java

/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.js.engine;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.twmacinta.util.*;

import ed.ext.org.mozilla.javascript.*;

import ed.js.*;
import ed.io.*;
import ed.lang.*;
import ed.util.*;

public class Convert {

    static boolean DJS = Boolean.getBoolean( "DEBUG.JS" );
    final boolean D;
    public static final String DEFAULT_PACKAGE = "ed.js.gen";

    public static JSFunction makeAnon( String code ){
        return makeAnon( code , false );
    }

    public static JSFunction makeAnon( String code , boolean forceEval ){
        try {

            final String nice = code.trim();
            final String name = "anon" + Math.random();

            if ( nice.startsWith( "function" ) &&
                 nice.endsWith( "}" ) ){

                Convert c = new Convert( name , code , true );
                JSFunction func = c.get();
                Scope s = Scope.newGlobal().child();
                s.setGlobal( true );

                func.call( s );

                String keyToUse = null;
                int numKeys = 0;

                for ( String key : s.keySet() ){
                    if ( key.equals( "arguments" ) )
                        continue;

                    keyToUse = key;
                    numKeys++;
                }


                if ( numKeys == 1 ){
                    Object val = s.get( keyToUse );
                    if ( val instanceof JSFunction ){
                        JSFunction f = (JSFunction)val;
                        f.setUsePassedInScope( forceEval );
                        return f;
                    }
                }

            }

            Convert c = new Convert( name , nice , forceEval );
            return c.get();
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "should be impossible" , ioe );
        }
    }

    public Convert( File sourceFile )
        throws IOException {
        this( sourceFile.getAbsolutePath() , StreamUtil.readFully( sourceFile , "UTF-8" ) );
    }

    public Convert( String name , String source )
        throws IOException {

        this(name, source, false);
    }

    public Convert( String name , String source, boolean invokedFromEval)
        throws IOException {
        this( name , source , invokedFromEval , Language.JS );
    }

    public Convert( String name , String source, boolean invokedFromEval , Language sourceLanguage )
        throws IOException {

        D = DJS
            && ! name.contains( "src/main/ed/lang" )
            && ! name.contains( "src_main_ed_lang" )
            ;

        _invokedFromEval = invokedFromEval;
        _sourceLanguage = sourceLanguage;

        _name = name;
        _source = source;

        _className = cleanName( _name ) + _getNumForClass( _name , _source );
        _fullClassName = _package + "." + _className;
        _random = _random( _fullClassName );
        _id = _random.nextInt();
        _scriptInfo = new ScriptInfo( _name , _fullClassName , _sourceLanguage , this );

        CompilerEnvirons ce = new CompilerEnvirons();

        Parser p = new Parser( ce , ce.getErrorReporter() );

        ScriptOrFnNode theNode = null;
        try {
            theNode = p.parse( _source , _name , 0 );
        }
        catch ( ed.ext.org.mozilla.javascript.EvaluatorException ee ){
            throw JSCompileException.create( ee );
        }
        _encodedSource = p.getEncodedSource();
        init( theNode );
    }

    public static String cleanName( String name ){
        StringBuilder buf = new StringBuilder( name.length() + 5 );

        for ( int i=0; i<name.length(); i++ ){

            final char c = name.charAt(i);

            if ( Character.isLetter( c ) || Character.isDigit( c ) ){
                buf.append( c );
                continue;
            }

            if ( buf.length() == 0 )
                continue;

            buf.append( "_" );
        }
        return buf.toString();
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

        _setLineNumbers( sn , sn );
        _addFunctionNodes( sn , state );

        if ( D ) System.out.println( "***************" );

        Node n = sn.getFirstChild();

        if ( n.getType() == Token.EXPR_RESULT ){
            Node n2 = n.getFirstChild();
            if ( n2 != null && n2.getType() == Token.NAME ){
                if ( n2.getString().equals( "loadonce" ) ){
                    _loadOnce = true;
                }
            }
        }

        String whyRasReturn = null;

        while ( n != null ){
            if ( n.getType() != Token.FUNCTION ){

                if ( n.getNext() == null ){

                    if ( n.getType() == Token.EXPR_RESULT ){
                        _append( "return " , n );
                        _hasReturn = true;
                        whyRasReturn = "EXPR_RESULT";
                    }

                    if ( n.getType() == Token.RETURN ){
                        _hasReturn = true;
                        whyRasReturn = "RETURN";
                    }
                }


                _add( n , sn , state );

                _append( "\n" , n );
            }

            n = n.getNext();
        }

        if ( ! _hasReturn ) {
            _append( "return null; /* null added at end */" , sn );
        }
        else {
            _append( "/* no return b/c : " + whyRasReturn + " */" , sn );
            int end = _mainJavaCode.length() - 1;
            boolean alreadyHaveOne = false;
            for ( ; end >= 0; end-- ){
                char c = _mainJavaCode.charAt( end );

                if ( Character.isWhitespace( c ) )
                    continue;

                if ( c == ';' ){
                    if ( ! alreadyHaveOne ){
                        alreadyHaveOne = true;
                        continue;
                    }

                    _mainJavaCode.setLength( end + 1 );
                }

                break;

            }

        }
    }

    private void _add( Node n , State state ){
        _add( n , null , state );
    }

    private void _add( Node n , ScriptOrFnNode sn , State state ){
        switch ( n.getType() ){

        case Token.TYPEOF:
            _append( "JS_typeof( " , n );
            _assertOne( n );
            _add( n.getFirstChild() , state );
            _append( " ) " , n );
            break;

        case Token.TYPEOFNAME:
            _append( "JS_typeof( " , n );
            if ( state.hasSymbol( n.getString() ) )
                _append( n.getString() , n );
            else
                _append( "scope.get( \"" + n.getString() + "\" )" , n );
            _append( " ) " , n );
            break;

        case Token.REGEXP:
            int myId = _regex.size();
            ScriptOrFnNode parent  = _nodeToSOR.get( n );
            if ( parent == null )
                throw new RuntimeException( "how is parent null : " + n.hashCode() );
            int rId = n.getIntProp( Node.REGEXP_PROP , -1 );

            _regex.add( new Pair<String,String>( parent.getRegexpString( rId ) , parent.getRegexpFlags( rId ) ) );
            _append( " _regex(" + myId + ") " , n );
            break;

        case Token.ARRAYLIT:
            {
                _append( "( JSArray.create( " , n );
                Node c = n.getFirstChild();
                while ( c != null ){
                    if ( c != n.getFirstChild() )
                        _append( " , " , n );
                    _add( c , state );
                    c = c.getNext();
                }
                _append( " ) ) " , n );
            }
            break;

        case Token.OBJECTLIT:
            {
                _append( "JS_buildLiteralObject( new String[]{ " , n );
                boolean first = true;
                Node c = n.getFirstChild();
                for ( Object id : (Object[])n.getProp( Node.OBJECT_IDS_PROP ) ){
                    if ( first )
                        first = false;
                    else
                        _append( " , " , n );

                    String name = id.toString();

                    if ( c.getType() == Token.GET )
                        name = JSObjectBase.getterName( name );
                    else if ( c.getType() == Token.SET )
                        name = JSObjectBase.setterName( name );

                    _append( getStringCode( name ) + ".toString()" , n );

                    c = c.getNext();
                }
                _append( " } " , n );

                c = n.getFirstChild();
                while ( c != null ){
                    _append( " , " , n );
                    _add( c , state  );
                    c = c.getNext();
                }
                _append( " ) " , n );
            }
            break;

        case Token.NEW:
            _append( "scope.clearThisNew( " , n );
            _addCall( n , state , true );
            _append( " ) " , n );
            break;

        case Token.THIS:
            _append( "passedIn.getThis()" , n );
            break;

        case Token.INC:
        case Token.DEC:
            _assertOne( n );

            Node tempChild = n.getFirstChild();

            if ( ( tempChild.getType() == Token.NAME || tempChild.getType() == Token.GETVAR ) && state.useLocalVariable( tempChild.getString() ) ){
                if ( ! state.isNumber( tempChild.getString() ) )
                    throw new RuntimeException( "can't increment local variable : " + tempChild.getString()  );
                String str = n.getType() == Token.INC ? "++ " : "-- ";
                _append( tempChild.getString() + str , n );
            }
            else {
                _append( "JS_inc( " , n );
                _createRef( n.getFirstChild() , state );
                _append( " , " , n );
                _append( String.valueOf( ( n.getIntProp( Node.INCRDECR_PROP , 0 ) & Node.POST_FLAG ) > 0 ) , n );
                _append( " , " , n );
                _append( String.valueOf( n.getType() == Token.INC ? 1 : -1 ) , n );
                _append( ")" , n );
            }
            break;

        case Token.USE_STACK:
            _append( "__tempObject.get( " + state._tempOpNames.pop() + " ) "  , n );
            break;

        case Token.SETPROP_OP:
        case Token.SETELEM_OP:
            Node theOp = n.getFirstChild().getNext().getNext();
            if ( ( theOp.getType() == Token.ADD ||
                   theOp.getType() == Token.SUB ||
                   theOp.getType() == Token.MUL ||
                   theOp.getType() == Token.DIV )
                 &&
                 ( theOp.getFirstChild().getType() == Token.USE_STACK ||
                   theOp.getFirstChild().getNext().getType() == Token.USE_STACK ) ){
                _append( "\n" , n );
                _append( "JS_setDefferedOp( (JSObject) " , n );
                _add( n.getFirstChild() , state );
                _append( " , " , n );
                _add( n.getFirstChild().getNext() , state );
                _append( " , " , n );
                _add( theOp.getFirstChild().getType() == Token.USE_STACK ?
                      theOp.getFirstChild().getNext() :
                      theOp.getFirstChild() ,
                      state );
                _append( " , " + theOp.getType() , n );
                _append( " \n ) \n" , n );
                break;
            }

            _append( "\n { \n" , n );

            _append( "JSObject __tempObject = (JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( ";\n" , n );


            String tempName = "__temp" + _rand();
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

            _addAsJSObject( n.getFirstChild() , state );
            _append( ".set( " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " ) " , n );
            break;

        case Token.GETPROPNOWARN:
        case Token.GETPROP:
        case Token.GETELEM:
            _addAsJSObject( n.getFirstChild() , state );
            _append( ".get( " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " )" , n );
            break;

        case Token.SET_REF:
            Node fc = n.getFirstChild();
            if( fc.getType() != Token.REF_SPECIAL && fc.getType() != Token.REF_MEMBER )
                throw new RuntimeException( "token is of type "+Token.name(fc.getType())+", should be of type REF_SPECIAL or REF_MEMBER.");

            _addAsJSObject( n.getFirstChild().getFirstChild() , state );
            _append( ".set( " , n );
            _add( fc , state );
            _append( " , " , n );
            _add( fc.getNext() , state );
            _append( " )" , n );
            break;

        case Token.GET_REF:
            _addAsJSObject( n.getFirstChild().getFirstChild() , state );
            _append( ".get( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) ", n );
            break;

        case Token.REF_SPECIAL :
            _append( "\"" + n.getProp( Node.NAME_PROP ).toString() + "\"" , n );
            break;

        case Token.REF_MEMBER :
            _append( "\"" , n );
            final int memberTypeFlags = n.getIntProp(Node.MEMBER_TYPE_PROP, 0);
            if ( ( memberTypeFlags & Node.DESCENDANTS_FLAG ) != 0 )
                _append( ".." , n );
            if ( ( memberTypeFlags & Node.ATTRIBUTE_FLAG ) != 0 )
                _append( "@" , n );

            _append( n.getFirstChild().getNext().getString() , n );
            _append( "\"" , n );
            break;

        case Token.REF_NS_MEMBER :
            if( ( n.getIntProp(Node.MEMBER_TYPE_PROP,0) & Node.ATTRIBUTE_FLAG ) != 0 ) {
                _append( "\"@\" + " , n );
            }
            _add( n.getFirstChild().getNext() , state );
            _append( " + \"::\" + ", n );
            _add( n.getFirstChild().getNext().getNext() , state );
            break;

        case Token.REF_NAME :
        case Token.ESCXMLTEXT :
        case Token.ESCXMLATTR :
            _add( n.getFirstChild(), state );
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
            double d = n.getDouble();
            String temp = String.valueOf( d );
            if ( temp.endsWith( ".0" ) ||
                 JSNumericFunctions.couldBeInt( d ) )
                temp = String.valueOf( (int)d );
            _append( "JSNumber.self( " + temp + ")" , n );
            break;
        case Token.STRING:
            final String theString = n.getString();
            _append( getStringCode( theString ) , n );
            break;
        case Token.TRUE:
            _append( " true " , n );
            break;
        case Token.FALSE:
            _append( " false " , n );
            break;
        case Token.NULL:
            _append( " null " , n );
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
            if ( state.useLocalVariable( n.getString() ) && state.hasSymbol( n.getString() ) )
                _append( n.getString() , n );
            else
                _append( "scope.get( \"" + n.getString() + "\" )" , n );
            break;
        case Token.SETVAR:
            final String foo = n.getFirstChild().getString();
            if ( state.useLocalVariable( foo ) ){
                if ( ! state.hasSymbol( foo ) )
                    throw new RuntimeException( "something is wrong" );
                if ( ! state.isPrimitive( foo ) )
                    _append( "JSInternalFunctions.self ( " , n );
                _append( foo + " = " , n );
                _add( n.getFirstChild().getNext() , state );
                if ( ! state.isPrimitive( foo ) )
                    _append( " )\n" , n );
            }
            else {
                _setVar( foo ,
                         n.getFirstChild().getNext() ,
                         state , true );
            }
            break;

        case Token.SETNAME:
            _addSet( n , state );
            break;

        case Token.GET:
            _addFunction( n.getFirstChild() , state );
            break;
        case Token.SET:
            _addFunction( n.getFirstChild() , state );
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
            boolean last = state._depth <= 1 && n.getNext() == null;
            if ( ! last )
                _append( "if ( true ) { " , n );
            _append( "return " , n );
            if ( n.getFirstChild() != null ){
                _assertOne( n );
                _add( n.getFirstChild() , state );
            }
            else {
                _append( " null " , n );
            }
            _append( "; /* explicit return */" , n );
            if ( ! last )
                _append( "}" , n );
            _append( "\n" , n );
            break;

        case Token.BITNOT:
            _assertOne( n );
            _append( "JS_bitnot( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) " , n );
            break;


        case Token.HOOK:
            _append( " JSInternalFunctions.self( JS_evalToBool( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) ? ( " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) : ( " , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " ) ) " , n );

            break;

        case Token.POS:
            _assertOne( n );
            _add( n.getFirstChild() , state );
            break;


        case Token.ADD:
            if ( state.isNumberAndLocal( n.getFirstChild() ) &&
                 state.isNumberAndLocal( n.getFirstChild().getNext() ) ){
                _append( "(" , n );
                _add( n.getFirstChild() , state );
                _append( " + " , n );
                _add( n.getFirstChild().getNext() , state );
                _append( ")" , n );
                break;
            }

        case Token.NE:
        case Token.MUL:
        case Token.DIV:
        case Token.SUB:
        case Token.EQ:
        case Token.SHEQ:
        case Token.SHNE:
        case Token.GE:
        case Token.LE:
        case Token.LT:
        case Token.GT:
        case Token.BITOR:
        case Token.BITAND:
        case Token.BITXOR:
        case Token.URSH:
        case Token.RSH:
        case Token.LSH:
        case Token.MOD:

            if ( n.getType() == Token.NE )
                _append( " ! " , n );

            _append( "JS_" , n );
            String fooooo = _2ThingThings.get( n.getType() );
            if ( fooooo == null )
                throw new RuntimeException( "noting for : " + n );
            _append( fooooo , n );
            _append( "\n( " , n );
            _add( n.getFirstChild() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " )\n " , n );
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
            _append( "if ( true ) continue " + n.getString() + ";\n" , n );
            break;

        case Token.WHILE:
            _append( "while( false || JS_evalToBool( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) ){ " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " }\n" , n );
            break;
        case Token.FOR:
            _addFor( n , state );

            break;

        case Token.TARGET:
            break;

        case Token.NOT:
            _assertOne( n );
            _append( " JS_not( " , n );
            _add( n.getFirstChild() , state );
            _append( " ) " , n );
            break;

        case Token.AND:
            /*
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
            */
        case Token.OR:
            Node cc = n.getFirstChild();

            if ( cc.getNext() == null )
                throw new RuntimeException( "what?" );
            if ( cc.getNext().getNext() != null )
                throw new RuntimeException( "what?" );

            String mod = n.getType() == Token.AND ? "and" : "or";
            _append( "JSInternalFunctions.self( scope." + mod + "Save( " , n );
            _add( cc , state );
            _append( " ) ? scope.get" + mod + "Save() : ( " , n );
            _add( cc.getNext() , state );
            _append( " ) ) " , n );

            break;

        case Token.LOCAL_BLOCK:
            _assertOne( n );
            if ( n.getFirstChild().getType() != Token.TRY )
                throw new RuntimeException("only know about LOCAL_BLOCK with try" );
            _addTry( n.getFirstChild() , state );
            break;

        case Token.JSR:
        case Token.RETURN_RESULT:
            // these are handled in block
            break;

        case Token.THROW:
            _append( "if ( true ) _throw( " , n );
            _add( n.getFirstChild() , state );
            _append( " ); " , n );
            break;
        case Token.INSTANCEOF:
            _append( "JS_instanceof( " , n );
            _add( n.getFirstChild() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) " , n );
            if ( n.getFirstChild().getNext().getNext() != null )
                throw new RuntimeException( "something is wrong" );
            break;
        case Token.DELPROP:
            _addAsJSObject( n.getFirstChild() , state );
            _append( ".removeField( "  , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) " , n );
            break;

        case Token.DEL_REF:
            _addAsJSObject( n.getFirstChild().getFirstChild() , state );
            _append( ".removeField( " , n );
            _add( n.getFirstChild() , state );
            _append( " )" , n );
            break;

        case Token.SWITCH:
            _addSwitch( n , state );
            break;

        case Token.COMMA:
            _append( "JS_comma( " , n );
            boolean first = true;
            Node inner = n.getFirstChild();
            while ( inner != null ){
                if ( first )
                    first = false;
                else
                    _append( " , " , n );
                _append( "\n ( " , n );
                _add( inner , state );
                _append( " )\n " , n );
                inner = inner.getNext();
            }
            _append( " ) " , n );
            break;

        case Token.IN:
            _addAsJSObject( n.getFirstChild().getNext() , state );
            _append( ".containsKey( " , n );
            _add( n.getFirstChild() , state );
            _append( ".toString() ) " , n  );
            break;

        case Token.NEG:
            _append( "JS_mul( -1 , " , n );
            _add( n.getFirstChild() , state );
            _append( " )" , n );
            break;

        case Token.ENTERWITH:
            _append( "scope.enterWith( (JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( " );" , n );
            break;

        case Token.LEAVEWITH:
            _append( "scope.leaveWith();" , n );
            break;

        case Token.WITH:
            _add( n.getFirstChild() , state );
            break;

        case Token.DOTQUERY:

            _addAsJSObject( n.getFirstChild() , state );
            _append( ".get( new ed.js.e4x.Query( " , n );

            Node n2 = n.getFirstChild().getNext();
            switch( n2.getFirstChild().getType() ) {
            case Token.GET_REF :
                _append( "\"@\" + " , n );
                _add( n2.getFirstChild().getFirstChild() , state );
                break;
            case Token.NAME :
                _append( "\"" + n2.getFirstChild().getString() + "\"" , n );
                break;
            }

            _append( " , " , n );
            _add( n2.getFirstChild().getNext() , state );
            _append( " + \"\" , " , n );

            String comp = Token.name( n2.getType() );
            _append( "\"" + comp + "\" ) ) " , n  );

            break;

        case Token.DEFAULTNAMESPACE :
            _append( "((ed.js.e4x.ENode.Cons)scope.get( \"XML\")).setAndGetDefaultNamespace( ", n );
            _add( n.getFirstChild(), state );
            _append(")", n );
            break;

        default:
            Debug.printTree( n , 0 );
            throw new RuntimeException( "can't handle : " + n.getType() + ":" + Token.name( n.getType() ) + ":" + n.getClass().getName() + " line no : " + n.getLineno() );
        }

    }

    private void _addAsJSObject( Node n , State state ){
        if ( n.getType() == Token.NUMBER ){
            _append( "(new JSNumber( " + n.getDouble() + "))" , n );
            return;
        }
        _append( "JS_toJSObject( " , n );
        _add( n , state );
        _append( ")" , n );
    }

    private void _addDotQuery( Node n , State state ){
        _append( "(new ed.js.e4x.E4X.Query(" , n );

        String s = Token.name( n.getType() );
        {
            Node t = n.getFirstChild();
            switch ( t.getType() ){
            case Token.GET_REF:
                _append( "\"@\" + " , n );
                _add( t.getFirstChild().getFirstChild() , state );
                break;
            default:
                throw new RuntimeException( "don't know how to handle " + Token.name( t.getType() ) + " in a DOTQUERY" );
            }

        }

        _append( " , " , n );
        _add( n.getFirstChild().getNext() , state );
        _append( "))" , n );
    }

    private void _addSwitch( Node n , State state ){
        _assertType( n , Token.SWITCH );

        String ft = "ft" + _rand();
        String val = "val" + _rand();
        _append( "boolean " + ft + " = false;\n" , n );
        _append( "do { \n " , n );
        _append( " if ( false ) break; \n" , n );

        Node caseArea = n.getFirstChild();
        _append( "Object " + val + " = " , n );
        _add( caseArea , state );
        _append( " ; \n " , n );

        n = n.getNext();
        _assertType( n , Token.GOTO ); // this is default ?
        n = n.getNext().getNext();

        caseArea = caseArea.getNext();
        while ( caseArea != null ){
            _append( "if ( " + ft + " || JS_eq( " + val + " , " , caseArea );
            _add( caseArea.getFirstChild() , state );
            _append( " ) ){\n " + ft + " = true; \n " , caseArea );

            _assertType( n , Token.BLOCK );
            _add( n , state );
            n = n.getNext().getNext();

            _append( " } \n " , caseArea );
            caseArea = caseArea.getNext();
        }

        if ( n != null && n.getType() == Token.BLOCK ){
            _add( n , state );
        }

        _append(" } while ( false );" , n );
    }

    private void _createRef( Node n , State state ){

        if ( n.getType() == Token.NAME || n.getType() == Token.GETVAR ){

            if ( state.useLocalVariable( n.getString() ) )
                throw new RuntimeException( "can't create a JSRef from a local variable : " + n.getString() );

            _append( " new JSRef( scope , null , " , n );
            _append( "\"" + n.getString() + "\"" , n );
            _append( " ) " , n );
            return;
        }

        if ( n.getType() == Token.GETPROP ||
             n.getType() == Token.GETELEM ){
            _append( " new JSRef( scope , (JSObject)" , n );
            _add( n.getFirstChild() , state );
            _append( " , " , n );
            _add( n.getFirstChild().getNext() , state );
            _append( " ) " , n );
            return;
        }

        throw new RuntimeException( "can't handle" );
    }

    private void _addTry( Node n , State state ){
        _assertType( n , Token.TRY );

        Node mainBlock = n.getFirstChild();
        _assertType( mainBlock , Token.BLOCK );

        _append( "try { \n " , n );
        _add( mainBlock , state );
        _append( " \n } \n " , n );

        n = mainBlock.getNext();

        final String num  = _rand();
        final String javaEName = "javaEEE" + num;
        final String javaName = "javaEEEO" + num;

        while ( n != null ){

            if ( n.getType() == Token.FINALLY ){
                _assertType( n.getFirstChild() , Token.BLOCK );
                _append( "finally { \n" , n );
                _add( n.getFirstChild() , state );
                _append( " \n } \n " , n );
                n = n.getNext();
                continue;
            }

            if ( n.getType() == Token.LOCAL_BLOCK &&
                 n.getFirstChild().getType() == Token.CATCH_SCOPE ){

                _append( " \n catch ( Exception " + javaEName + " ){ \n " , n );
                _append( " \n Object " + javaName + " = ( " + javaEName + " instanceof JSException ) ? " +
                         "  ((JSException)" + javaEName + ").getObject() : " + javaEName + " ; \n" , n );

                _append( "try { scope.pushException( " + javaEName + " ); \n" , n );

                Node catchScope = n.getFirstChild();

                while ( catchScope != null ){

                    final Node c = catchScope;
                    if ( c.getType() != Token.CATCH_SCOPE )
                        break;

                    Node b = c.getNext();
                    _assertType( b , Token.BLOCK );
                    _assertType( b.getFirstChild() , Token.ENTERWITH );
                    _assertType( b.getFirstChild().getNext() , Token.WITH );

                    b = b.getFirstChild().getNext().getFirstChild();
                    _assertType( b , Token.BLOCK );

                    String jsName = c.getFirstChild().getString();

                    _append( " scope.put( \"" + jsName + "\" , " + javaName + " , true ); " , c );

                    b = b.getFirstChild();

                    boolean isIF = b.getType() == Token.IFNE;

                    if ( isIF ){
                        _append( "\n if ( " + javaEName + " != null && JS_evalToBool( " , b );
                        _add( b.getFirstChild() , state );
                        _append( " ) ){ \n " , b  );
                        b = b.getNext().getFirstChild();
                    }

                    while ( b != null ){
                        if ( b.getType() == Token.LEAVEWITH )
                            break;
                        _add( b , state );
                        b = b.getNext();
                    }

                    _append( "if ( true ) " + javaEName + " = null ;\n" , b );

                    if ( isIF ){
                        _append( "\n } \n " , b );
                    }

                    catchScope = catchScope.getNext().getNext();
                }

                _append( "if ( " + javaEName + " != null ){ if ( " + javaEName + " instanceof RuntimeException ){ throw (RuntimeException)" + javaEName + ";} throw new JSException( " + javaEName + ");}\n" , n );

                _append( " } finally { scope.popException(); } " , n );
                _append( "\n } \n " , n ); // ends catch

                n = n.getNext();
                continue;
            }

            if ( n.getType() == Token.GOTO ||
                 n.getType() == Token.TARGET ||
                 n.getType() == Token.JSR ){
                n = n.getNext();
                continue;
            }

            if ( n.getType() == Token.RETHROW ){
                //_append( "\nthrow " + javaEName + ";\n" , n );
                n = n.getNext();
                continue;
            }

            throw new RuntimeException( "what : " + Token.name( n.getType() ) );
        }
    }

    private void _addFor( Node n , State state ){
        _assertType( n , Token.FOR );

        final int numChildren = countChildren( n );
        if ( numChildren == 4 ){
            _append( "\n for ( " , n );

            if ( n.getFirstChild().getType() == Token.BLOCK ){

                Node temp = n.getFirstChild().getFirstChild();
                while ( temp != null ){

                    if ( temp.getType() == Token.EXPR_VOID )
                        _add( temp.getFirstChild() , state );
                    else
                        _add( temp , state );

                    temp = temp.getNext();
                    if ( temp != null )
                        _append( " , " , n );
                }
            }
            else if ( n.getFirstChild().getType() != Token.EMPTY ) {
                _add( n.getFirstChild() , state );
            }

            _append( " ; \n JS_evalToBool( " , n );
            if( n.getFirstChild().getNext().getType() == Token.EMPTY ) {
                _append( "true" , n );
            }
            else {
                _add( n.getFirstChild().getNext() , state );
            }
            _append( " ) ; \n" , n );
            _add( n.getFirstChild().getNext().getNext() , state );
            _append( " )\n " , n );
            _append( " { \n " , n );
            _add( n.getFirstChild().getNext().getNext().getNext() , state );
            _append( " } \n " , n );
        }
        else if ( numChildren == 3 ){
            String name = n.getFirstChild().getString();
            String tempName = name + "TEMP";

            if( n.getFirstChild().getFirstChild() != null &&
                n.getFirstChild().getFirstChild().getType() == Token.ENUM_INIT_VALUES ) {
                _append( "\n for ( Object " , n );
                _append( tempName , n );
                _append( " : JSInternalFunctions.JS_collForForEach( ", n );
                _add( n.getFirstChild().getNext() , state );
                _append( " ) ){\n " , n );

                if ( state.useLocalVariable( name ) && state.hasSymbol( name ) )
                    _append( name + " = " + tempName + "; " , n );
                else
                    _append( "scope.put( \"" + name + "\" , " + tempName + " , true );\n" , n );
                _add( n.getFirstChild().getNext().getNext() , state );
                _append( "\n}\n" , n );
            }
            else {
                _append( "\n for ( String " , n );
                _append( tempName , n );
                _append( " : JSInternalFunctions.JS_collForFor( " , n );
                _add( n.getFirstChild().getNext() , state );
                _append( " ) ){\n " , n );

                if ( state.useLocalVariable( name ) && state.hasSymbol( name ) )
                    _append( name + " = new JSString( " + tempName + ") ; " , n );
                else
                    _append( "scope.put( \"" + name + "\" , new JSString( " + tempName + " ) , true );\n" , n );
                _add( n.getFirstChild().getNext().getNext() , state );
                _append( "\n}\n" , n );
            }
        }
        else {
            throw new RuntimeException( "wtf?" );
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

            _append( "do { \n " , theLoop );
            _add( main , state );
            _append( " } \n while ( false || JS_evalToBool( " , n );
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

        _append( "if ( JS_evalToBool( " , n );
        _add( n.getFirstChild() , state );
        _append( " ) ){\n" , n );
        _add( ifBlock , state );
        _append( "}\n" , n );
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

        _append( " else if ( true ) { " , n );
        _add( n , state );
        _append( " } \n" , n );

        _assertType( n.getNext() , Token.TARGET );
        if ( n.getNext().getNext() != null )
            throw new RuntimeException( "something is wrong" );
    }

    private void _addFunctionNodes( final ScriptOrFnNode sn , final State state ){
        Set<Integer> baseIds = new HashSet<Integer>();
        {
            Node temp = sn.getFirstChild();
            while ( temp != null ){

                if ( temp.getType() == Token.FUNCTION &&
                     temp.getString() != null ){

                    int prop = temp.getIntProp( Node.FUNCTION_PROP , -1 );
                    if ( prop >= 0 ){
                        baseIds.add( prop );
                    }
                }

                temp = temp.getNext();
            }
        }

        for ( int i=0; i<sn.getFunctionCount(); i++ ){

            FunctionNode fn = sn.getFunctionNode( i );
            _setLineNumbers( fn , fn );

            String name = fn.getFunctionName();
            String anonName = "tempFunc_" + _id + "_" + i + "_" + _methodId++;

            boolean anon = name.length() == 0;
            if ( anon )
                name = anonName;

            if ( D ){
                System.out.println( "***************" );
                System.out.println( i + " : " +  name );
            }

            String useName = name;

            if ( ! anon && ! baseIds.contains( i ) ){
                useName = anonName;
                state._nonRootFunctions.add( i );
            }

            state._functionIdToName.put( i , useName );

            _setVar( useName , fn , state , anon );
            _append( "; \n scope.getFunction( \"" + useName + "\" ).setName( \"" + name + "\" );\n\n" , fn );

        }
    }

    private void _addFunction( Node n , State state ){
        if ( ! ( n instanceof FunctionNode ) ){
            if ( n.getString() != null && n.getString().length() != 0 ){
                int id = n.getIntProp( Node.FUNCTION_PROP , -1 );
                if ( state._nonRootFunctions.contains( id ) ){
                    _append( "scope.set( \"" + n.getString() + "\" , scope.get( \"" + state._functionIdToName.get( id ) + "\" ) );\n" , n );
                }
                return;
            }
            _append( getFunc( n , state ) , n );
            return;
        }

        _assertOne( n );

        FunctionNode fn = (FunctionNode)n;
        FunctionInfo fi = FunctionInfo.create( fn );

        state = state.child();
        state._fi = fi;

        boolean hasArguments = fi.usesArguemnts();

        _append( "new JSFunctionCalls" + fn.getParamCount() + "( scope , null ){ \n" , n );

        _append( "protected void init(){ super.init(); _sourceLanguage = getFileLanguage(); \n " , n );

        _append( "_arguments = new JSArray();\n" , n );
        for ( int i=0; i<fn.getParamCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            _append( "_arguments.add( \"" + foo + "\" );\n" , n );
        }

        _append( "_globals = new JSArray();\n" , n );
        for ( String g : fi._globals )
            _append( "_globals.add( \"" + g + "\" );\n" , n );
        _append( "}\n" , n );

        String callLine = "public Object call( final Scope passedIn ";
        String varSetup = "";

        for ( int i=0; i<fn.getParamCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            callLine += " , ";
            callLine += " Object " + foo;

            if ( ! state.useLocalVariable( foo ) ){
                callLine += "INNNNN";
                varSetup += " \nscope.put(\"" + foo + "\"," + foo + "INNNNN , true  );\n ";
                if ( hasArguments )
                    varSetup += "arguments.add( " + foo + "INNNNN );\n";
            }
            else {
                state.addSymbol( foo );
                if ( hasArguments )
                    varSetup += "arguments.add( " + foo + " );\n";
            }

            callLine += " ";
        }
        callLine += " , Object ___extra[] ){\n" ;

        _append( callLine + " final Scope scope = usePassedInScope() ? passedIn : new Scope( \"func scope\" , getScope() , passedIn , getFileLanguage() ); " , n );
        if ( hasArguments ){
            _append( "JSArray arguments = new JSArray();\n" , n );
            _append( "arguments.set( \"callee\" , this );\n" , n );
            _append( "scope.put( \"arguments\" , arguments , true );\n" , n );
        }

        for ( int i=0; i<fn.getParamCount(); i++ ){

            final String foo = fn.getParamOrVarName( i );
            final String javaName = foo + ( state.useLocalVariable( foo ) ? "" : "INNNNN" );
            final Node defArg = fn.getDefault( foo );
            if ( defArg == null )
                continue;


            _append( "if ( null == " + javaName + " ) " , defArg );
            _append( javaName + " = " , defArg );
            _add( defArg , state );
            _append( ";\n" , defArg );
        }

        _append(  varSetup , n );
        if ( hasArguments ){
            _append( "if ( ___extra != null ) for ( Object TTTT : ___extra ) arguments.add( TTTT );\n" , n );
            _append( "{ Integer numArgs = _lastStart.get(); _lastStart.set( null ); while( numArgs != null && arguments.size() > numArgs && arguments.get( arguments.size() -1 ) == null ) arguments.remove( arguments.size() - 1 ); }" , n );
        }

        for ( int i=fn.getParamCount(); i<fn.getParamAndVarCount(); i++ ){
            final String foo = fn.getParamOrVarName( i );
            if ( state.useLocalVariable( foo ) ){
                state.addSymbol( foo );
                if ( state.isNumber( foo ) ){
                    _append( "double " + foo + " = 0;\n" , n );
                }
                else
                    _append( "Object " + foo + " = null;\n" , n );
            }
            else {
                _append( "scope.put( \"" + foo + "\" , null , true );\n" , n );
            }
        }

        _addFunctionNodes( fn , state );

        _add( n.getFirstChild() , fn , state );
        _append( "}\n" , n );

        int myStringId = _strings.size();
        _strings.add( getSource( fn ) );
        _append( "\t public String getSourceCode(){ return _strings[" + myStringId + "].toString(); }" , fn );
        _append( "\t public String toString(){ return _strings[" + myStringId + "].toString(); }" , fn );

        _append( "}\n" , n );

    }

    private void _addBlock( Node n , State state ){
        _assertType( n , Token.BLOCK );

        if ( n.getFirstChild() == null ){
            _append( "{}" , n );
            return;
        }

        // this is weird.  look at bracing0.js

        boolean bogusBrace = true;
        Node c = n.getFirstChild();
        while ( c != null ){
            if ( c.getType() != Token.EXPR_VOID ){
                bogusBrace = false;
                break;
            }

            if ( c.getFirstChild().getNext() != null ){
                bogusBrace = false;
                break;
            }

            if ( c.getFirstChild().getType() != Token.SETVAR ){
                bogusBrace = false;
                break;
            }

            c = c.getNext();
        }


        bogusBrace = bogusBrace ||
            ( n.getFirstChild().getNext() == null &&
              n.getFirstChild().getType() == Token.EXPR_VOID &&
              n.getFirstChild().getFirstChild() == null );

        if ( bogusBrace ){
            c = n.getFirstChild();
            while ( c != null ){
                _add( c  , state );
                c = c.getNext();
            }
            return;
        }

        boolean endReturn =
            n.getLastChild() != null &&
            n.getLastChild().getType() == Token.RETURN_RESULT;

        state._depth++;
        _append( "{" , n );

        String ret = "retName" + _rand();
        if ( endReturn )
            _append( "\n\nObject " + ret + " = null;\n\n" , n );

        Node child = n.getFirstChild();
        while ( child != null ){

            if ( endReturn && child.getType() == Token.LEAVEWITH )
                break;

            if ( endReturn && child.getType() == Token.EXPR_RESULT )
                _append( ret + " = " , child );

            _add( child , state );

            if ( child.getType() == Token.IFNE ||
                 child.getType() == Token.SWITCH )
                break;

            child = child.getNext();
        }
        if ( endReturn )
            _append( "\n\nif ( true ){ return " + ret + "; }\n\n" , n );
        _append( "}" , n );
        state._depth--;
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
        _addCall( n , state , false );
    }

    private void _addCall( Node n , State state , boolean isClass ){
        Node name = n.getFirstChild();

        boolean useThis = name.getType() == Token.GETPROP && ! isClass;

        if ( useThis )
            _append( "scope.clearThisNormal( " , n );

        Boolean inc[] = new Boolean[]{ true };
        String f = getFunc( name , state , isClass , inc  );
        _append( ( inc[0] ? f : "" ) + ".call( scope" + ( isClass ? ".newThis( " + f + " )" : "" ) + " " , n );

        Node param = name.getNext();
        while ( param != null ){
            _append( " , " , param );
            _add( param , state );
            param = param.getNext();
        }

        _append( " , new Object[0] ) " , n );
        if ( useThis )
            _append( " ) " , n );
    }

    private void _setVar( String name , Node val , State state ){
        _setVar( name , val , state , false );
    }

    private void _setVar( String name , Node val , State state , boolean local ){
        if ( state.useLocalVariable( name ) && state.hasSymbol( name ) ){
            boolean prim = state.isPrimitive( name );
            if ( ! prim )
                _append( "JSInternalFunctions.self( " , val );
            _append( name + " = " , val );
            _add( val , state );
            _append( "\n" , val );
            if ( ! prim )
                _append( ")\n" , val );
            return;
        }
        _append( "scope.put( \"" + name + "\" , " , val);
        _add( val , state );
        _append( " , " + local + "  ) " , val );
    }

    static int countChildren( Node n ){
        int num = 0;
        Node c = n.getFirstChild();
        while ( c != null ){
            num++;
            c = c.getNext();
        }
        return num;
    }

    public static void _assertOne( Node n ){
        if ( n.getFirstChild() == null ){
            Debug.printTree( n , 0 );
            throw new RuntimeException( "no child" );
        }
        if ( n.getFirstChild().getNext() != null ){
            Debug.printTree( n , 0 );
            throw new RuntimeException( "more than 1 child" );
        }
    }

    public void _assertType( Node n , int type ){
        _assertType( n , type , this );
    }

    public static void _assertType( Node n , int type , Convert c ){
        if ( type == n.getType() )
            return;

        String msg = "wrong type. was : " + Token.name( n.getType() ) + " should be " + Token.name( type );
        if ( c != null )
            msg += " file : " + c._name + " : " + ( c._nodeToSourceLine.get( n ) + 1 );
        throw new RuntimeException( msg );
    }

    private void _setLineNumbers( final Node startN , final ScriptOrFnNode startSOF ){

        final Set<Integer> seen = new HashSet<Integer>();

        final List<Pair<Node,ScriptOrFnNode>> overallTODO = new LinkedList<Pair<Node,ScriptOrFnNode>>();
        overallTODO.add( new Pair<Node,ScriptOrFnNode>( startN , startSOF ) );

        while ( overallTODO.size() > 0 ){
            final Pair<Node,ScriptOrFnNode> temp = overallTODO.remove( 0 );

            Node n = temp.first;
            final ScriptOrFnNode sof = temp.second;

            final int line = n.getLineno();

            if ( line < 0 )
                throw new RuntimeException( "something is wrong" );

            List<Node> todo = new LinkedList<Node>();

            _nodeToSourceLine.put( n , line );
            _nodeToSOR.put( n , sof );

            if ( n.getFirstChild() != null )
                todo.add( n.getFirstChild() );
            if ( n.getNext() != null )
                todo.add( n.getNext() );

            while ( todo.size() > 0 ){
                n = todo.remove(0);

                if ( seen.contains( n.hashCode() ) )
                    continue;

                seen.add( n.hashCode() );

                if ( n.getLineno() > 0 ){
                    overallTODO.add( new Pair<Node,ScriptOrFnNode>( n , n instanceof ScriptOrFnNode ? (ScriptOrFnNode)n : sof ) );
                    continue;
                }

                _nodeToSourceLine.put( n , line );
                _nodeToSOR.put( n , sof );

                if ( n.getFirstChild() != null )
                    todo.add( n.getFirstChild() );
                if ( n.getNext() != null )
                    todo.add( n.getNext() );
            }
        }
    }

    private void _append( String s , Node n ){
        _mainJavaCode.append( s );

        if ( n == null )
            return;

        int numLines = 0;
        int max = s.length();
        for ( int i=0; i<max; i++ )
            if ( s.charAt( i ) == '\n' )
                numLines++;

        final int start = _currentLineNumber;
        int end = _currentLineNumber + numLines;

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
        return getFunc( n , state , false , null );
    }
    private String getFunc( Node n , State state , boolean isClass , Boolean inc[] ){
        if ( n.getClass().getName().indexOf( "StringNode" ) < 0 ){
            if ( n.getType() == Token.GETPROP && ! isClass ){
                _append( "scope.getFunctionAndSetThis( " , n );
                _add( n.getFirstChild() , state );
                _append( " , " , n );
                _add( n.getFirstChild().getNext() , state );
                _append( ".toString() ) " , n );
                return "";
            }

            int start = _mainJavaCode.length();
            _append( "((JSFunction )" , n);
            _add( n , state );
            _append( ")" , n );
            int end = _mainJavaCode.length();
            if( isClass ){
                if ( inc == null )
                    throw new RuntimeException( "inc is null and can't be here" );
                inc[0] = false;
                return "(" + _mainJavaCode.substring( start , end ) + ")";
            }
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
            return "(( JSFunction)" + name + ")";

        return "scope.getFunction( \"" + name + "\" )";
    }

    public String getClassName(){
        return _className;
    }

    public String getClassString(){
        StringBuilder buf = new StringBuilder();

        buf.append( "package " + _package + ";\n" );

        buf.append( "import ed.js.*;\n" );
        buf.append( "import ed.js.func.*;\n" );
        buf.append( "import ed.js.engine.Scope;\n" );
        buf.append( "import ed.js.engine.JSCompiledScript;\n" );

        buf.append( "public class " ).append( _className ).append( " extends JSCompiledScript {\n" );

        buf.append( "\t protected void myInit(){\n" );
        if ( _loadOnce )
            buf.append( "\t\t _loadOnce = true; \n" );
        buf.append( "\t} \n" );


        buf.append( "\tpublic Object _call( Scope scope , Object extra[] ) throws Exception {\n" );

        buf.append( "\t\t final Scope passedIn = scope; \n" );

        if (_invokedFromEval) {
            buf.append("\t\t // not creating new scope for execution as we're being run in the context of an eval\n");
        }
        else {
            String cleanName = FileUtil.clean( _name );
            buf.append( "\t\t if ( ! usePassedInScope() ){\n" );
            buf.append( "\t\t\t scope = new Scope( \"compiled script for:" + cleanName + "\" , scope , null , getFileLanguage() ); \n" );
            buf.append( "\t\t\t scope.setPath( this._path ); \n" );
            buf.append( "\t\t }\n" );
            buf.append( "\t\t scope.putAll( getTLScope() );\n" );
        }

        buf.append( "\t\t JSArray arguments = new JSArray(); scope.put( \"arguments\" , arguments , true );\n " );
        buf.append( "\t\t if ( extra != null ) for ( Object TTTT : extra ) arguments.add( TTTT );\n" );

        _preMainLines = StringUtil.count( buf.toString() , "\n" );

        buf.append( _mainJavaCode );


        buf.append( "\n\n\t}\n\n" );

        buf.append( "\n}\n\n" );
        return buf.toString();
    }

    public JSFunction get(){
        if ( _it != null )
            return _it;

        try {
            Class c = CompileUtil.compile( _package , getClassName() , getClassString() , this );
            JSCompiledScript it = (JSCompiledScript)c.newInstance();

            _scriptInfo.setup( this );
            it._scriptInfo = _scriptInfo;

            it._regex = _regex;

            it._strings = new String[ _strings.size() ];
            for ( int i=0; i<_strings.size(); i++ )
                it._strings[i] = _strings.get( i );

            it.setName( _name );

            _it = it;

            StackTraceHolder.getInstance().set( _fullClassName , _scriptInfo );
            StackTraceHolder.getInstance().setPackage( "ed.js" , _scriptInfo );
            StackTraceHolder.getInstance().setPackage( "ed.js.func" , _scriptInfo );
            StackTraceHolder.getInstance().setPackage( "ed.js.engine" , _scriptInfo );

            return _it;
        }
        catch ( RuntimeException re ){
            re.printStackTrace();
            fixStack( re );
            throw re;
        }
        catch ( Exception e ){
            e.printStackTrace();
            fixStack( e );
            throw new RuntimeException( e );
        }
    }

    public void fixStack( Throwable e ){
        StackTraceHolder.getInstance().fix( e );
    }

    Node _getNodeFromJavaLine( int line ){
        line = ( line - _preMainLines ) - 1;

        List<Node> nodes = _javaCodeToLines.get( line );
        if ( nodes == null || nodes.size() == 0  ){
            return null;
        }


        return nodes.get(0);
    }

    public int _mapLineNumber( int line ){
        Node n = _getNodeFromJavaLine( line );
        if ( n == null )
            return -1;
        Integer i = _nodeToSourceLine.get( n );
        if ( i == null )
            return -1;
        return i + 1;
    }

    public void _debugLineNumber( final int line ){
        System.out.println( "-----" );
        for ( int temp = Math.max( 0 , line - 5 );
              temp < line + 5 ;
              temp++ )
            System.out.println( "\t" + temp + "->" + _mapLineNumber( temp ) + " || " + _getNodeFromJavaLine( temp ) );
        System.out.println( "-----" );
    }



    String getSource( FunctionNode fn ){
        final int start = fn.getEncodedSourceStart();
        final int end = fn.getEncodedSourceEnd();

        final String encoded =  _encodedSource.substring( start , end );
        final String realSource = Decompiler.decompile( encoded , 0 , new UintMap() );

        return realSource;
    }

    private String getStringCode( String s ){
        int stringId = _strings.size();
        _strings.add( s );
        return "_string(" + stringId + ")";
    }

    public boolean hasReturn(){
        return _hasReturn;
    }

    public int findStringId( String s ){
        for ( int i=0; i<_strings.size(); i++ ){
            if ( _strings.get(i).equals( s ) ){
                return i;
            }
        }
        return -1;
    }

    String _rand(){
        return String.valueOf( _random.nextInt( 1000000 ) );
    }

    static Random _random( String name ){
        return new Random( name.hashCode() );
    }

    final Random _random;
    final String _name;
    final String _source;
    final String _encodedSource;
    final String _className;
    final String _fullClassName;
    final String _package = DEFAULT_PACKAGE;
    final boolean _invokedFromEval;
    final Language _sourceLanguage;
    final int _id;

    // these 3 variables should only be use by _append
    private int _currentLineNumber = 0;
    final Map<Integer,List<Node>> _javaCodeToLines = new TreeMap<Integer,List<Node>>();
    final Map<Node,Integer> _nodeToSourceLine = new IdentityHashMap<Node,Integer>();
    final Map<Node,ScriptOrFnNode> _nodeToSOR = new IdentityHashMap<Node,ScriptOrFnNode>();
    final List<Pair<String,String>> _regex = new ArrayList<Pair<String,String>>();
    final List<String> _strings = new ArrayList<String>();
    final ScriptInfo _scriptInfo;

    int _preMainLines = -1;
    private final StringBuilder _mainJavaCode = new StringBuilder();

    private boolean _hasReturn = false;
    private JSFunction _it;
    private boolean _loadOnce = false;

    private int _methodId = 0;

    private final static Map<Integer,String> _2ThingThings = new HashMap<Integer,String>();
    static {
        _2ThingThings.put( Token.ADD , "add" );
        _2ThingThings.put( Token.MUL , "mul" );
        _2ThingThings.put( Token.SUB , "sub" );
        _2ThingThings.put( Token.DIV , "div" );

        _2ThingThings.put( Token.SHEQ , "sheq" );
        _2ThingThings.put( Token.SHNE , "shne" );
        _2ThingThings.put( Token.EQ , "eq" );
        _2ThingThings.put( Token.NE , "eq" );

        _2ThingThings.put( Token.GE , "ge" );
        _2ThingThings.put( Token.LE , "le" );
        _2ThingThings.put( Token.LT , "lt" );
        _2ThingThings.put( Token.GT , "gt" );

        _2ThingThings.put( Token.BITOR , "bitor" );
        _2ThingThings.put( Token.BITAND , "bitand" );
        _2ThingThings.put( Token.BITXOR , "bitxor" );

        _2ThingThings.put( Token.URSH , "ursh" );
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

    // SCRIPT INFO

    static class ScriptInfo implements StackTraceFixer {

        ScriptInfo( String name , String fullClassName , Language l , Convert c ){
            _name = name;
            _fullClassName = fullClassName;
            _sourceLanguage = l;
            _convert = DJS ? c : null;
        }

        public void fixStack( Throwable e ){
            StackTraceHolder.getInstance().fix( e );
        }

        public StackTraceElement fixSTElement( StackTraceElement element ){
            return fixSTElement( element , false );
        }

        public StackTraceElement fixSTElement( StackTraceElement element , boolean debug ){

            if ( ! element.getClassName().startsWith( _fullClassName ) )
                return null;

            if ( debug ){
                System.out.println( element );
                if ( _convert != null )
                    _convert._debugLineNumber( element.getLineNumber() );
            }

            Pair<Integer,String> p = _lines.get( element.getLineNumber() );
            if ( p == null )
                return null;

            return new StackTraceElement( _name , p.second , _name , p.first );
        }

        public boolean removeSTElement( StackTraceElement element ){
            String s = element.toString();

            return
                s.contains( ".call(JSFunctionCalls" ) ||
                s.contains( "ed.js.JSFunctionBase.call(" ) ||
                s.contains( "ed.js.engine.JSCompiledScript.call" );
        }

        void setup( Convert c ){
            for ( int i=0; i<c._currentLineNumber + 100; i++ ){
                Node n = c._getNodeFromJavaLine( i );
                if ( n == null )
                    continue;

                int line = c._mapLineNumber( i );

                ScriptOrFnNode sof = c._nodeToSOR.get( n );
                String method = "___";
                if ( sof instanceof FunctionNode )
                    method = ((FunctionNode)sof).getFunctionName();

                _lines.put( i , new Pair<Integer,String>( line , method ) );
            }
        }

        final String _name;
        final String _fullClassName;
        final Language _sourceLanguage;
        final Convert _convert;

        final Map<Integer,Pair<Integer,String>> _lines = new HashMap<Integer,Pair<Integer,String>>();
    }

    // END SCRIPT INFO

    // this is class compile optimization below

    static synchronized int _getNumForClass( String name , String source ){
        ClassInfo ci = _classes.get( name );
        if ( ci == null ){
            ci = new ClassInfo();
            _classes.put( name , ci );
        }
        return ci.getNum( source );
    }

    private static Map<String,ClassInfo> _classes =  Collections.synchronizedMap( new HashMap<String,ClassInfo>() );

    static class ClassInfo {

        synchronized int getNum( String source ){
            _myMd5.Init();
            _myMd5.Update( source );
            final String hash = _myMd5.asHex();

            Integer num = _sourceToNumber.get( hash  );
            if ( num != null )
                return num;

            num = ++_numSoFar;
            _sourceToNumber.put( hash , num );

            return num;
        }

        final MD5 _myMd5 = new MD5();
        final Map<String,Integer> _sourceToNumber = new TreeMap<String,Integer>();

        int _numSoFar = 0;
    }
}
