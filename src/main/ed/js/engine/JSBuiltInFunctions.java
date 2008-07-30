// JSBuiltInFunctions.java

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
import java.lang.reflect.*;

import com.twmacinta.util.*;

import ed.log.*;
import ed.js.*;
import ed.js.func.*;
import ed.io.*;
import ed.net.*;
import ed.util.*;
import ed.security.*;

/**
 * @anonymous name : {SYSOUT}, desc : {Dumps a string to standard output.}, param : {type : (string), name : (s), desc : (the string to print)}
 * @anonymous name : {assert}, desc : {Verifies a given condition and terminates the flow of execution if false.} param : {type : (boolean), name : (cond), desc : (condition to test)}
 * @anonymous name : {download}, desc : {Downloads the file at a given URL.}, return : {type : (file), desc : (downloaded file)}, param : {type : (string), name : (url), desc : (url of the file to download)}
 * @anonymous name : {fork}, desc : {Creates a new thread to run a given function.  Once created, the thread can be run and it's return data fetched, as shown in the example.}, example : { x = fork(function() { return 3; });
 x.run();
 x.returnData(); // will print 3 } param : {type : (function), name : (f), desc : (function to execute in a separate thread)}, return : {type : (thread), desc : (the thread generated)}
 * @anonymous name : {isAlpha} desc : {Determines if the input is a single alphabetic character.} return : {type : (boolean), desc : (if the given string is a single alphabetic character)}, param : { type : (string), name : (ch), desc : (character to check)}
 * @anonymous name : {isArray} desc : {Checks that a given object is an array.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is an array)}
 * @anonymous name : {isBool} desc : {Checks that a given object is a boolean value.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a boolean value)}
 * @anonymous name : {isDate} desc : {Checks that a given object is a date object.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a date object)}
 * @anonymous name : {isDigit} desc : {Checks that a given object is a string representing a single digit.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a string representing a single digit)}
 * @anonymous name : {isFunction} desc : {Checks that a given object is a function.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a function)}
 * @anonymous name : {isNumber} desc : {Checks that a given object is a number.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a number)}
 * @anonymous name : {isObject} desc : {Checks that a given object is an object.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is an object)}
 * @anonymous name : {isRegExp} desc : {Checks that a given object is a regular expression.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a regular expression)}
 * @anonymous name : {isRegex} desc : {Checks that a given object is a regular expression.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a regular expression)}
 * @anonymous name : {isSpace} desc : {Checks that a given object is a single whitespace character.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a single whitespace character)}
 * @anonymous name : {isString} desc : {Checks that a given object is a string.} param : {type : (any), name : (arr), desc : (object to check)}, return : {type : (boolean), desc : (if the object is a string)}
 * @anonymous name : {javaStatic} desc : {Calls a static java function.}, param : {type : (string), name : (cls), desc : (Java class to call)}, param : {type : (string), name : (method), desc : (method to call within the class)}, param : {type : (any), name : (params), desc : (some number of arguments to be passed to the java function)}
 * @anonymous name : {javaStaticProp}, desc : {Returns the value of a given static Java property.}, param : {type : (string), name : (cls), desc : (Java class)}, param : {type : (string), name : (property), desc : (method to call within the class)}, return : {type : (any), desc : (the value of the requested property)}
 * @anonymous name : {md5} desc : {Returns an md5 encoding of the given object.} param : {type : (any), name : (thing), desc : (object to be encoded)}, return : {type : (string), desc : (md5 hash of the given object)}
 * @anonymous name : {parseBool} desc : {Converts an object into a boolean value.  Objects that, when converted to a string, start with "t", "T", or "1" are true.  Everything else is false. } param : {type : (any), name : (obj), desc : (object to be converted into a boolean)}, return : { type : (boolean) desc : (the boolean equivalent of the given object)}
 * @anonymous name : {parseDate} desc : {Converts a date or string into a date object.} param : {type : (string|Date), name : (d), desc : (object to be converted into a date)}, return : { type : (Date) desc : (the date equivalent of the given object)}
 * @anonymous name : {parseNumber} desc : {Converts an object into a numeric value. } param : {type : (any), name : (obj), desc : (object to be converted into a number)}, return : { type : (number) desc : (the numeric equivalent of the given object)}
 * @anonymous name : {printnoln} desc : {Prints a string with no terminating newline.} param : {type : (string) name : (str), desc : (string to print)}
 * @anonymous name : {processArgs} desc : {Assigns the values passed to a function in the variable <tt>arguments</tt> to a list of given variable names.} param : {type : (any) name : (param), desc : (a series of variable names to which to assign arguments)}
 * @anonymous name : {sleep} desc : {Pauses the thread's execution for a given number of milliseconds.} param : {type : (number) name : (ms), desc : (the number of milliseconds for which to pause)}
 * @anonymous name : {sysexec} desc : {Executes a system command.} param : {type : (string) name : (cmd), desc : (command to execute)}, param : {type : (string), name : (in) desc : (input to command) isOptional : (true)}, param : {isOptional : (true) type : (Object), name : (env), desc : (environmental variables to use)} param : {type : (string) name : (loc) desc : (path from which to execute the command) isOptional : (true)} return : { type : (Object) desc : (the output of the command)}
 * @expose
 */
public class JSBuiltInFunctions {

    static {
        JS._debugSIStart( "JSBuiltInFunctions" );
    }

    /** Returns a new scope in which the builtin functions are defined.
     * @return the new scope
     */
    public static Scope create(){
        return create( "Built-In" );
    }

    /** Returns a new scope with a given name in which the builtin functions are defined.
     * @return the new scope
     */
    public static Scope create( String name ){
        Scope s = new Scope( name , _base );
        try {
            _setup( s );
        }
        catch ( RuntimeException re ){
            re.printStackTrace();
            System.exit(-1);
        }
        s.setGlobal( true );
        s.lock();
        return s;
    }

    public static class jsassert extends JSFunctionCalls1 {
        public jsassert(){
            JSFunction myThrows = new JSFunctionCalls2(){
                    public Object call( Scope scope , Object exctype, Object f,
                                        Object extra[] ){
                        if( ! ( f instanceof JSFunction ) ){
                            throw new RuntimeException( "Second argument to assert.throws must be a function" );
                        }
                        try {
                            ((JSFunction)f).call( scope , null );
                        }
                        catch(JSException e){
                            if( exctype instanceof JSString || exctype instanceof String ){
                                if( e.getObject().equals( exctype.toString() ) )
                                    return Boolean.TRUE;
                            }
                            Object desc = e.getObject();
                            if( desc instanceof Throwable && match( (Throwable) desc , exctype ) )
                                return Boolean.TRUE;

                            Throwable cause = e.getCause();
                            if( match( cause , exctype ) )
                                return Boolean.TRUE;

                            throw new JSException( "given function threw something else: " + cause.toString() );
                        }
                        catch(Throwable e){
                            if( match( e , exctype.toString() ) ) {
                                return Boolean.TRUE;
                            }

                            // FIXME: what if exctype is a JSFunction (i.e.
                            // an exception type?
                            // Find out how to do instanceof in JS API

                            throw new JSException( "given function threw something else: " + e.toString() );
                        }
                        // Didn't throw anything
                        throw new JSException( "given function did not throw " + exctype );

                    }
                };

            set("throws", myThrows);
            set("raises", myThrows);
        }

        public Object call( Scope scope , Object foo , Object extra[] ){
            if ( JSInternalFunctions.JS_evalToBool( foo ) )
                return Boolean.TRUE;

            if ( extra != null && extra.length > 0 && extra[0] != null )
                throw new JSException( "assert failed : " + extra[0] );

            throw new JSException( "assert failed" );
        }

        public boolean match( Throwable e , Object exctype ){
            if( exctype instanceof JSString || exctype instanceof String ){
                String s = exctype.toString();
                String gotExc = e.getClass().toString();

                if( gotExc.equals( "class " + s ) ) return true;
                if( gotExc.equals( "class java.lang." + s ) )
                    return true;

                if( e instanceof JSException && ((JSException)e).getObject().equals( s ) )
                    return true;

                // FIXME: check subclasses?
            }
            return false;
        }
    }

    /** @unexpose */
    public static Class _getClass( String name )
        throws Exception {

        final int colon = name.indexOf( ":" );

        if ( colon < 0 )
            return Class.forName( name );

        String base = name.substring( 0 , colon );
        Class c = Class.forName( base );

        String inner = "$" + name.substring( colon + 1 );
        for ( Class child : c.getClasses() ){
            if ( child.getName().endsWith( inner ) )
                return child;
        }

        throw new JSException( "can't find inner class [" + inner + "] on [" + c.getName() + "]" );

    }

    public static class javaCreate extends JSFunctionCalls1 {
        public Object call( Scope scope , Object clazzNameJS , Object extra[] ){

            String clazzName = clazzNameJS.toString();

            if ( ! Security.isCoreJS() )
                throw new JSException( "you can't do create a :" + clazzName + " from [" + Security.getTopJS() + "]" );

            Class clazz = null;
            try {
                clazz = _getClass( clazzName );
            }
            catch ( Exception e ){
                throw new JSException( "can't find class for [" + clazzName + "]" );
            }

            Constructor[] allCons = clazz.getConstructors();
            Arrays.sort( allCons , Scope._consLengthComparator );
            for ( int i=0; i<allCons.length; i++ ){

                Object params[] = Scope.doParamsMatch( allCons[i].getParameterTypes() , extra );

                if ( params != null ){
                    try {
                        return allCons[i].newInstance( params );
                    }
                    catch ( RuntimeException re ){
                        ed.lang.StackTraceHolder.getInstance().fix( re );
                        throw re;
                    }
                    catch ( Exception e ){
                        throw new JSException( "can' instantiate" , e );
                    }
                }

            }

            throw new RuntimeException( "can't find valid constructor" );
        }
    }

    public static class javaStatic extends JSFunctionCalls2 {
        public Object call( Scope scope , Object clazzNameJS , Object methodNameJS , Object extra[] ){
            final boolean debug = false;

            String clazzName = clazzNameJS.toString();

            if ( ! Security.isCoreJS() )
                throw new JSException( "you can't use a :" + clazzName + " from [" + Security.getTopJS() + "]" );

            Class clazz = null;
            try {
                clazz = _getClass( clazzName );
            }
            catch ( Exception e ){
                throw new JSException( "can't find class for [" + clazzName + "]" );
            }

            Method[] all = clazz.getMethods();
            Arrays.sort( all , Scope._methodLengthComparator );
            for ( int i=0; i<all.length; i++ ){
                Method m = all[i];
                if ( debug ) System.out.println( m.getName() );

                if ( ( m.getModifiers() & Modifier.STATIC ) == 0  ){
                    if ( debug ) System.out.println( "\t not static" );
                    continue;
                }

                if ( ! m.getName().equals( methodNameJS.toString() ) ){
                    if ( debug ) System.out.println( "\t wrong name" );
                    continue;
                }

                Object params[] = Scope.doParamsMatch( m.getParameterTypes() , extra , debug );
                if ( params == null ){
                    if ( debug ) System.out.println( "\t params don't match" );
                    continue;
                }

                try {
                    return m.invoke( null , params );
                }
                catch ( Exception e ){
                    e.printStackTrace();
                    throw new JSException( "can't call" , e );
                }

            }

            throw new RuntimeException( "can't find valid method" );
        }
    }

    public static class javaStaticProp extends JSFunctionCalls2 {
        public Object call( Scope scope , Object clazzNameJS , Object fieldNameJS , Object extra[] ){


            String clazzName = clazzNameJS.toString();

            if ( ! Security.isCoreJS() )
                throw new JSException( "you can't use a :" + clazzName + " from [" + Security.getTopJS() + "]" );

            Class clazz = null;
            try {
                clazz = _getClass( clazzName );
            }
            catch ( JSException e ){
                throw e;
            }
            catch ( Exception e ){
                throw new JSException( "can't find class for [" + clazzName + "]" );
            }

            try {
                return clazz.getField( fieldNameJS.toString() ).get( null );
            }
            catch ( NoSuchFieldException n ){
                throw new JSException( "can't find field [" + fieldNameJS + "] from [" + clazz.getName() + "]" );
            }
            catch ( Exception e ){
                throw new JSException( "can't get field [" + fieldNameJS + "] from [" + clazz.getName() + "] b/c " + e );
            }
        }
    }

    public static class print extends JSFunctionCalls1 {
        print(){
            this( true );
        }

        print( boolean newLine ){
            super();
            _newLine = newLine;
        }

        public Object call( Scope scope , Object foo , Object extra[] ){
            if ( _newLine )
                System.out.println( foo );
            else
                System.out.print( foo );
            return null;
        }

        final boolean _newLine;
    }

    public static class NewObject extends JSFunctionCalls0{

        public Object call( Scope scope , Object extra[] ){
            return new JSObjectBase();
        }

        public Object get( Object o ){
            if ( o == null )
                return null;

            if ( o.toString().equals( "prototype" ) )
                return JSObjectBase._objectLowFunctions;

            return super.get( o );
        }

        protected void init(){
            /**
             * Copies all properties from the source to the destination object.
             * Not in JavaScript spec! Please refer to Prototype docs!
             */
            set( "extend", new Prototype.Object_extend() );
            set( "values", new Prototype.Object_values() );
            set( "keys", new Prototype.Object_keys() );
        }
    };


    public static class NewDate extends JSFunctionCalls1 {
        public Object call( Scope scope , Object t , Object extra[] ){

            if ( t == null )
                return new JSDate();

            if ( ! ( t instanceof Number ) )
                return new JSDate();

            return new JSDate( ((Number)t).longValue() );
        }
    }

    public static class CrID extends JSFunctionCalls1 {

        public Object call( Scope scope , Object idString , Object extra[] ){
            if ( idString == null )
                return ed.db.ObjectId.get();
            if ( idString instanceof ed.db.ObjectId )
                return idString;
            return new ed.db.ObjectId( idString.toString() );
        }

        public JSObject newOne(){
            throw new JSException( "ObjectId is not a constructor" );
        }
    }

    public static class sleep extends JSFunctionCalls1 {
        public Object call( Scope scope , Object timeObj , Object extra[] ){
            if ( ! ( timeObj instanceof Number ) )
                return false;

            try {
                Thread.sleep( ((Number)timeObj).longValue() );
            }
            catch ( Exception e ){
                return false;
            }

            return true;
        }
    }

    public static class isXXX extends JSFunctionCalls1 {
        isXXX( Class c ){
            _c = c;
        }

        public Object call( Scope scope , Object o , Object extra[] ){
            return _c.isInstance( o );
        }

        final Class _c;
    }

    public static class isNaN extends JSFunctionCalls1 {

        public Object call( Scope scope , Object o , Object extra[] ){
            return o.equals(Double.NaN);
        }
    }

    public static class isXXXs extends JSFunctionCalls1 {
        isXXXs( Class ... c ){
            _c = c;
        }

        public Object call( Scope scope , Object o , Object extra[] ){
            for ( int i=0; i<_c.length; i++ )
                if ( _c[i].isInstance( o ) )
                    return true;
            return false;
        }

        final Class _c[];
    }


    public static class fork extends JSFunctionCalls1 {

        public Object call( final Scope scope , final Object funcJS , final Object extra[] ){

            if ( ! ( funcJS instanceof JSFunction ) )
                throw new JSException( "fork has to take a function" );

            final JSFunction func = (JSFunction)funcJS;
            final Thread t = new Thread( "fork" ){
                    public void run(){
                        try {
                            _result = func.call( scope , extra );
                        }
                        catch ( Throwable t ){
                            if ( scope.get( "log" ) != null )
                                ((Logger)scope.get( "log" ) ).error( "error in fork" , t );
                            else
                                t.printStackTrace();
                        }
                    }

                    public Object returnData()
                        throws InterruptedException {
                        join();
                        return _result;
                    }

                    private Object _result;
                };
            return t;
        }
    }
    
    public static class processArgs extends JSFunctionCalls0 {
        public Object call( Scope scope , Object [] args){
            JSArray a = (JSArray)scope.get("arguments");
            for(int i = 0; i < args.length; i++){
                scope.put(args[i].toString(), a.getInt(i), true);
            }
            return null;
        }
    }


    /** Returns if a given scope is the main scope.
     * @param the scope to check
     * @return if the given scope is the main scope
     */
    public static final boolean isBase( Scope s ){
        return s == _base;
    }

    private static final Scope _base; // these are things that aren't modifiable, so its safe if there is only 1 copy
    //private static final Scope _myScope; // this is the security hole.  need to get rid off TODO
    static {

        Scope s = new Scope( "base" , null );
        try {
            _setupBase( s );

        }
        catch ( RuntimeException re ){
            re.printStackTrace();
            System.exit( -1 );
        }
        finally {
            _base = s;
            _base.lock();
            _base.setGlobal( true );
        }
    }

    /**
     * everything that gets put into the scope that is a JSObjetBase gets locked
     */
    private static void _setupBase( Scope s ){
        s.put( "sysexec" , new ed.io.SysExec() , true );
        s.put( "print" , new print() , true );
        s.put( "printnoln" , new print( false ) , true );
        s.put( "SYSOUT" , new print() , true );
        s.put( "sleep" , new sleep() , true );
        s.put( "fork" , new fork() , true );

        CrID crid = new CrID();
        s.put( "CrID" , crid , true );
        s.put( "ObjID" , crid , true );
        s.put( "ObjId" , crid , true );
        s.put( "ObjectID" , crid , true );
        s.put( "ObjectId" , crid , true );

        s.put( "parseBool" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object b , Object extra[] ){
                    if ( b == null )
                        return false;

                    String s = b.toString();
                    if ( s.length() == 0 )
                        return false;

                    char c = s.charAt( 0 );
                    return c == 't' || c == 'T' || c == '1';
                }
            } , true );

	s.put( "parseFloat" ,
                      new JSFunctionCalls1(){
                          public Object call( Scope scope , Object a , Object extra[] ){

                              if ( a == null )
                                  return Double.NaN;

                              try {
                                  return Double.parseDouble( a.toString() );
                              }
                              catch ( Exception e ){}

                              return Double.NaN;
                          }
                      }
                      , true );
	s.put( "parseInt" ,
                      new JSFunctionCalls2(){
                          public Object call( Scope scope , Object a , Object b , Object extra[] ){

                              if ( a == null )
                                  return Double.NaN;

                              if ( a instanceof Number )
                                  return ((Number)a).intValue();

                              String s = a.toString();
                              try {
                                  if ( b != null && b instanceof Number ){
                                      return StringParseUtil.parseIntRadix( s , ((Number)b).intValue() );
                                  }

                                  return StringParseUtil.parseIntRadix( s , 10 );
                              }
                              catch ( Exception e ){}

                              return Double.NaN;
                          }
                      }
                      , true );

        s.put( "parseDate" ,
                      new JSFunctionCalls1(){
                          public Object call( Scope scope , Object a , Object extra[] ){
                              if ( a == null )
                                  return null;

                              if ( a instanceof JSDate )
                                  return a;

                              if ( ! ( a instanceof String || a instanceof JSString ) )
                                  return null;

                              long t = JSDate.parseDate( a.toString() , 0 );
                              if ( t == 0 )
                                  return null;

                              return new JSDate( t );
                          }
                      } , true );

        s.put( "NaN" , Double.NaN , true );

        s.put( "md5" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object b , Object extra[] ){
                    synchronized ( _myMd5 ){
                        _myMd5.Init();
                        _myMd5.Update( b.toString() );
                        return new JSString( _myMd5.asHex() );
                    }
                }

                private final MD5 _myMd5 = new MD5();

            } , true );

        s.put( "isArray" , new isXXX( JSArray.class ) , true );
        s.put( "isBool" , new isXXX( Boolean.class ) , true );
        s.put( "isNumber" , new isXXX( Number.class ) , true );
        s.put( "isDate" , new isXXX( JSDate.class ) , true );
        s.put( "isFunction" , new isXXX( JSFunction.class ) , true );
        s.put( "isRegExp" , new isXXX( JSRegex.class ) , true );
        s.put( "isRegex" , new isXXX( JSRegex.class ) , true );

        s.put( "isNaN", new isNaN(), true);

        s.put( "isString" , new isXXXs( String.class , JSString.class ) , true );

        s.put( "isObject" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    if ( o == null )
                        return false;

                    if ( ! ( o instanceof JSObject ) )
                        return false;

                    if ( o instanceof JSString )
                        return false;

                    return true;
                }
            } , true );


        s.put( "isAlpha" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    char c = getChar( o );
                    return Character.isLetter( c );
                }
            } , true );
        s.put( "isSpace" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    char c = getChar( o );
                    return Character.isWhitespace( c );
                }
            } , true );
        s.put( "isDigit" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                     char c = getChar( o );
                    return Character.isDigit( c );
                }
            } , true );

        s.put( "__self" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    return o;
                }
            } , true );

        s.put( "assert" , new jsassert() , true );
        s.put( "javaCreate" , new javaCreate() , true );
        s.put( "javaStatic" , new javaStatic() , true );
        s.put( "javaStaticProp" , new javaStaticProp() , true );

        s.put( "JSCaptcha" , new JSCaptcha() , true );
        s.put( "MimeTypes" , new ed.appserver.MimeTypes() , true );
        s.put( "Base64" , new ed.util.Base64() , true );
        s.put( "download" , new HttpDownload.downloadFunc() , true );

        s.put( "processArgs", new processArgs(), true );

	s.put( "XML" , E4X.CONS , true );

        // mail stuff till i'm done
        s.put( "JAVAXMAILTO" , javax.mail.Message.RecipientType.TO , true );
	
        JSON.init( s );
        Encoding.install( s );

        for ( String key : s.keySet() ){
            Object val = s.get( key );
            if ( val instanceof JSObjectBase )
                ((JSObjectBase)val).lock();
        }

        ed.db.migrate.Drivers.init( s );
    }


    private static void _setup( Scope s ){

        // core js

        s.put( "Object" , new NewObject() , true );
        s.put( "Array" , new JSArray.JSArrayCons() , true );
        s.put( "Date" , new JSDate.Cons() , true );
        s.put( "JSDate" , s.get( "Date" ) , true ); // b/c Eliot always types this
        s.put( "String" , new JSString.JSStringCons() , true );
        
        s.put( "RegExp" , new JSRegex.Cons() , true );
        s.put( "Regexp" , s.get( "RegExp" ) , true ); // for Ruby technically
        s.put( "XMLHttpRequest" , XMLHttpRequest._cons , true );
        s.put( "Function" , new JSInternalFunctions.FunctionCons() , true );
        s.put( "Math" , JSMath.getInstance() , true );
        s.put( "Class", ed.js.Prototype._class , true );
	s.put( "Number" , JSNumber.CONS , true );
	s.put( "parseNumber" , JSNumber.CONS , true );

        // extensions
        
        s.put( "Exception" , new JSException.cons() , true );
        s.put( "Map" , new JSMap.Cons() , true );

        s.lock();
    }

    private static char getChar( Object o ){

        if ( o instanceof Character )
            return (Character)o;

        if ( o instanceof JSString )
            o = o.toString();

        if ( o instanceof String ){
            String s = (String)o;
            if ( s.length() == 1 )
                return s.charAt( 0 );
        }

        return 0;
    }

    static {
        JS._debugSIDone( "JSBuiltInFunctions" );
    }
}
