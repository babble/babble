// JSON.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSON {
    
    public static void init( Scope s ){
        s.put( "tojson" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return serialize( o );
                }
            }
            , true
            );
        
    }

    public static String serialize( Object o ){
        StringBuilder buf = new StringBuilder();
        try {
            serialize( buf , o );
        }
        catch ( java.io.IOException e ){
            throw new RuntimeException( e );
        }
        return buf.toString();
    }

    public static void serialize( Appendable a , Object o )
        throws java.io.IOException {
        Serializer.go( a , o , 0 );
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
        
        static void go( Appendable a , Object something , int indent )
            throws java.io.IOException {
            
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
                a.append( "\"" + something + "\"" );
                return;
            }

            if ( something instanceof JSFunction ){
                a.append( something.toString() );
                return;
            }

            JSObject o = (JSObject)something;

            a.append( _i( indent ) );
            a.append( "{" );
            
            boolean first = true;

            for ( String s : o.keySet() ){
                if ( first )
                    first = false;
                else 
                    a.append( " ,\n" );
                
                a.append( _i( indent + 1 ) );
                a.append( s );
                a.append( " : " );
                go( a , o.get( s ) , indent + 1 );
            }

            a.append( _i( indent + 1 ) );
            a.append( "\n}\n" );
        }

    }
}

