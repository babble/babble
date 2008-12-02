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

import ed.ext.org.mozilla.javascript.*;

import ed.util.*;
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

    static boolean PRETTY = true; // could be configurable, or parameter
    static int INDENT_AMOUNT = 4; // could be configurable, or parameter

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
                    return new JSString(serialize( o , true ));
                }
            } , true
            );

        s.put( "tojson_u" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object foo[] ){
                    return new JSString(serialize( o , false ));
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
        serialize( a , o , trusted , false , nl );
    }

    public static void serialize( Appendable a , Object o , boolean trusted , boolean strict , String nl )
        throws java.io.IOException {
        Serializer.go( a , o , trusted , strict , 0 , nl , new IdentitySet() );
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

        static boolean _loopable( Object o ){
            if ( o == null )
                return false;
            
            if ( o instanceof Number || o instanceof String || o instanceof Boolean || o instanceof JSString )
                return false;

            return true;
        }

        static void go( Appendable a , Object something , boolean trusted , boolean strict , int indent , String nl , IdentitySet seen )
            throws java.io.IOException {

            if ( _loopable( something ) ){
                
                if ( seen.contains( something ) )
                    throw new RuntimeException( "loop depetected.  can't serialize a loop : " + something + " : " + something.getClass() );
                
                seen.add( something );
            }
            
            if ( nl.length() > 0 ){
                if ( a instanceof StringBuilder ){
                    StringBuilder sb = (StringBuilder)a;
                    int lastNL = sb.lastIndexOf( nl );
                }
            }

            try {
                if ( something == null ||
                     something instanceof JSInternalFunctions.Void ){
                    a.append( "null" );
                    return;
                }

                if ( something instanceof Number ||
                     something instanceof Boolean ||
                     something instanceof JSRegex ||
                     something instanceof JSBoolean ){
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

                if ( something instanceof ed.js.e4x.ENode ||
                     something instanceof ed.js.e4x.XMLList ||
                     something instanceof ed.js.e4x.Namespace ||
                     something instanceof ed.js.e4x.QName ) {
                    a.append( something.toString() );
                    return;
                }
                
                if ( something instanceof JSFunction ){

                    if ( strict )
                        return;
                    
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
                    a.append( "[" );
                    a.append( nl );
                    for ( int i=0; i<arr._array.size(); i++ ){
                        if ( i > 0 ){
                            a.append( " ," );
                            a.append( nl );
                        }
                        a.append( _i( indent + INDENT_AMOUNT ) );
                        go( a , arr._array.get( i ) , trusted, strict , indent + INDENT_AMOUNT , nl , seen );
                    }
                    a.append( nl );
                    a.append( _i( indent ) );
                    a.append( "]" );
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

                a.append( "{" );
                a.append( nl );

                boolean first = true;

                for ( String s : o.keySet( false ) ){

                    if ( IGNORE_NAMES.contains( s ) )
                        continue;

                    Object val = o instanceof JSObjectBase ? 
                        ((JSObjectBase)o)._simpleGet( s ) : o.get( s );
                    if ( val instanceof JSObjectBase ){
                        ((JSObjectBase)val).prefunc();
                        if ( ( o instanceof JSObjectBase ?
                               ((JSObjectBase)o)._simpleGet( s ) : o.get( s ) ) == null )
                            continue;
                    }

                    if ( strict && val instanceof JSFunction )
                        continue;

                    if ( first )
                        first = false;
                    else{
                        a.append( " ,"  );
                        a.append( nl );
                    }

                    a.append( _i( indent + INDENT_AMOUNT ) );
                    string( a , s );
                    a.append( " : " );
                    go( a , val , trusted , strict , indent + INDENT_AMOUNT , nl , seen );
                }

                a.append( nl );
                a.append( _i( indent ) );
                a.append( "}"  );
            }
            finally {
                if( _loopable( something ) )
                    seen.remove( something );

            }
        }

    }

    /** Parse a string in JSON form to create an object.
     * @param s String to be parsed.
     * @return Object created from parsing string.
     * @throws JSException If the parsed "object" is neither an object nor an array
     */
    public static Object parse( String s ){

	if ( s == null )
	    return null;

	s = s.trim();
	if ( s.length() == 0 )
	    return null;

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

        return build( lit , theNode );
    }

    private static Object build( Node n , ScriptOrFnNode script ){
        if ( n == null )
            return null;

        Node c;
        final String name;
        final Object[] args;

        switch ( n.getType() ){

        case Token.OBJECTLIT:
            JSObject o = new JSObjectBase();
            Object[] names = (Object[])n.getProp( Node.OBJECT_IDS_PROP );
            int i=0;

            c = n.getFirstChild();
            while ( c != null ){
                o.set( names[i++].toString() , build( c , script ) );
                c = c.getNext();
            }

            return o;

        case Token.ARRAYLIT:
            JSArray a = new JSArray();
            c = n.getFirstChild();
            while ( c != null ){
                a.add( build( c , script ) );
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
            
        case Token.NULL:
            return null;

        case Token.TRUE:
            return true;
            
        case Token.FALSE:
            return false;
            
        case Token.REGEXP:
            int rId = n.getIntProp( Node.REGEXP_PROP , -1 );
            return new JSRegex( script.getRegexpString( rId  ) , script.getRegexpFlags( rId ) );

        case Token.CALL:
            name = n.getFirstChild().getString();
            args = getArgs( n.getFirstChild().getNext() , script );
            
            if ( ALLOWED_FUNCTIONS.contains( name ) )
                return getJsonScope().getFunction( name ).call( getJsonScope() , args );

            throw new RuntimeException( "not allowed to call [" + name + "] in json" );

        case Token.NEW:
            name = n.getFirstChild().getString();
            args = getArgs( n.getFirstChild().getNext() , script );
            
            if ( ! ALLOWED_FUNCTIONS.contains( name ) )
                throw new RuntimeException( "not allowed to create [" + name + "] in json" );

            JSFunction func = getJsonScope().getFunction( name );

            Scope s = getJsonScope().child();
            Object newThing = func.newOne();
            s.setThis( newThing );
            
            func.call( s , args );
            return newThing;
        }
        
        

        Debug.printTree( n , 0 );
        throw new RuntimeException( "don't know how to convert from json: " + Token.name( n.getType() ) );
    }

    static Object[] getArgs( Node start , ScriptOrFnNode script ){
        List l = new ArrayList();
        while ( start != null ){
            l.add( build( start , script ) );
            start = start.getNext();
        }
        return l.toArray();
    }

    private static Scope JSON_SCOPE;
    static Scope getJsonScope(){
        if ( JSON_SCOPE == null )
            JSON_SCOPE = Scope.newGlobal();
        return JSON_SCOPE;
    }

    static final Set<String> ALLOWED_FUNCTIONS = new HashSet<String>();
    static {
        ALLOWED_FUNCTIONS.add( "ObjectId" );
    }
}
