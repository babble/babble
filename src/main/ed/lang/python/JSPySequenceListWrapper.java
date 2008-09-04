// JSPySequenceListWrapper.java

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

/**
 * This class mainly exists to translate JS Array methods/attributes into 
 * Python.
 *
 * Like other wrapper classes, it will probably eventually have to keep a copy
 * of both the Python and JS versions of any contained objects, so that when 
 * they get set in JS and then retrieved in JS, the same thing comes out, but
 * Python also gets a version.
 */
public class JSPySequenceListWrapper extends JSPyObjectWrapper
    implements java.util.List {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.JSPYSEQUENCELISTWRAPPER" );

    /** @unexpose */
    private final static JSFunction _cons = new JSPySequenceListWrapperCons();

    public static class JSPySequenceListWrapperCons extends JSFunctionCalls1 {
        public JSObject newOne(){
            throw new RuntimeException("you shouldn't be able to instantiate a sequence wrapper from JS");
        }

        public Object call( Scope scope , Object a , Object[] extra ){
            throw new RuntimeException("you shouldn't be able to instantiate a sequence wrapper from JS");
        }

        protected void init(){
            _prototype.set( "some",  new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        for ( Object o : a._pSeq ){
                            Object j = toJS( o );

                            if ( JS_evalToBool( f.call( s , j ) ) )
                                return true;
                        }
                        return false;
                    }
                } );

            _prototype.set( "every", new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());
                        JSFunction f = (JSFunction)fo;

                        for ( Object o : a._pSeq ){
                            Object j = toJS( o );

                            if ( ! JS_evalToBool( f.call( s , j ) ) )
                                return false;
                        }

                        return true;
                    }
                } );

            _prototype.set( "push", new   JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());

                        PyObject p = toPython( fo );
                        a._pSeq.invoke( "append" , p );
                        return a._pSeq.__len__();
                    }
                } );

            _prototype.set( "filter", new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        if( ! ( fo instanceof JSFunction ) )
                            throw new RuntimeException( "first argument to filter must be a function" );
                        JSFunction f = (JSFunction)fo;
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());

                        JSArray arr = new JSArray();

                        for ( Object o : a._pSeq ){
                            Object res = f.call( s , toJS( o ) );
                            if( JS_evalToBool( res ) )
                                arr.add( toJS( o ) );
                        }

                        return arr;
                    }
                } );

            _prototype.set( "map" , new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        if( ! ( fo instanceof JSFunction ) )
                            throw new RuntimeException( "first argument to map must be a function" );
                        JSFunction f = (JSFunction)fo;
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());
                        JSArray arr = new JSArray();

                        for ( Object o : a._pSeq ){
                            Object res  = f.call( s , toJS( o ) );
                            arr.add( res );
                        }

                        return arr;
                    }
                } );

            _prototype.set( "reduce", new JSFunctionCalls1() {
                    public Object call( Scope s , Object fo , Object foo[] ){
                        JSPySequenceListWrapper a = (JSPySequenceListWrapper)(s.getThis());
                        JSFunction f = (JSFunction)fo;
                        Object val = null;
                        if ( foo != null && foo.length > 0 )
                            val = foo[0];

                        Integer l = a._pSeq.size();

                        for ( int i = 0 ; i < l ; ++i ){
                            val = f.call( s , val , toJS( a._pSeq.get(i) ) , i , l );
                        }

                        return val;
                    }
                } );
        }
    }

    public JSPySequenceListWrapper( PySequenceList o ){
        super( o );
        _pSeq = o;
        setConstructor( _cons );
    }

    public Object get( Object n ){
        if( n instanceof String || n instanceof JSString ){
            String s = n.toString();
            if( s.equals( "length" ) ) return _p.__len__();
        }

        try {
            int i = JSPySequenceListWrapper.checkInt( n );
            if( i != -1 ){
                return getInt( i );
            }
        }
        catch(Exception e){
            // JS semantics: fail silently
        }
        return super.get( n );
    }

    public Object set( Object k , Object v ){
        try {
            int i = JSPySequenceListWrapper.checkInt( k );
            if( i >= _pSeq.size() ){
                for(int j = _pSeq.size(); j <= i; ++j){
                    _pSeq.add(Py.None);
                }
            }
            if( i != -1 )
                return _pSeq.set( i , toPython( v ) );
        }
        catch(Exception e){
            // shrug?
        }
        return super.set( k , v );
    }

    public static int checkInt( Object o ){
        if ( o == null )
            return -1;

        if ( o instanceof Number )
            return ((Number)o).intValue();

        if ( o instanceof JSString )
            o = o.toString();

        if ( ! ( o instanceof String ) )
            return -1;

        String str = o.toString();
	if ( str.length() == 0 )
	    return -1;
	
        for ( int i=0; i<str.length(); i++ )
            if ( ! Character.isDigit( str.charAt( i ) ) )
                return -1;

        try {
            return Integer.parseInt( str );
        }
        catch(Exception e){
            return -1;
        }
    }
    
    public Object getInt( int i ){
        Object o = super.getInt( i );
        if( o == null ){ // FIXME: containsKey()
            o = toJS( _pSeq.get( i ) );
        }

        return o;
    }
    
    public Object removeField( Object n ){
        _p.__delitem__( toPython( n ) );
        return null; // FIXME: we removed both of them, who cares
    }
    
    public Collection<String> keySet( boolean includePrototype ){
        List<String> keys = new ArrayList<String>();

        int n = _pSeq.__len__();

        for( int i = 0 ; i < n ; ++i ){
            keys.add( Integer.toString( i ) );
        }

        return keys;
    }
    
    public String toString(){
        return _p.toString();
    }

    private PySequenceList _pSeq; // just to fool the static typing
    

    // java.util.List API
    public boolean contains( Object o ){
        return _pSeq.contains( toPython( o ) );
    }

    public boolean containsAll( Collection c ){
        // FIXME
        return true;
    }

    public boolean equals( Object o ){
        // FIXME?
        return _pSeq.equals( o );
    }

    /*    public int hashCode(){
          return _pSeq.hashCode();
          }*/

    public Iterator iterator(){
        // FIXME
        return null;
    }

    public boolean remove( Object o ){
        return _pSeq.remove( toPython( o ) );
    }

    public boolean removeAll( Collection c ){
        for( Object o : c ){
            _pSeq.remove( toPython( o ) );
        }
        return false;
    }

    public Object set( int index , Object element ){
        _pSeq.__setitem__( index, toPython( element ) );
        return element;
    }

    public int size(){
        return _pSeq.size();
    }

    public Object[] toArray(){
        // FIXME
        return null;
    }

    public Object[] toArray( Object[] a ){
        return null;
    }

    public void add( int index, Object element ){
        _pSeq.add( index, toPython( element ) );
    }

    public boolean add( Object o ){
        return _pSeq.add( toPython( o ) );
    }

    public boolean addAll( Collection c ){
        // FIXME
        return false;
    }

    public boolean addAll( int index , Collection c ){
        // FIXME
        return false;
    }

    public void clear(){
        // FIXME
    }

    public Object get( int index ){
        return getInt( index );
    }

    public int indexOf( Object o ){
        return _pSeq.indexOf( toPython( o ) );
    }

    public boolean isEmpty(){
        return _pSeq.isEmpty();
    }

    public int lastIndexOf( Object o ){
        return _pSeq.lastIndexOf( toPython( o ) );
    }

    public ListIterator listIterator(){
        // FIXME
        return null;
    }

    public ListIterator listIterator( int index ){
        // FIXME
        return null;
    }

    public Object remove( int index ){
        return toJS( _pSeq.remove( index ) );
    }

    public boolean retainAll( Collection c ){
        // FIXME
        return true;
    }

    public List subList( int fromIndex , int toIndex ){
        // FIXME
        return null;
    }
}
    
