// JSON.java

package ed.js;

import java.util.*;

import org.mozilla.javascript.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSON {

    static Set<String> IGNORE_NAMES = new HashSet<String>();
    static {
        IGNORE_NAMES.add( "_save" );
        IGNORE_NAMES.add( "_ns" );
    }
    
    public static void init( Scope s ){

        s.put( "tojson" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return serialize( o );
                }
            } , true
            );
        
        s.put( "fromjson" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return parse( o.toString() );
                }
            } , true );
    }

    public static String serialize( Object o ){
        return serialize( o , "\n" );
    }

    public static String serialize( Object o , String nl ){
        StringBuilder buf = new StringBuilder();
        try {
            serialize( buf , o , nl );
        }
        catch ( java.io.IOException e ){
            throw new RuntimeException( e );
        }
        return buf.toString();
    }

    public static void serialize( Appendable a , Object o )
        throws java.io.IOException {
        serialize( a , o , "\n" );
    }

    public static void serialize( Appendable a , Object o , String nl )
        throws java.io.IOException {
        Serializer.go( a , o , 0 , nl );
    }
    
    static class Serializer {

        static Map<Integer,String> _indents = new HashMap<Integer,String>();
        
        static String _i( final int i ){
            String s = _indents.get( i );
            if ( s == null ){
                s = "";
                for ( int j=0; j<i; j++ )
                    s += " ";
                _indents.put( i , s );
            }
            return s;
        }
        
        static void go( Appendable a , Object something , int indent , String nl  )
            throws java.io.IOException {
            
            if ( nl.length() > 0 ){
                if ( a instanceof StringBuilder ){
                    StringBuilder sb = (StringBuilder)a;
                    int lastNL = sb.lastIndexOf( nl );
                    if ( sb.length() - lastNL > 60 ){
                        a.append( nl );
                    }
                }
            }

            if ( something == null ){
                a.append( "null" );
                return;
            }
    
            if ( something instanceof Number || 
                 something instanceof Boolean ||
                 something instanceof JSRegex ||
                 something instanceof JSDate ){
                a.append( something.toString() );
                return;
            }

            if ( something instanceof JSString || 
                 something instanceof String ){
		String foo = something.toString();
		foo = foo.replaceAll( "\"" , "\\\\\"" );
                a.append( "\"" + foo + "\"" );
                return;
            }

            if ( something instanceof JSFunction ){
                a.append( something.toString() );
                return;
            }
            
            if ( something instanceof ed.db.ObjectId ){
                a.append( "CrID( \"" + something + "\" )" );
                return;
            }

            if ( ! ( something instanceof JSObject ) ){
                a.append( something.toString() );
                return;
            }
            
            if ( something instanceof JSArray ){
                JSArray arr = (JSArray)something;
                a.append( "[ " );
                for ( int i=0; i<arr._array.size(); i++ ){
                    if ( i > 0 )
                        a.append( " , " );
                    go( a , arr._array.get( i ) , indent , nl );
                }
                a.append( " ]" );
                return;
            }

            JSObject o = (JSObject)something;

            { 
                Object foo = o.get( "tojson" );
                if ( foo != null && foo instanceof JSFunction ){
                    a.append( ((JSFunction)foo).call( Scope.GLOBAL ).toString() );
                    return;
                }
            }

            a.append( _i( indent ) );
            a.append( "{" );
            
            boolean first = true;

            for ( String s : o.keySet() ){
                
                if ( IGNORE_NAMES.contains( s ) )
                    continue;

                Object val = o.get( s );
                if ( val instanceof JSObjectBase ){
                    ((JSObjectBase)val).prefunc();
                    if ( o.get( s ) == null )
                        continue;
                }

                if ( first )
                    first = false;
                else 
                    a.append( " ,"  );
                
                a.append( _i( indent + 1 ) );
                a.append( s );
                a.append( " : " );
                go( a , val , indent + 1 , nl );
            }

            a.append( _i( indent + 1 ) );
            a.append( " }\n"  );
        }

    }


    public static Object parse( String s ){
        CompilerEnvirons ce = new CompilerEnvirons();
        Parser p = new Parser( ce , ce.getErrorReporter() );

        ScriptOrFnNode theNode = p.parse( "return " + s + ";" , "foo" , 0 );
        
        Node ret = theNode.getFirstChild();
        Convert._assertType( ret , Token.RETURN );
        Convert._assertOne( ret );
        
        Node lit = ret.getFirstChild();
        if ( lit.getType() != Token.OBJECTLIT && lit.getType() != Token.ARRAYLIT ){
            Debug.printTree( lit , 0 );
            throw new JSException( "not a literal" );
        }
        
        return build( lit );
    }

    private static Object build( Node n ){
        if ( n == null )
            return null;
        
        Node c;

        switch ( n.getType() ){
            
        case Token.OBJECTLIT:
            JSObject o = new JSObjectBase();
            Object[] names = (Object[])n.getProp( Node.OBJECT_IDS_PROP );
            int i=0;
            
            c = n.getFirstChild();
            while ( c != null ){
                o.set( names[i++].toString() , build( c ) );
                c = c.getNext();
            }            

            return o;

        case Token.ARRAYLIT:
            JSArray a = new JSArray();
            c = n.getFirstChild();
            while ( c != null ){
                a.add( build( c ) );
                c = c.getNext();
            }
            return a;
        
        case Token.NUMBER:
            return n.getDouble();
        case Token.STRING:
            return new JSString( n.getString() );

        }
        
        Debug.printTree( n , 0 );
        throw new RuntimeException( "what: " + n.getType() );
    }
}

