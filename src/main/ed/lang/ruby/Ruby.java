// Ruby.java

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

package ed.lang.ruby;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.*;

public class Ruby {
    
    public static final String RUBY_V_CALL = "_rubyVCall";
    public static final String RUBY_CV_CALL = "_rubyCVCall";
    public static final String RUBY_NEW = "_rubyNew";
    public static final String RUBY_INCLUDE = "_rinclude";
    public static final String RUBY_RESCURE_INSTANCEOF = "__rrescueinstanceof";
    public static final String RUBY_TOARRAY = "__rtoarray";
    public static final String RUBY_RETURN = "__rreturn";
    
    public static final String RUBY_NEWNAME = "__rnew";
    public static final String RUBY_SHIFT = "__rshift";
    public static final String RUBY_PRIVATE = "__rprivate";
    public static final String RUBY_PROTECTED = "__rprivate";
    public static final String RUBY_REQUIRE = "__rrequire";
    public static final String RUBY_RAISE = "__rraise";
    public static final String RUBY_DEFINE_CLASS = "__rdefineclass";
    public static final String RUBY_RANGE = "__rrange";
    public static final String RUBY_BUILD_HASH = "__rbuildHash";

    static final Map<String,String> _nameMapping = new TreeMap<String,String>();
    static {
        _nameMapping.put( "new" , RUBY_NEWNAME );
        _nameMapping.put( "private" , RUBY_PRIVATE );
        _nameMapping.put( "protected" , RUBY_PROTECTED );
        _nameMapping.put( "<<" , RUBY_SHIFT );
        _nameMapping.put( "require" , RUBY_REQUIRE );
        _nameMapping.put( "raise" , RUBY_RAISE );

    }

    public static boolean alreadyInstalled( Scope s ){
	if ( s == null )
	    return false;
	
	s = s.getGlobal( true );
	return s.get( "Ruby" ) != null;
    }

    static Scope getScopeToUse( Scope s ){
	AppRequest ar = AppRequest.getThreadLocal();
	if ( ar != null )
	    return ar.getContext().getScope();

	if ( s == null ){
	    s = Scope.getThreadLocal();
	    if ( s == null )
		return null;
	}

	return s.getGlobal( true );
    }

    public static void install( Scope s ){
	s = getScopeToUse( s );
	if ( s == null ){
	    return;
	}

	if ( alreadyInstalled( s ) )
	    return;
	
	Scope defer = s.getTLPreferred();
	try {
	    s.setTLPreferred( null );
	    _install( getScopeToUse( s ) );
	}
	finally {
	    s.setTLPreferred( defer );
	}
    }

    private static void _install( final Scope s ){

        final JSObjectBase r = new JSObjectBase();
        s.put( "Ruby" , r , true );
        final JSArray loadPath = new JSArray();
        r.set( "loadPath" , loadPath );

        s.put( RUBY_V_CALL , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo , Object extra[] ){
                    
                    String name = null;
                    Object useThis = s.getThis();
                    if ( extra != null && extra.length > 1 )
                        useThis = extra[1];

                    if ( foo == null && extra != null && extra.length > 0 ){
                        name = extra[0].toString();
                        
                        if ( extra.length >  1 ){
                            JSObject tt = (JSObject)(extra[1]);
                            foo = tt.get( name );
                            if( foo != null )
                                useThis = tt;
                        }
                        
                        if ( foo == null && s.getThis() instanceof JSObject ){
                            JSObject tt = (JSObject)s.getThis();
                            foo = tt.get( name );
                            if( foo != null )
                                useThis = tt;
                        }
                    }
                    
                    if ( ! ( foo instanceof JSFunction ) )
                        return foo;
                    
                    JSFunction f = (JSFunction)foo;
                    if ( useThis != null ){
                        return f.callAndSetThis( s , useThis , null );
                    }
                    return f.call( s );

                }
            } , true );

        final JSFunctionCalls2 _cvCall = 
            new JSFunctionCalls2(){
                public Object call( Scope s , Object thing , Object funcName , Object extra[] ){
                    
                    Object useThis = thing;

                    if ( thing == null ){
                        if ( funcName != null ){
                            String temp = funcName.toString();
                            if ( temp.equals( "blank_q_" ) )
                                return true;
                            if ( temp.equals( "to_s" ) )
                                return "";
                            if ( temp.equals( "nil_q_" ) )
                                return true;
                            
                            throw new NullPointerException( "trying to get [" + temp + "] from a null item" );
                        }
                        throw new NullPointerException();
                    }
                    
                    if ( funcName == null )
                        throw new NullPointerException( "funcName can't be null" );
                    
                    if ( thing instanceof Number )
                        thing = JSNumber.getFunctions();

                    if ( thing instanceof Boolean )
                        thing = JSBoolean.functions;

                    if ( ! ( thing instanceof JSObject) )
                        return NativeBridge.callNative( s , thing , funcName.toString() , null );
                    

                    JSObject jo = (JSObject)thing;
                    
                    Object func = jo.get( RubyConvert._mangleFunctionName( funcName.toString() ) );
		    
                    if ( func == null ){
			if ( thing.getClass() != JSObjectBase.class )
			    return NativeBridge.callNative( s , thing , funcName.toString() , null );
                        return null;
		    }
                    
                    if ( ! ( func instanceof JSFunction ) )
                        return func;
                    
                    JSFunction f = (JSFunction)func;
                    return f.callAndSetThis( s , useThis , extra );
                }
            };
        
        s.put( RUBY_CV_CALL , _cvCall , true );
        
        s.put( RUBY_NEW , new JSFunctionCalls1(){
                public Object call( Scope s , Object thing , Object extra[] ){
                    if ( thing == null )
                        throw new NullPointerException( "need a function or a constructor" );
                    
                    if ( ! ( thing instanceof JSFunction ) )
                        return _cvCall.call( s , thing , RUBY_NEWNAME , extra );
                    
                    JSObjectBase o = new JSObjectBase();
                    o.setConstructor( (JSFunction)thing , true , extra );
                    return o;
                }
            } , true );

        s.put( RUBY_INCLUDE , new JSFunctionCalls1(){
                public Object call( Scope s , Object thing , Object extra[] ){

                    if ( thing == null )
                        throw new NullPointerException( "tried to include a null thing" );
                    
                    if ( thing instanceof JSObject ){
                        JSObject t = (JSObject)s.getThis();
                        if ( t.get( "__globalThis") != null )
                            t = s;

                        JSObject o = (JSObject)thing;
                        
                        for ( String key : o.keySet() )
                            t.set( key , o.get( key ) );
                        
                        Object incObj = o.get( "included" );
                        if ( incObj != null && incObj instanceof JSFunction )
                            ((JSFunction)incObj).call( s , t );
                        
                        return null;
                    }

                    throw new RuntimeException( "don't know what to do ");
                }                
            } , true );

        // ---

        s.put( "attr_accessor" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_accessor needs it" );
                    return null;
                }
            } , true );


        s.put( "attr_accessible" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_accessor needs it" );
                    return null;
                }
            } , true );

        s.put( "attr_reader" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_reader needs it" );
                    return null;
                }
            } , true );

        s.put( "attr_writer" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_writer needs it" );
                    return null;
                }
            } , true );

        s.put( "attr_protected" , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    JSObjectBase job = (JSObjectBase)s.getThis();
                    if ( job == null )
                        throw new NullPointerException( "no this and attr_writer needs it" );
                    return null;
                }
            } , true );
        
        
        // ---

        s.put( RUBY_PRIVATE , new JSFunctionCalls0(){
                public Object call( Scope s , Object symbols[] ){
                    return null;
                }
            } , true );

        r.set( "libPath" , new JSArray() );
        
        r.set( "defined" , new JSFunctionCalls1(){
                public Object call( Scope s , Object o , Object extra[] ){
                    return o != null;
                }
            } );

        s.put( RUBY_REQUIRE , new JSFunctionCalls1(){
                
                Map<String,JSFileLibrary> _libs = new TreeMap<String,JSFileLibrary>();

                public Object call( Scope s , Object pathObj , Object extra[] ){
                    if ( pathObj == null )
                        throw new NullPointerException( "can't send require nothing" );

                    String path = pathObj.toString();
                    int idx = path.lastIndexOf( "." );
                    if ( idx > 0 ){
                        int slash = path.lastIndexOf( "/" );
                        if ( idx > slash )
                            path = path.substring( 0 , idx );
                    }

                    path = path.replaceAll( "//+" , "/" );

                    Object thing = s.get("__path__" ) == null ? null : ((JSFileLibrary)s.get( "__path__" )).getFromPath( path );
                    
                    JSFileLibrary local = (JSFileLibrary)s.get( "local" );

                    if ( thing == null )
                        thing = local.getFromPath( "lib/" + path );
                    
                    if ( thing == null ){
                        for ( Object o : ((JSArray)(r.get( "libPath" ))) ){
                            thing = local.getFromPath( o.toString() + "/" + path );
                            if ( thing != null )
                                break;
                        }
                    }
                    
                    if ( thing == null )
                        thing = ((JSFileLibrary)s.get( "core" )).getFromPath( "rails/lib/" + path );
                    
                    if ( thing == null )
                        thing = ((JSFileLibrary)s.get( "external" )).getFromPath( "ruby/current/" + path );
                    
                    if ( thing == null ){
                        for ( int i=0; i<loadPath.size(); i++ ){
                            String dir = loadPath.get(i).toString();
                            System.out.println( "here: " + dir );
                            JSFileLibrary lib = _libs.get( dir );
                            if ( lib == null ){
                                lib = new JSFileLibrary( new java.io.File( dir ) , null , s );
                                _libs.put( dir , lib );
                            }
                            thing = lib.getFromPath( path );
                            
                            if ( thing != null )
                                break;
                        }
                    }


                    if ( thing == null || ! ( thing instanceof JSFunction ) ){
                        s.getFunction( "raiseLoadError" ).call( s , "can't find [" + path + "]" );
                        throw new RuntimeException( "shouldn't be here, should have throw exception" );
                    }
                    
                    final JSFunction func = (JSFunction)thing;
                    
                    {
                        Object blah = func.get( "__required__success__" );
                        if ( blah != null ){
                            boolean bad = false;
                            for ( Object key : (Iterable)blah ){
                                if ( ! s.containsKey( key.toString() ) ){
                                    bad = true;
                                    break;
                                }
                                if ( ! bad )
                                    return null;
                            }
                        }
                    }
                    
                    List<String> before = new ArrayList<String>( s.getGlobal().keySet() );
                    final Object ret = func.call( s , null );
                    before.removeAll( s.getGlobal().keySet() );

                    func.set( "__required__success__" , before );
                    return ret;
                }
            } , true );

        s.put( RUBY_TOARRAY , new JSFunctionCalls0(){
                public Object call( Scope s , Object extra[] ){
                    if ( extra == null || extra.length == 0 )
                        return new JSArray();
                    
                    if ( extra.length == 1 && extra[0] instanceof JSArray )
                        return extra[0];
                    
                    return new JSArray( extra );
                }
            } , true );

        s.put( "__revstr" , new JSFunctionCalls1(){
                public Object call( Scope s , Object foo , Object extra[] ){
                    if ( foo == null )
                        return "";
                    
                    return foo.toString();
                }
            } , true );


        s.put( RUBY_RAISE , new JSFunctionCalls1(){

                final boolean debug = false;

                public Object call( Scope s , Object clazz , Object extra[] ){
                    RuntimeException e = _getException( s , clazz , extra );
                    if ( debug ) System.out.println( "going to throw : " + e );
                    throw e;
                }
                
                RuntimeException _getException( Scope s , Object clazz , Object extra[] ){
                    
                    if ( clazz == null || ! ( clazz instanceof JSFunction ) )
                        return new JSException( clazz == null ? "unnamed error" : clazz.toString() );    
                    
                    JSFunction func = (JSFunction)clazz;
                    if ( debug ) System.out.println( "going to raise : " + func.getClass() );
                    
                    Object e = func.newOne();
                    s = s.child();
                    s.setThis( e );

                    Object n = func.call( s , extra );
                    if ( n != null )
                        e = n;
                    
                    if ( debug ) System.out.println( "\t " + e.getClass() );
                    
                    if ( e instanceof RuntimeException )
                        throw ( RuntimeException)e;

                    
                    JSException ex = new JSException( e );
                    if ( extra != null && extra.length > 0 )
                        ex.setMessage( extra[0] );
                    return ex;

                } 
            } , true );
        
        
        s.put( RUBY_RESCURE_INSTANCEOF , new JSFunctionCalls2(){
                public Object call( Scope s , Object t , Object c , Object extra[] ){
                    
                    final boolean debug = false;

                    if ( debug ) System.out.println( "t    :" + ( t == null ? "null" : t.getClass() ) );
                    if ( debug ) System.out.println( "c    :" + ( c == null ? "null" : c.getClass() ) );
                    
                    if ( t instanceof RubyReturnHack )
                        return false;


                    if ( ! ( t instanceof JSObjectBase && 
                             c instanceof JSObjectBase ) )
                        return false;
                    
                    JSObjectBase thing = (JSObjectBase)t;
                    JSObjectBase clazz = (JSObjectBase)c;
                    
                    JSFunction cons = thing.getConstructor();
                    
                    if ( debug ) System.out.println( "cons :" + ( cons == null ? "null" : cons.getClass() ) );
                    
                    final boolean b =
                        cons == clazz ||
                        clazz.getClass() == cons.getClass();
                    
                    if ( debug ) System.out.println( "\t" + b );
                    return b;
                    
                }
            } , true );

        s.put( RUBY_DEFINE_CLASS , new JSFunctionCalls3(){
                public Object call( Scope s , Object old , Object func , Object name , Object extra[] ){
                    
                    if ( ! ( func instanceof JSFunction ) )
                        throw new RuntimeException( "somethingis wrong" );
                    
                    JSFunction n = (JSFunction)func;
                    n.set( "name" , name );
                    n.set( "_className" , name );
                    
                    if ( old instanceof JSFunction ){
                        
                        JSFunction o = (JSFunction)old;

                        if ( o.getPrototype() != null )
                            for ( String key : o.getPrototype().keySet() )
                                n.getPrototype().set( key , o.getPrototype().get( key ) );
                        
                    }
                    
                    return n;
                }
            } , true );

        s.put( "__rvarset" , new JSFunctionCalls2(){
                public Object call( Scope scope , Object obj , Object name , Object extra[] ){
                    
                    if ( obj == null || name == null )
                        throw new NullPointerException();

                    if ( ! ( obj instanceof JSObject ) )
                        throw new RuntimeException("trying to set something on a non-object");
                    
                    final JSObject o = (JSObject)obj;
                    final String n = name.toString();
                    
                    Object func = o.get( n + "_eq_" );
                    if ( ! ( func instanceof JSFunction ) )
                        return o.set( n , extra == null || extra.length == 0 ? null : extra[0] );
                    
                    return ((JSFunction)func).callAndSetThis( scope , o , extra );
                }
            } , true );

        s.put( RUBY_RANGE , new JSFunctionCalls2(){
                public Object call( Scope scope , Object start , Object end , Object extra[] ){

                    if ( start == null )
                        throw new NullPointerException( "range start is null" );

                    if ( end == null )
                        throw new NullPointerException( "range end is null" );

                    start = _rangeFix( start );
                    end = _rangeFix( end );

                    JSArray a = new JSArray();
                    a.set( "to_a" , new JSFunctionCalls0(){
                            public Object call( Scope s , Object foo[] ){
                                return s.getThis();
                            }
                        } );
                    
                    a.set( "start" , start );
                    a.set( "end" , end );
                    
                    if ( start instanceof Character && end instanceof Character ){
                        char s = (Character)start;
                        char e = (Character)end;
                        
                        while ( s <= e ){
                            a.add( s );
                            s++;
                        }
                        
                    }
                    else if ( start instanceof Number && end instanceof Number ){
                        int s = ((Number)start).intValue();
                        int e = ((Number)end).intValue();

                        while ( s <= e ){
                            a.add( s );
                            s++;
                        }
                    }
                    else {
                        throw new RuntimeException( "can't compare : " + start.getClass() + " and " + end.getClass() );
                    }

                    return a;
                    
                }
            } , true );

        s.put( RUBY_RETURN , new JSFunctionCalls1(){
                public Object call( Scope scope , Object thing , Object extra[] ){
                    throw new RubyReturnHack( thing );
                }
            } , true );
        
        s.put( "__risReturnThing" , new JSFunctionCalls1(){
                public Object call( Scope scope , Object thing , Object extra[] ){
                    if ( thing == null )
                        return false;
                    return thing instanceof RubyReturnHack;
                }
            }  , true );
            
        
        s.put( RUBY_BUILD_HASH , new JSFunctionCalls0(){
                public Object call( Scope s , Object things[] ){
                    
                    JSObjectBase o = new JSObjectBase();

                    if ( things == null )
                        return o;
                    
                    for ( int i=0; i<things.length; i+=2 ){
                        Object v = null;
                        if ( i + 1 < things.length ) 
                            v = things[i+1];
                        o.set( things[i] , v );
                    }

                    return o;
                }
            } , true );

        JSFileLibrary lib = JSFileLibrary.loadLibraryFromEd("ed/lang/ruby", "ruby", s);

        if (lib == null ){
            System.err.println( "can't find ruby root :  RUBY WILL NOT WORK" );
            return;
        }

        ((JSFunction)(lib.get( "lib" ))).call( s );
        ((JSFunction)(lib.get( "core" ))).call( s );
    }
    
    public static class RubyReturnHack extends RuntimeException {
        RubyReturnHack( Object obj ){
            super( "RubyReturnHack" );
            _obj = obj;
        }
        
        public Object getReturn(){
            return _obj;
        }

        final Object _obj;
    }

    static Object _rangeFix( Object o ){
        if ( o instanceof String || o instanceof JSString ){
            String s = o.toString();
            if ( s.length() > 1 )
                throw new RuntimeException( "can't range a string of length > 1" );
            return s.charAt( 0 );
        }
        return o;
    }
}
