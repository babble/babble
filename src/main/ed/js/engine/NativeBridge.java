// NativeBridge.java

package ed.js.engine;

import java.util.*;
import java.lang.reflect.*;

import ed.js.*;
import ed.js.func.*;
import ed.lang.*;

public class NativeBridge {

    private static final Object[] EMPTY_OBJET_ARRAY = new Object[0];
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    
    private static final Set<String> _disallowedNativeNames = new HashSet<String>();
    static {
        _disallowedNativeNames.add( "getClassLoader" );
        _disallowedNativeNames.add( "loadClass" );
    }

    static final JSFunctionCalls0 _nativeFuncCall = new JSFunctionCalls0(){

            public Object call( Scope s , Object params[] ){
                
                final boolean debug = false;
                
                Scope.This temp = s._this.peek();
                if ( temp._this != null )
                    throw new RuntimeException( "why is this._this is not null : " + temp._this + " s:" + s.hashCode() );
                final Object obj = temp._nThis;
                final String name = temp._nThisFunc;
                
                if ( obj == null )
                    throw new NullPointerException( "object was null.  name was:" + name );
                
                if ( debug ) System.out.println( obj.getClass() + " : " + name );
                
                return callNative( s , obj , name , params , debug );
            }
        };
    
    private static final Map< Class , Map< String , List<Method> > > _classToMethods = new HashMap< Class , Map< String , List<Method> > >();
    
    private static List<Method> getMethods( Class c , String n ){
        Map<String,List<Method>> m = _classToMethods.get( c );
        if ( m == null ){
            m = new HashMap<String,List<Method>>();
            _classToMethods.put( c , m );
        }
        
        List<Method> l = m.get( n );
        if ( l != null )
            return l;
        
        l = new ArrayList<Method>();
        Method all[] = c.getMethods();
        Arrays.sort( all , _methodLengthComparator );
        for ( Method method : all )
            if ( method.getName().equals( n ) )
                l.add( method );
        m.put( n , l );
        return l;
    }


    public static Object callNative( Scope s , Object obj , String name , Object params[] ){
        return callNative( s , obj , name , params , false );
    }
    
    public static Object callNative( Scope s , Object obj , String name , Object params[]  , boolean debug ){
        
        if ( _disallowedNativeNames.contains( name ) )
            throw new JSException( "[" + name + "] is not allowed" );

        List<Method> methods = getMethods( obj.getClass() , name );
        if ( methods != null && methods.size() > 0 ){
            methods:
            for ( Method m : methods ){
                if ( debug ) System.out.println( "\t " + m.getName() );
                
                Object nParams[] = doParamsMatch( m.getParameterTypes() , params , debug );
                if ( nParams == null ){
                    if ( debug ) System.out.println( "\t\t boo" );
                    continue;
                }
                
                if ( debug ) System.out.println( "\t\t yay" );
                
                m.setAccessible( true );
                try {
                    Object ret = m.invoke( obj , nParams );
                    if ( ret != null ){
                        if ( ret instanceof String )
                            ret = new JSString( ret.toString() );
                        else if ( ret instanceof java.util.Date ) 
                            ret = new JSDate( (java.util.Date)ret );
                        else if ( ret instanceof java.util.Collection ){
                            if ( ! ( ret instanceof JSArray  ) ){
                                JSArray a = new JSArray();
                                for ( Object o : (Collection)ret )
                                    a.add( o );
                                ret = a;
                            }
                        }
                        else if ( ret.getClass().isArray() ){
                            if ( ret.getClass().getName().toString().length() > 3 ){ // primitive
                                JSArray a = new JSArray();
                                for ( Object o : ((Object[])ret) )
                                    a.add( o );
                                return a;
                            }
                        }
                    }
                    return ret;
                }
                catch ( InvocationTargetException e ){
                    StackTraceHolder.getInstance().fix( e.getCause() );
                    if ( e.getCause() instanceof RuntimeException )
                        throw (RuntimeException)(e.getCause());
                    if ( e.getCause() instanceof Error )
                        throw (Error)e.getCause();
                    throw new RuntimeException( e.getCause() );
                }
                catch ( RuntimeException e ){
                    throw e;
                }
                catch ( Exception e ){
                    throw new RuntimeException( e );
                }
            }
            
            throw new NullPointerException( "no method with matching params [" + name + "] (from a [" + obj.getClass() + "])" );
        }
                
        if ( obj.getClass() == JSObjectBase.class )
            throw new NullPointerException( "no function called : " + name + " fields [" + ((JSObjectBase)obj).keySet() + "]" );
        
        if ( obj instanceof ed.appserver.JSFileLibrary )
            throw new NullPointerException( "included file [" + ((ed.appserver.JSFileLibrary)obj).getURIBase().replaceAll( "^jxp" , "" )  + "/" + name + "] does not exist" );

        throw new NullPointerException( name + " (from a [" + obj.getClass() + "])" );

    }

    static Object[] doParamsMatch( Class myClasses[] , Object params[] ){
        return doParamsMatch( myClasses , params , false );
    }
    
    static Object[] doParamsMatch( Class myClasses[] , Object params[] , final boolean debug ){
        
        if ( myClasses == null )
            myClasses = EMPTY_CLASS_ARRAY;
        
        if ( params == null )
            params = EMPTY_OBJET_ARRAY;
        
        if ( myClasses.length > params.length ){
            Object n[] = new Object[myClasses.length];
            for ( int i=0; i<params.length; i++ )
                n[i] = params[i];
            params = n;
        }

        if ( myClasses.length != params.length ){
            if ( debug ) System.out.println( "param length don't match " + myClasses.length + " != " + params.length );
            return null;
        }
        
        for ( int i=0; i<myClasses.length; i++ ){

            // null is fine with me
            if ( params[i] == null ) 
                continue;
            
            Class myClass = myClasses[i];
            final Class origMyClass = myClass;
            
            if ( myClass == String.class )
                params[i] = params[i].toString();
            
            if ( myClass.isPrimitive() ){
                if ( myClass == Integer.TYPE || 
                     myClass == Long.TYPE || 
                     myClass == Double.TYPE ){
                    myClass = Number.class;
                }
                else if ( myClass == Boolean.TYPE ) 
                    myClass = Boolean.class;
            }
            
            
            if ( myClass.isArray() && params[i] instanceof JSArray ){
                params[i] = ((JSArray)params[i]).toArray();
                continue;
            }

            if ( ! myClass.isAssignableFrom( params[i].getClass() ) ){
                if ( debug ) System.out.println( "\t native assignement failed b/c " + myClasses[i] + " is not mappable from " + params[i].getClass() );
                return null;
            }
            
            if ( myClass == Number.class && origMyClass != params[i].getClass() ){
                Number theNumber = (Number)params[i];
                
                if ( origMyClass == Double.class || origMyClass == Double.TYPE )
                    params[i] = theNumber.doubleValue(); 
                else if ( origMyClass == Integer.class || origMyClass == Integer.TYPE )
                    params[i] = theNumber.intValue(); 
                else if ( origMyClass == Float.class || origMyClass == Float.TYPE )
                    params[i] = theNumber.floatValue(); 
                else if ( origMyClass == Long.class || origMyClass == Long.TYPE )
                    params[i] = theNumber.longValue(); 
                else if ( origMyClass == Short.class || origMyClass == Short.TYPE )
                    params[i] = theNumber.shortValue(); 
                else
                    throw new RuntimeException( "what is : " + origMyClass );
            }

            if ( myClass == Object.class && params[i].getClass() == JSString.class )
                params[i] = params[i].toString();

        }
        
        return params;
    }
    

    static final int compareParams( Class as[] , Class bs[] ){
        if ( as.length != bs.length )
            return as.length - bs.length;
        
        for ( int i=0; i<as.length; i++ ){
            
            Class a = as[i];
            Class b = bs[i];

            if ( a == String.class && b != String.class )
                return 1;
            if ( b == String.class && a != String.class )
                return -1;
        }

        return 0;
    }

    static final Comparator<Method> _methodLengthComparator = new Comparator<Method>(){
        public int compare( Method a , Method b ){
            return compareParams( a.getParameterTypes() , b.getParameterTypes() );
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };

    static final Comparator<Constructor> _consLengthComparator = new Comparator<Constructor>(){
        public int compare( Constructor a , Constructor  b ){
            return compareParams( a.getParameterTypes() , b.getParameterTypes() );
        }
        public boolean equals(Object obj){
            return this == obj;
        }
    };
}
