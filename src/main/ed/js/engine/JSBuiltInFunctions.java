// JSBuiltInFunctions.java

package ed.js.engine;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import com.twmacinta.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.io.*;
import ed.net.*;
import ed.security.*;

public class JSBuiltInFunctions {

    public static class jsassert extends JSFunctionCalls1 {
        public Object call( Scope scope , Object foo , Object extra[] ){
            if ( JSInternalFunctions.JS_evalToBool( foo ) )
                return Boolean.TRUE;
                    
            if ( extra != null && extra.length > 0 && extra[0] != null )
                throw new JSException( "assert failed : " + extra[0] );
            
            throw new JSException( "assert failed" );
        }        
    }
    
    public static class javaCreate extends JSFunctionCalls1 {
        public Object call( Scope scope , Object clazzNameJS , Object extra[] ){

            String clazzName = clazzNameJS.toString();
            System.out.println( "want to create a : " + clazzName );
            
            if ( ! Security.isCoreJS() )
                throw new JSException( "you can't do create a :" + clazzName );
            
            Class clazz = null;
            try {
                clazz = Class.forName( clazzName );
            }
            catch ( Exception e ){
                throw new JSException( "can't find class for [" + clazzName + "]" );
            }
            
            Constructor[] allCons = clazz.getConstructors();
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
            
            
            String clazzName = clazzNameJS.toString();
            
            if ( ! Security.isCoreJS() )
                throw new JSException( "you can't use a :" + clazzName );
            
            Class clazz = null;
            try {
                clazz = Class.forName( clazzName );
            }
            catch ( Exception e ){
                throw new JSException( "can't find class for [" + clazzName + "]" );
            }
            
            Method[] all = clazz.getMethods();
            for ( int i=0; i<all.length; i++ ){
                
                Method m = all[i];
                if ( ( m.getModifiers() & Modifier.STATIC ) == 0  )
                    continue;
                
                if ( ! m.getName().equals( methodNameJS.toString() ) )
                    continue;

                Object params[] = Scope.doParamsMatch( m.getParameterTypes() , extra );
                
                if ( params != null ){
                    try {
                        return m.invoke( null , params );
                    }
                    catch ( Exception e ){
                        throw new JSException( "can' call" , e );
                    }
                }
                    
            }

            throw new RuntimeException( "can't find valid method" );
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

    public static class NewObject extends JSFunctionCalls0 {
        NewObject(){
            super();
        }

        public Object call( Scope scope , Object extra[] ){
            return new JSObjectBase();
        }
    }
    
    public static class NewArray extends JSFunctionCalls1 {
        NewArray(){
            super();
        }

        public JSObject newOne(){
            return new JSArray();
        }

        public Object call( Scope scope , Object a , Object[] extra ){
            int len = 0;
            if ( extra == null || extra.length == 0 ){
                if ( a instanceof Number )
                    len = ((Number)a).intValue();
            }
            else {
                len = 1 + extra.length;
            }
            
            JSArray arr = new JSArray( len );
            
            if ( extra != null && extra.length > 0 ){
                arr.setInt( 0 , a );
                for ( int i=0; i<extra.length; i++)
                    arr.setInt( 1 + i , extra[i] );
            }
            
            return arr;
        }
    }
    
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
            return new ed.db.ObjectId( idString.toString() );
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

    public static class sysexec extends JSFunctionCalls1 {
        
        // adds quotes as needed
	static String[] fix( String s ){
	    String base[] = s.split( "\\s+" );
            
	    List<String> fixed = new ArrayList();
	    boolean changed = false;
            
	    for ( int i=0; i<base.length; i++ ){

		if ( ! base[i].startsWith( "\"" ) ){
		    fixed.add( base[i] );
		    continue;
		}
            
		int end = i;
		while( end < base.length && ! base[end].endsWith( "\"" ) )
		    end++;
            
		String foo = base[i++].substring( 1 );
		for ( ; i<=end && i < base.length; i++ )
		    foo += " " + base[i];

		i--;

		if ( foo.endsWith( "\"" ) )
		    foo = foo.substring( 0 , foo.length() - 1 );
            
		fixed.add( foo );
		changed = true;
	    }

	    if ( changed ){
		System.out.println( fixed );
		base = new String[fixed.size()];
		for ( int i=0; i<fixed.size(); i++ )
		    base[i] = fixed.get(i);
	    }

	    return base;
	}

        public Object call( Scope scope , Object o , Object extra[] ){
            if ( o == null )
                return null;
            
            if ( ! Security.isCoreJS() )
                throw new JSException( "can't exec this" );

            File root = scope.getRoot();
            if ( root == null )
                throw new JSException( "no root" );
            
            String cmd[] = fix( o.toString() );
            String env[] = new String[]{};
	    
	    String toSend = null;
	    if ( extra != null && extra.length > 0 )
		toSend = extra[0].toString();

	    if ( extra != null && extra.length > 1 && extra[1] instanceof JSObject ){
		JSObject foo = (JSObject)extra[1];
		env = new String[ foo.keySet().size() ];
		int pos = 0;
		for ( String name : foo.keySet() ){
		    Object val = foo.get( name );
		    if ( val == null )
			val = "";
		    env[pos++] = name + "=" + val.toString();
		}
	    }

            try {
                Process p = Runtime.getRuntime().exec( cmd , env , root );
		
		if ( toSend != null ){
		    OutputStream out = p.getOutputStream();
		    out.write( toSend.getBytes() );
		    out.close();
		}

                JSObject res = new JSObjectBase();
                res.set( "out" , StreamUtil.readFully( p.getInputStream() ) );
                res.set( "err" , StreamUtil.readFully( p.getErrorStream() ) );
                
                return res;
            }
            catch ( Throwable t ){
                throw new JSException( t.toString() , t );
            }
            
        }        
    }

    
    static Scope _myScope = new Scope( "Built-Ins" , null );
    static {
        _myScope.put( "sysexec" , new sysexec() , true );
        _myScope.put( "print" , new print() , true );
        _myScope.put( "printnoln" , new print( false ) , true );
        _myScope.put( "SYSOUT" , new print() , true );
        _myScope.put( "sleep" , new sleep() , true );

        _myScope.put( "Object" , new NewObject() , true );
        _myScope.put( "Array" , new NewArray() , true );
        _myScope.put( "Date" , JSDate._cons , true );
        _myScope.put( "String" , JSString._cons , true );
        _myScope.put( "RegExp" , JSRegex._cons , true );
        _myScope.put( "XMLHttpRequest" , XMLHttpRequest._cons , true );

        _myScope.put( "Math" , JSMath.getInstance() , true );
        
        CrID crid = new CrID();
        _myScope.put( "CrID" , crid , true );
        _myScope.put( "ObjID" , crid , true );
        _myScope.put( "ObjId" , crid , true );
        _myScope.put( "ObjectID" , crid , true );
        _myScope.put( "ObjectId" , crid , true );

        _myScope.put( "Base64" , new ed.util.Base64() , true );
        
        _myScope.put( "download" , HttpDownload.DOWNLOAD , true );

        _myScope.put( "JSCaptcha" , new JSCaptcha() , true );


        _myScope.put( "parseBool" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object b , Object extra[] ){
                    if ( b == null )
                        return false;
                    
                    String s = b.toString();
                    if ( s.length() == 0 )
                        return false;
                    
                    char c = s.charAt( 0 );
                    return c == 't' || c == 'T';
                }
            } , true );
	
        JSFunctionCalls2 parseNumber = new JSFunctionCalls2(){
		public Object call( Scope scope , Object a , Object b , Object extra[] ){
                    Object r = JSInternalFunctions.parseNumber( a , b );
                    if ( r instanceof Number )
                        return r;
                    if ( b != null )
                        return b;
                    throw new RuntimeException( "not a number [" + a + "]" );
		}
            };

	_myScope.put( "parseNumber" , parseNumber , true );
	_myScope.put( "parseFloat" , parseNumber , true );
	_myScope.put( "parseInt" , parseNumber , true );
	_myScope.put( "Number" , parseNumber , true );
	
        _myScope.put( "md5" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object b , Object extra[] ){
                    synchronized ( _myMd5 ){
                        _myMd5.Init();
                        _myMd5.Update( b.toString() );
                        return new JSString( _myMd5.asHex() );
                    }
                }

                private final MD5 _myMd5 = new MD5();
                
            } , true );
        
        _myScope.put( "isArray" , new isXXX( JSArray.class ) , true );
        _myScope.put( "isBool" , new isXXX( Boolean.class ) , true );
        _myScope.put( "isNumber" , new isXXX( Number.class ) , true );
        _myScope.put( "isObject" , new isXXX( JSObject.class ) , true );
        _myScope.put( "isDate" , new isXXX( JSDate.class ) , true );
        _myScope.put( "isFunction" , new isXXX( JSFunction.class ) , true );

        _myScope.put( "isString" , new isXXXs( String.class , JSString.class ) , true );
        
        _myScope.put( "isAlpha" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    char c = getChar( o );
                    return Character.isLetter( c );
                }
            } , true );
        _myScope.put( "isSpace" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    char c = getChar( o );
                    return Character.isWhitespace( c );
                }
            } , true );
        _myScope.put( "isDigit" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                     char c = getChar( o );
                    return Character.isDigit( c );
                }
            } , true );
        
        _myScope.put( "assert" , new jsassert() , true );
        _myScope.put( "javaCreate" , new javaCreate() , true );
        _myScope.put( "javaStatic" , new javaStatic() , true );
        
        _myScope.put( "escape" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object o , Object extra[] ){
                    return java.net.URLEncoder.encode( o.toString() ).replaceAll( "\\+" , "%20" );
                }
            } , true );

        JSON.init( _myScope );
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
}
