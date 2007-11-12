// JSON.java

package ed.js;

import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSON {

    static Set<String> IGNORE_NAMES = new HashSet<String>();
    static {
        IGNORE_NAMES.add( "_save" );
    }
    
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
            a.append( " }"  );
        }

    }
}

