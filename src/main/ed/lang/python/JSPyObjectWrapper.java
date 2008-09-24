// JSPyObjectWrapper.java

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

package ed.lang.python;

import java.util.*;

import org.python.core.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import static ed.lang.python.Python.*;

public class JSPyObjectWrapper extends JSFunctionCalls0 {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.JSPYOBJECTWRAPPER" );


    static JSFunction _apply = new ed.js.func.JSFunctionCalls3(){
            public Object call( Scope s , Object obj , Object args , Object explodeArgs , Object [] foo ){
                JSPyObjectWrapper func = (JSPyObjectWrapper)s.getThis();

                if ( args == null )
                    args = new JSArray();

                if( ! (args instanceof JSArray) )
                    throw new RuntimeException("second argument to JSPyObjectWrapper.prototype.apply must be an array, not a " + args.getClass() );

                JSArray jary = (JSArray)args;

                if ( ! (explodeArgs instanceof JSObject ) ){
                    throw new RuntimeException("third argument to JSPyObjectWrapper.prototype.apply must be an Object, not a " + args.getClass() );
                }

                JSObject jo = (JSObject)explodeArgs;

                s.setThis( obj );
                try {
                    // FIXME: actually passing this?
                    boolean passThis = obj != null;
                    return toJS( func.callPython( s , jary.toArray() , jo , passThis ) );

                }
                finally {
                    s.clearThisNormal( null );
                }
            }
        };


    public static class JSPyObjectWrapperCons extends JSFunctionCalls1 {
        public JSObject _throwException(){
            throw new RuntimeException("you shouldn't be able to instantiate an object wrapper from JS");
        }

        public JSObject newOne(){
            return _throwException();
        }

        public Object call( Scope scope , Object a , Object[] extra ){
            return _throwException();
        }

        protected void init(){
            JSFunction._init( this );
            _prototype.set( "apply" , _apply );
        }
    }

    private final static JSFunction _cons = new JSPyObjectWrapperCons();

    private JSPyObjectWrapper( ){
        setConstructor( _cons );
        _p = null;
        _passThis = false;
    }

    public JSPyObjectWrapper( PyObject o ){
        this( o , false );
    }

    public JSPyObjectWrapper( PyObject o , boolean passThis ){
        setConstructor( _cons );
        _p = o;
        if ( _p == null )
            throw new NullPointerException( "don't think you should create a JSPyObjectWrapper for null" );
        _passThis = passThis;
    }

    public static JSPyObjectWrapper newShell( ){
        return new JSPyObjectWrapper();
    }

    public void setContained( PyObject p ){
        // Be careful with this
        _p = p;
    }

    public PyObject getContained( ){
        return _p;
    }
    
    public boolean isCallable(){
        // All python objects are *potentially* callable, so we subclass
        // JSFunction.
        // This function checks whether the wrapped object is callable.
        return __builtin__.callable( _p );
    }

    public Object set( Object n , Object v ){
        if( _p == null && ( n.equals( "prototype" ) || n.equals( "length" ) ) ){
            if( DEBUG )
                System.err.println("I'm not set up yet! Ignoring set to " + n);
            return v;
        }
        try {
            _p.__setitem__( toPython( n ) , toPython( v ) );
        }
        catch(PyException e){
            // meh -- try setattr
            String s = n.toString();
            __builtin__.setattr( _p, s.intern() , toPython( v ) );
        }
        return v;
    }

    public Object get( Object n ){
        PyObject p = toPython( n );

        Object o = null;
        try {
            o = _p.__finditem__( p );
        } catch(PyException e){
            if ( D ) System.out.println("JSPyObjectWrapper.get FIXME: " + e.type);
        }

        if ( o == null )
            o = _p.__findattr__( n.toString() );

        if ( o == null )
            return super.get( n );

        return toJS( o );
    }
    
    public Object setInt( int n , Object v ){
        _p.__setitem__( toPython( n ) , toPython( v ) );
        return v;
    }

    public Object getInt( int n ){
        return toJS( _p.__getitem__( toPython ( n ) ) );
    }
    
    public Object removeField( Object n ){
        super.removeField( n );
        _p.__delattr__( n.toString() );
        return null; // FIXME: we removed both of them, who cares
    }
    
    public boolean containsKey( String s ){
        // TODO: make less awful
        return keySet().contains( s );
    }
    
    public Object call( Scope s , Object [] params ){
        return toJS( callPython( s , params , null , _passThis ) );
    }

    public PyObject callPython( Object [] params, JSObject kwargs ){
        return callPython( null , params , kwargs , false );
    }

    public PyObject callPython( Scope s , Object [] params , JSObject kwargs , boolean passThis ){
        Collection<String> keys = null;
        String [] pykeys = null;

        JSArray args = argumentNames();
        if ( args == null || args.size() == 0 )
            passThis = false;

        int length = 0;
        if( params != null ){
            length += params.length;
        }
        if( passThis ) length++;

        int offset = 0;

        if( kwargs != null ){
            keys = kwargs.keySet();
            length += keys.size();
            pykeys = new String[ keys.size() ];
        }

        PyObject [] pParams = new PyObject[ length ];
        int i = 0;
        if( passThis ){
            pParams[offset++] = toPython( s.getThis() );
        }
        if( params != null ){
            for( ; i < params.length; ++i ){
                pParams[ offset + i ] = toPython( params[ i ] );
            }
        }

        int j = 0;
        if( kwargs != null ){
            for( String key : keys ){
                pykeys[ j ] = key;
                pParams[ i + j ] = toPython( kwargs.get( key ) );
                ++j;
            }
        }
        else{
            pykeys = new String[0];
        }
        
        return _p.__call__( pParams , pykeys );
    }

    public Collection<String> keySet( boolean includePrototype ){
        List<String> keys = new ArrayList<String>();
    
        if ( _p instanceof PyDictionary ){
            for ( Object o : ((PyDictionary)_p).keySet() )
                keys.add( o.toString() );
        }
        else try {
                for ( PyObject o : _p.asIterable() ){
                    keys.add( o.toString() );
                }
                return keys;
            }
            catch( PyException e ){
                PyObject dict = _p.getDict();
                
                if( dict == null ){

                    if( _p instanceof PyInstance ){
                        dict = ((PyInstance)_p).__dict__;
                    }

                    if( dict == null ){
                        if(D) System.out.println("can't figure out how to get keyset for " + _p.getClass());
                        return keys;
                    }

                }

                if( dict instanceof PyStringMap ){
                    for ( Object o : ((PyStringMap)dict).keys() ){
                        keys.add( o.toString() );
                    }
                }
                else {
                    PyObject pykeys;
                    pykeys = dict.invoke( "keys" );
                    if( pykeys instanceof PySequenceList ){
                        for( Object o : ((PySequenceList)pykeys) )
                            keys.add( o.toString() );
                    }
                    else
                        throw new RuntimeException("can't figure out how to iterate " + dict.getClass() );
                }
            }
        
        return keys;
    }

    public JSFunction getConstructor(){
        Object o = toJS( _p.getType() );
        if( ! ( o instanceof JSFunction) )
            throw new RuntimeException("I'm so confused");
        return (JSFunction)o;
    }

    public JSObject getSuper(){
        // FIXME: This is probably wrong; since we treat all Python objects as
        // functions, why shouldn't we return JSFunction._prototype?
        return JSObjectBase._objectLowFunctions;
    }
    
    public String toString(){
        return _p.invoke( "__str__" ).toString();
    }
    
    public PyCode getPyCode(){
        if ( _p instanceof PyFunction )
            return ((PyFunction)_p).func_code;
        return null;
    }

    public JSArray argumentNames(){
        PyCode c = getPyCode();
        if ( c == null )
            return null;

        if ( ! ( c instanceof PyTableCode ) )
            return null;

        PyTableCode tc = (PyTableCode)c;
        
        JSArray foo = new JSArray();
        for ( String s : tc.co_varnames )
            foo.add( s );
        return foo;
    }

    public String getSourceCode(){
        if ( ! isCallable() )
            throw new RuntimeException( "can't call getSourceCode on something that isn't callable" );
        
        if ( _p instanceof PyFunction ){
            PyFunction func = (PyFunction)_p;
            if ( func.func_code instanceof PyTableCode )
                return getSourceCode( (PyTableCode)func.func_code );
            
        }
        return null;
    }
    
    public static String getSourceCode( PyTableCode code ){
        if ( code == null )
            return null;
        
        PyFile pyFile;
        try {
            pyFile = new PyFile( code.co_filename, "U", -1);
        } 
        catch (PyException pye) {
            return null;
        }
        
        StringBuilder buf = new StringBuilder();
        int curLine = 0;
        try {
            while ( true ){
                String line = pyFile.readline();
                curLine++;
                if ( curLine < code.co_firstlineno )
                    continue;
                if ( line == null || line.equals("\n") || line.equals( "\r\n" ) )
                    break;
                buf.append( line );
            }
        } 
        catch (PyException pye){}
        
        try {
            pyFile.close();
        }
        catch (PyException pye){}
        
        return buf.toString();
        
    }

    protected PyObject _p;
    final boolean _passThis;
}
    
