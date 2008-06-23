// State.java

package ed.js.engine;

import java.util.*;

import org.mozilla.javascript.*;

/**
 * this is used only by Convert
 * to keep compile state
 */
class State {
    
    State(){
        _parent = null;
    }
    
    State( State s ){
        _parent = s;
    }
    
    State child(){
        return new State( this );
    }
    
    State parent(){
        return _parent;
    }
    
    boolean hasSymbol( String s ){
        return _localSymbols.contains( s );
    }
    
    boolean addSymbol( String s ){
        return _localSymbols.add( s );
    }

    boolean isNumberAndLocal( String s ){
        return isNumber( s ) && useLocalVariable( s );
    }

    boolean isNumber( String s ){
        if ( _fi == null )
            return false;
        return _fi.isNumber( s );
    }

    boolean isNumberAndLocal( Node n ){
        if ( ! isNumber( n ) )
            return false;

        if ( n.getType() == Token.GETVAR || n.getType() == Token.NAME )
            return useLocalVariable( n.getString() );
        
        return true;
    }

    boolean isNumber( Node n ){
        if ( n.getType() == Token.NUMBER )
            return true;
        
        if ( _fi == null )
            return false;
        
        return _fi.isNumber( n );
    }

    boolean isPrimitive( String s ){
        return isNumber( s );
    }

    boolean useLocalVariable( String name ){
        if ( name.equals( "arguments" ) )
            return false;
        
        if ( JAVA_RESERVED_WORDS.contains( name ) )
            return false;

        if ( _fi == null )
            return false;
        
        return _fi.canUseLocal( name );
    }

    final Set<String> _localSymbols = new HashSet<String>();
    final Map<Integer,String> _functionIdToName = new HashMap<Integer,String>();
    final Set<Integer> _nonRootFunctions = new HashSet<Integer>();
    final Stack<String> _tempOpNames = new Stack<String>();

    final State _parent;
    

    FunctionInfo _fi;
    
    final static Set<String> JAVA_RESERVED_WORDS = new HashSet<String>();
    static {
        JAVA_RESERVED_WORDS.add( "first" );
        JAVA_RESERVED_WORDS.add( "final" );
        JAVA_RESERVED_WORDS.add( "assert" );
        JAVA_RESERVED_WORDS.add( "abstract" );
        JAVA_RESERVED_WORDS.add( "default" );
        JAVA_RESERVED_WORDS.add( "goto" ) ;
        JAVA_RESERVED_WORDS.add( "package" );
        JAVA_RESERVED_WORDS.add( "synchronized" );
        JAVA_RESERVED_WORDS.add( "boolean" );
        JAVA_RESERVED_WORDS.add( "private" );
        JAVA_RESERVED_WORDS.add( "double" );
        JAVA_RESERVED_WORDS.add( "implements" );
        JAVA_RESERVED_WORDS.add( "protected" );
        JAVA_RESERVED_WORDS.add( "byte" );
        JAVA_RESERVED_WORDS.add( "import" ); 
        JAVA_RESERVED_WORDS.add( "public" );	
        JAVA_RESERVED_WORDS.add( "throws" );
        JAVA_RESERVED_WORDS.add( "case" );	
        JAVA_RESERVED_WORDS.add( "enum" );	
        JAVA_RESERVED_WORDS.add( "instanceof" );
        JAVA_RESERVED_WORDS.add( "transient" );
        JAVA_RESERVED_WORDS.add( "extends" );
        JAVA_RESERVED_WORDS.add( "int" );
        JAVA_RESERVED_WORDS.add( "short" );
        JAVA_RESERVED_WORDS.add( "char" );
        JAVA_RESERVED_WORDS.add( "final" );
        JAVA_RESERVED_WORDS.add( "interface" );
        JAVA_RESERVED_WORDS.add( "static" );
        JAVA_RESERVED_WORDS.add( "void" );
        JAVA_RESERVED_WORDS.add( "class" );
        JAVA_RESERVED_WORDS.add( "finally" );      
        JAVA_RESERVED_WORDS.add( "long" );
        JAVA_RESERVED_WORDS.add( "strictfp" );
        JAVA_RESERVED_WORDS.add( "volatile" );
        JAVA_RESERVED_WORDS.add( "const" );
        JAVA_RESERVED_WORDS.add( "float" );
        JAVA_RESERVED_WORDS.add( "native" );
        JAVA_RESERVED_WORDS.add( "super" );

    }
}

