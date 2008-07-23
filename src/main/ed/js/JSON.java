// JSON.java

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

package ed.js;

import java.util.*;

import org.mozilla.javascript.*;

import ed.js.func.*;
import ed.js.engine.*;
import ed.log.Logger;

/**  Methods to serialize objects into JSON.
 *   These are exposed to Javascript as tojson and tojson_u.  tojson
 *   serializes dates as strings of the form <tt>new Date( 1215614349 )</tt>
 *   and serializes ObjectIds as strings of the form
 *   <tt>ObjectId( "4874e0ca0bea01fd00d330d3" )</tt>.  This might not be
 *   appropriate if you're going to parse it using YUI or any other JSON
 *   library.  <tt>tojson_u</tt> instead serializes these as <tt>1215614349</tt>
 *   and <tt>"4874e0ca0bea01fd00d330d3"</tt>, respectively.
 *
 *   tojson will try to serialize functions; tojson_u will simply refuse.
 *
 *   @anonymous name : {fromjson} desc : {Takes a string in json form and converts it to an object.}, return : {type : (Object), desc : (the object created from the given string)}, param : {type : (string) desc : (the string representation of an object), name : (jsonStr)}
 *   @anonymous name : {tojson} desc : {Serializes an object into JSON form.}, return : {type : (string), desc : (the string created from the given object)}, param : {type : (Object) desc : (the object to convert), name : (obj)}
 *   @anonymous name : {tojson_u} desc : {Serializes an untrusted object into escaped JSON form.}, return : {type : (string), desc : (the safe string created from the given object)}, param : {type : (Object) desc : (the untrusted object to convert), name : (obj)}
 *   @expose
 */
public class JSON {

    /** Hidden fields to ignore.  Includes _save, _ns, and _update. */
    public static Set<String> IGNORE_NAMES = new HashSet<String>();
    static {
        IGNORE_NAMES.add( "_save" );
        IGNORE_NAMES.add( "_update" );
        IGNORE_NAMES.add( "_ns" );
    }

    /** Initialize json functions tojson, tojson_u, and fromjson.
     */
    public static void init( Scope s ){

        s.put( "tojson" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return serialize( o , true );
                }
            } , true
            );

        s.put( "tojson_u" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return serialize( o , false );
                }
            } , true
            );

        s.put( "fromjson" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return parse( o.toString() );
                }
            } , true );
    }

    /** Serializes an object.  Assumes trusted mode.
     * @param o Object to be serialized.
     * @return <tt>o</tt> in serialized form.
     */
    public static String serialize( Object o ){
        // Backwards compatibility
        return serialize( o, true );
    }

    /** Serializes an object.
     * @param o Object to be serialized.
     * @param trusted Trusted mode; see {@link JSON}.
     * @return <tt>o</tt> in serialized form.
     */
    public static String serialize( Object o , boolean trusted ){
        return serialize( o , trusted , "\n" );
    }

    /** Serializes an object with the given newline string.
     * @param o Object to be serialized.
     * @param trusted Trusted mode; see {@link JSON}.
     * @param nl Newline string to use.
     * @return <tt>o</tt> in serialized form.
     */
    public static String serialize( Object o , boolean trusted , String nl ){
        StringBuilder buf = new StringBuilder();
        try {
            serialize( buf , o , trusted , nl );
        }
        catch ( java.io.IOException e ){
            throw new RuntimeException( e );
        }
        return buf.toString();
    }

    /** Serializes an object to the given Appendable.
     * @param a Appender for serialized object.
     * @param o Object to be serialized.
     * @param trusted Trusted mode; see {@link JSON}.
     */
    public static void serialize( Appendable a , Object o , boolean trusted )
        throws java.io.IOException {
        serialize( a , o , trusted , "\n" );
    }

    /** Serializes an object to the given Appendable.
     * @param a Appender for serialized object.
     * @param o Object to be serialized.
     * @param trusted Trusted mode; see {@link JSON}.
     * @param nl Newline character to use.
     */
    public static void serialize( Appendable a , Object o , boolean trusted , String nl )
        throws java.io.IOException {
        Serializer.go( a , o , trusted , 0 , nl );
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

        static void string( Appendable a , String s )
            throws java.io.IOException {
            a.append("\"");
            for(int i = 0; i < s.length(); ++i){
                char c = s.charAt(i);
                if(c == '\\')
                    a.append("\\\\");
                else if(c == '"')
                    a.append("\\\"");
                else if(c == '\n')
                    a.append("\\n");
                else if(c == '\r')
                    a.append("\\r");
                else if(c == '\t')
                    a.append("\\t");
                else
                    a.append(c);
            }
            a.append("\"");
        }

        static void go( Appendable a , Object something , boolean trusted , int indent , String nl  )
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
                 something instanceof JSRegex ){
                a.append( something.toString() );
                return;
            }

	    if ( something instanceof JSDate ){
                if ( trusted ) {
                    a.append( "new Date( " + ((JSDate)something)._time + " ) " );
                    return;
                }
                else {
                    a.append( new Long(((JSDate)something)._time).toString() );
                    return;
                }
	    }

            if ( something instanceof JSString ||
                 something instanceof String ){
                string( a , something.toString() );
                return;
            }

            if ( something instanceof Logger ){
                string( a , something.toString() );
                return;
            }

            if ( something instanceof JSFunction ){
                if ( trusted ) {
                    String s = something.toString();
                    if( s.trim().startsWith("function ()" ) )
                        // JS implementation; presumed safe to work
                        a.append( s );
                    else
                        // Java implementation -- not valid JSON
                        string ( a, s );
                    return;
                }
                throw new java.io.IOException("can't serialize functions in untrusted mode");
            }

            if ( something instanceof ed.db.ObjectId ){
                if ( trusted ) {
                    a.append( "ObjectId( \"" + something + "\" )" );
                    return;
                }
                else {
                    string( a , something.toString() );
                    return;
                }
            }

            if ( ! ( something instanceof JSObject ) ){
                string( a , something.toString() );
                return;
            }

            if ( something instanceof JSArray ){
                JSArray arr = (JSArray)something;
                a.append( "[ " );
                for ( int i=0; i<arr._array.size(); i++ ){
                    if ( i > 0 )
                        a.append( " , " );
                    go( a , arr._array.get( i ) , trusted, indent , nl );
                }
                a.append( " ]" );
                return;
            }

            if ( something instanceof Scope ){
                a.append( something.toString() );
                return;
            }

            JSObject o = (JSObject)something;

            {
                Object foo = o.get( "tojson" );
                if ( foo != null && foo instanceof JSFunction ){
                    a.append( ((JSFunction)foo).call( Scope.getAScope() ).toString() );
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
                string( a , s );
                a.append( " : " );
                go( a , val , trusted , indent + 1 , nl );
            }

            a.append( _i( indent + 1 ) );
            a.append( " }\n"  );
        }

    }

    /** Parse a string in JSON form to create an object.
     * @param s String to be parsed.
     * @return Object created from parsing string.
     * @throws JSException If the parsed "object" is neither an object nor an array
     */
    public static Object parse( String s ){
        CompilerEnvirons ce = new CompilerEnvirons();
        Parser p = new Parser( ce , ce.getErrorReporter() );

        s = "return " + s.trim() + ";";

        ScriptOrFnNode theNode = p.parse( s , "foo" , 0 );

        Node ret = theNode.getFirstChild();
        Convert._assertType( ret , Token.RETURN , null );
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
            double d = n.getDouble();
            if ( JSNumericFunctions.couldBeInt( d ) )
                return (int)d;
            return d;
        case Token.STRING:
            return new JSString( n.getString() );

        }

        Debug.printTree( n , 0 );
        throw new RuntimeException( "what: " + n.getType() );
    }
}
