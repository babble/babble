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

public class JSPySequenceListWrapper extends JSPyObjectWrapper {

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

        return super.get( n );
    }
    
    public Object removeField( Object n ){
        _p.__delitem__( toPython( n ) );
        return null; // FIXME: we removed both of them, who cares
    }
    
    public Collection<String> keySet( boolean includePrototype ){
        throw new RuntimeException("not implemented");
        /*
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
                if( ! ( dict instanceof PyStringMap ) )
                    throw new RuntimeException( "keySet() of weird __dict__ " + dict.getClass() + "; I give up" );

                for ( Object o : ((PyStringMap)dict).keys() ){
                    keys.add( o.toString() );
                }
            }
        
            return keys;*/
    }
    
    public String toString(){
        return _p.toString();
    }

    private PySequenceList _pSeq; // just to fool the static typing
    
}
    
