// Python.java

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

import java.io.*;
import java.util.*;

import org.python.core.*;
import org.python.antlr.*;
import org.python.antlr.ast.*;

import ed.db.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.lang.*;
import ed.appserver.*;

import static ed.lang.python.PythonSmallWrappers.*;

public class Python extends Language {

    public Python(){
        super( "python" );
    }

    static final boolean D = Boolean.getBoolean( "DEBUG.PY" );

    static {
        Options.includeJavaStackInExceptions = true;
        PySystemState.initialize();
    }
    
    public static PyCode compile( File f )
        throws IOException {
        return (PyCode)(Py.compile( new FileInputStream( f ) , f.toString() , "exec" ));
    }

    public static void deleteCachedJythonFiles( File dir ){
        for( File child : dir.listFiles() ){
            if( child.getName().endsWith( "$py.class") ){
                child.delete();
            }
            if( child.isDirectory() ){
                deleteCachedJythonFiles( child );
            }
        }
    }


    public static Object toJS( Object p ){
        if( D )
            System.out.println( "toJS " + p );

        if ( p == null || p instanceof PyNone )
            return null;
 
        if ( p instanceof JSObject ||
             p instanceof JSString || 
             p instanceof Number )
            return p;

        if ( p instanceof String )
            return new JSString( p.toString() );

        if ( p instanceof PyJSStringWrapper )
            p = ((PyJSStringWrapper)p)._p;

        if ( p instanceof PyJSObjectWrapper ){
            if( D )
                System.out.println( "unwrapping " + p );
            return ((PyJSObjectWrapper)p)._js;
        }

        if ( p instanceof PyBoolean )
            return ((PyBoolean)p).getValue() == 1;

        if ( p instanceof PyInteger )
            return ((PyInteger)p).getValue();
        
        if ( p instanceof PyFloat )
            return ((PyFloat)p).getValue();
        
        if ( p instanceof PyString )
            return new JSString( p.toString() );

        if ( p instanceof PyObjectId )
            return ((PyObjectId)p)._id;

        if ( p instanceof PyClass || p instanceof PyType ){
            return new JSPyClassWrapper( (PyObject)p );
        }

        if ( p instanceof PySequenceList ){
            return new JSPySequenceListWrapper((PySequenceList)p);
        }

        // this needs to be last
        if ( p instanceof PyObject )
            return new JSPyObjectWrapper( (PyObject)p );

        throw new RuntimeException( "can't convert [" + p.getClass().getName() + "] from py to js" );       
    }
    
    public static PyObject toPython( Object o ){
        return toPython( o , null );
    }

    public static PyObject toPython( Object o , Object useThis ){
        
        if ( o == null )
            return Py.None;

        if ( o instanceof DBRef )
            o = ((DBRef)o).doLoad();

        if ( o instanceof JSPyObjectWrapper )
            return ((JSPyObjectWrapper)o).getContained();

        if ( o instanceof PyObject )
            return (PyObject)o;
        
        if ( o instanceof Boolean )
            return new PyBoolean( (Boolean)o );

        if ( o instanceof Integer )
            return new PyInteger( ((Integer)o).intValue() );
        
        if ( o instanceof Number )
            return new PyFloat( ((Number)o).doubleValue() );
        
        if ( o instanceof String )
            return new PyString( (String)o );

        if ( o instanceof JSString )
            return new PyJSStringWrapper( (JSString)o );
        
        if ( o instanceof ed.db.ObjectId )
            return new PyObjectId( (ed.db.ObjectId)o );

        // FILL IN MORE HERE

        if ( o instanceof JSArray ){
            PyList l = new PyList();
            for( Object c : ((JSArray)o) ){
                l.append( toPython( c ) );
            }
            return l;
        }

        // these should be at the bottom
        if ( o instanceof JSFunction ){
            Object p = ((JSFunction)o).getPrototype();
            if( p instanceof JSObject ){
                JSObject jsp = (JSObject)p;
                if( ! jsp.keySet().isEmpty() )
                    return new PyJSClassWrapper( (JSFunction)o );
            }

            return new PyJSFunctionWrapper( (JSFunction)o , useThis );
        }
        

        if ( o instanceof JSObject )
            return new PyJSObjectWrapper( (JSObject)o );
        
        throw new RuntimeException( "can't convert [" + o.getClass().getName() + "] from js to py" );
    }

    public JSFunction compileLambda( final String source ){
        return extractLambda( source );
    }

    public Object eval( Scope s , String code , boolean[] hasReturn ){
        if( D )
            System.out.println( "Doing eval on " + code );
        PyObject globals = getGlobals( s );
        code = code+ "\n";
        PyCode pycode;
        String filename = "<input>";

        // Hideous antlr code to figure out if this is a module or an expression
        ModuleParser m = new ModuleParser( new org.antlr.runtime.ANTLRStringStream( code ) , filename , false );
        modType tree = m.file_input();
        if( ! ( tree instanceof org.python.antlr.ast.Module ) ){
            // no idea what this would mean -- tell Ethan
            throw new RuntimeException( "can't happen -- blame Ethan" );
        }

        // Module is the class meaning "toplevel sequence of statements"
        org.python.antlr.ast.Module mod = (org.python.antlr.ast.Module)tree;

        // If there's only one statement and it's an expression statement,
        // compile just that expression as its own module.
        hasReturn[0] = mod.body != null && mod.body.length == 1 && (mod.body[0] instanceof Expr );
        if( hasReturn[0] ){
            // I guess this class is treated specially, has a return value, etc.
            Expression expr = new Expression( new PythonTree() , ((Expr)mod.body[0]).value );

            pycode = (PyCode)Py.compile( expr , filename );
        }
        else {
            // Otherwise compile the whole module
            pycode = (PyCode)Py.compile( mod , filename );
        }

        return toJS( __builtin__.eval( pycode , globals ) );
    }

    public static PyObject getGlobals( Scope s ){
        if( s == null ) throw new RuntimeException("can't construct globals for null");
        Scope pyglobals = s.child( "scope to hold python builtins" );

        PyObject globals = new PyJSScopeWrapper( pyglobals , false );
        Scope tl = pyglobals.getTLPreferred();

        pyglobals.setGlobal( true );
        __builtin__.fillWithBuiltins( globals );
        globals.invoke( "update", PySystemState.builtins );
        pyglobals.setGlobal( false );
        return globals;
    }

    public static JSFunction extractLambda( final String source ){
        
        final PyCode code = (PyCode)(Py.compile( new ByteArrayInputStream( source.getBytes() ) , "anon" , "exec" ) );

        if ( _extractGlobals == null )
            _extractGlobals = Scope.newGlobal();

        Scope s = _extractGlobals.child();
        s.setGlobal( true );
        PyObject globals = new PyJSScopeWrapper( s , false );        

        PyModule module = new PyModule( "__main__" , globals );
        PyObject locals = module.__dict__;
        
        Set<String> before = new HashSet<String>( s.keySet() );
        Py.runCode( code, locals, globals );
        Set<String> added = new HashSet<String>( s.keySet() );
        added.removeAll( before );
        
        JSPyObjectWrapper theFunc = null;
        
        for ( String n : added ){
            if ( s.get( n ) == null )
                continue;
            Object foo = s.get( n );
            if ( ! ( foo instanceof JSPyObjectWrapper ) )
                continue;
            
            JSPyObjectWrapper p = (JSPyObjectWrapper)foo;
            if ( ! p.isCallable() )
                continue;
            
            if ( p.getPyCode() == null )
                continue;
            
            theFunc = p;
        }
        
        return new JSPyObjectWrapper( (PyFunction)(theFunc.getContained()) , true );
    }

    /**
     * Get a sensible site-specific state for either the given app
     * context or the given scope.
     *
     * Given a Scope, get the Python site-specific state for that scope.
     * If one does not exist, create one with the given AppContext and Scope.
     * If the Scope is null, it will be obtained from the AppContext.
     *
     * The Scope will store the Python state, so if possible make it an
     * AppContext (or suitably long-lived) scope.
     *
     * @return an already-existing SiteSystemState for the given site
     *   or a new one if needed
     */
    public static SiteSystemState getSiteSystemState( AppContext ac , Scope s ){
        if( ac == null && s == null ){
            throw new RuntimeException( "can't get site-specific state for null site with no context" );
        }

        if( s == null ){ // but ac != null, or we'd throw above
            s = ac.getScope();
        }
        Object __python__ = s.get( "__python__" );
        if( __python__ != null && __python__ instanceof SiteSystemState ){
            return (SiteSystemState)__python__;
        }

        SiteSystemState state = new SiteSystemState( ac , getGlobals( s ) , s );
        if( D )
            System.out.println("Making a new PySystemState "+ __python__ + " in " + s + " " + __builtin__.id( state.getPyState() ));

        s.putExplicit( "__python__" , state );

        if( _rmap == null ){
            _rmap = new HashMap<PySystemState, SiteSystemState>();
        }
        _rmap.put( state.getPyState(), state );

        return state;
    }

    /**
     * Get the already-existing SiteSystemState that wraps the given
     * PySystemState.
     *
     * This assumes you've already passed through the other
     * getSiteSystemState code path at some point and are returning a
     * PySystemState wrapped by a SiteSystemState.
     */
    public static SiteSystemState getSiteSystemState( PySystemState py ){
        return _rmap.get( py );
    }

    private static Scope _extractGlobals;
    private static Map<PySystemState, SiteSystemState> _rmap;
}
