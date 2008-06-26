// JSBuiltInFunctions.java

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

public class JSBuiltInFunctions {

    static {
        JS._debugSIStart( "JSBuiltInFunctions" );
    }

    public static Scope create(){
        return create( "Built-In" );
    }

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
        public Object call( Scope scope , Object foo , Object extra[] ){
            if ( JSInternalFunctions.JS_evalToBool( foo ) )
                return Boolean.TRUE;
                    
            if ( extra != null && extra.length > 0 && extra[0] != null )
                throw new JSException( "assert failed : " + extra[0] );
            
            throw new JSException( "assert failed" );
        }        
    }

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
                throw new JSException( "you can't do create a :" + clazzName );
            
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
            catch ( Throwable t ){
                throw new JSException( "can't get field [" + fieldNameJS + "] from [" + clazz.getName() + "] b/c " + t );
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

        s.put( "processArgs", new processArgs(), true );
        
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
        s.put( "Array" , JSArray._cons , true );
        s.put( "Date" , JSDate._cons , true );
        s.put( "JSDate" , JSDate._cons , true ); // b/c Eliot always types this
        s.put( "String" , JSString._cons , true );
        s.put( "RegExp" , JSRegex._cons , true );
        s.put( "Regexp" , JSRegex._cons , true ); // for Ruby technically
        s.put( "XMLHttpRequest" , XMLHttpRequest._cons , true );
        s.put( "Function" , JSInternalFunctions.FunctionCons , true );
        s.put( "Math" , JSMath.getInstance() , true );
        s.put( "Class", ed.js.Prototype._class , true );        
	s.put( "Number" , JSNumber.CONS , true );
	s.put( "parseNumber" , JSNumber.CONS , true );

        // extensions
        
        s.put( "Exception" , JSException._cons , true );
        s.put( "Map" , JSMap._cons , true );
        
        s.put( "download" , new HttpDownload.downloadFunc() , true );
        
        
        // packages
        ed.lang.ruby.Ruby.install( s );

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
