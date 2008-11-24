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
import org.python.modules.sre.PatternObject;
import org.python.modules.sre.SRE_STATE;
import org.python.expose.*;
import org.python.expose.generate.*;
import org.python.util.*;

import ed.db.*;
import ed.util.*;
import ed.js.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.appserver.*;
import ed.security.*;
import ed.appserver.adapter.AdapterType;
import ed.appserver.jxp.JxpSource;

import static ed.lang.python.PythonSmallWrappers.*;

public class Python extends Language {

    public Python(){
        super( "python" );
    }
    
    static final boolean D = Boolean.getBoolean( "DEBUG.PY" );

    static {
        Options.includeJavaStackInExceptions = true;
        PySystemState.initialize();

        PyStackTraceFixer _stackFixer = new PyStackTraceFixer();

        StackTraceHolder h = StackTraceHolder.getInstance();
        h.setPackage( "org.python.pycode" , _stackFixer );
        h.setPackage( "org.python.core" , _stackFixer );
        h.setFileType( "py" , _stackFixer );
    }

    public static PyCode compile( File f )
        throws IOException {
        return (PyCode)(Py.compile_flags( new FileInputStream( f ) , f.toString() , "exec" , new CompilerFlags() ));
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
            System.out.println( "toJS " + p + " " + p.getClass());

        if ( p == null || p instanceof PyNone )
            return null;

        if ( p instanceof Boolean ||
             p instanceof Number )
            return p;

        if ( p instanceof String )
            return new JSString( p.toString() );

        if ( p instanceof PyJSStringWrapper )
            p = ((PyJSStringWrapper)p)._p;

        if ( p instanceof PyJSObjectWrapper ){
            if( D )
                System.out.println( "unwrapping " + p + " as " + ((PyJSObjectWrapper)p)._js.getClass());
            return ((PyJSObjectWrapper)p)._js;
        }

        if ( p instanceof PyJSLogLevelWrapper )
            return ((PyJSLogLevelWrapper)p)._level;

        if ( p instanceof PyJSArrayWrapper )
            return  ((PyJSArrayWrapper)p)._js;


        if ( p instanceof PyBoolean )
            return ((PyBoolean)p).getValue() == 1;

        if ( p instanceof PyInteger )
            return ((PyInteger)p).getValue();

        if ( p instanceof PyLong )
            return ((PyLong)p).getValue();

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

        // TODO: this doesn't support several of the Python extensions
        // known things that won't work:
        //   (?iLmsux)
        //   (?P<name>...)
        //   (?P=name)
        //   (?#...)
        //   (?<=...)
        //   (?<!...)
        //   (?(id/name)yes-pattern|no-pattern)
        //   LOCALE, UNICODE and VERBOSE flags
        //   either of MULTILINE or DOTALL will be translated to both MULTILINE and DOTALL
        //   {,n} doesn't work. can be written as {0,n} which will work
        // other stuff could be broken as well...
        if (p instanceof PatternObject) {
            PatternObject re = (PatternObject)p;

            // all Python re's get the JS global match flag
            String flags = "g";
            if ((re.flags & SRE_STATE.SRE_FLAG_IGNORECASE) > 0) {
                flags = flags + "i";
            }
            if ((re.flags & SRE_STATE.SRE_FLAG_MULTILINE) > 0 || (re.flags & SRE_STATE.SRE_FLAG_DOTALL) > 0) {
                flags = flags + "m";
            }

            return new JSRegex(re.pattern.toString(), flags);
        }

        // Insufficiently Pythonic?
        if ( p instanceof PyObjectDerived ){
            PyType type = ((PyObject)p).getType();
            if( type != null && type.fastGetName() != null ){
                String name = type.fastGetName();
                if( name != null && name.equals( "datetime" ) ){
                    Object cal = ((PyObject)p).__tojava__( Calendar.class );
                    if( cal != Py.NoConversion ){
                        return new JSDate( (Calendar)cal );
                    }
                }
            }
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

        if ( o == null )
            return Py.None;

        if ( o instanceof JSPyObjectWrapper )
            return ((JSPyObjectWrapper)o).getContained();

        if ( o instanceof PyObject )
            return (PyObject)o;

        if ( o instanceof Boolean )
            return new PyBoolean( (Boolean)o );

        if ( o instanceof Integer )
            return Py.newInteger( ((Integer)o) );

        if ( o instanceof Long )
            return Py.newLong( ((Long)o) );

        if ( o instanceof Number )
            return new PyFloat( ((Number)o).doubleValue() );

        if ( o instanceof String )
            return new PyString( (String)o );

        if ( o instanceof JSString )
            return new PyJSStringWrapper( (JSString)o );

        if ( o instanceof ObjectId )
            return new PyObjectId( (ObjectId)o );

        if ( o instanceof DBCursor )
            return new PyDBCursor( (DBCursor)o );

        // FILL IN MORE HERE

        if (o instanceof InputStream) {
            return new PyFile((java.io.InputStream)o);
        }

        if (o instanceof JSRegex) {
            PyObject re = __builtin__.__import__("re");
            JSRegex regex = (JSRegex)o;
            ArrayList<PyObject> args = new ArrayList<PyObject>();

            args.add(new PyString(regex.getPattern()));

            if (regex.getFlags().contains("i")) {
                args.add(re.__findattr__("I".intern()));
            }
            if (regex.getFlags().contains("m")) {
                args.add(re.__findattr__("M".intern()));
                args.add(re.__findattr__("S".intern()));
            }

            PyObject compile = re.__findattr__("compile".intern());
            return compile.__call__(args.toArray(new PyObject[1]));
        }

        if ( o instanceof JSArray ){
            return new PyJSArrayWrapper( (JSArray)o );
        }

        if ( o instanceof ed.log.Level ){
            return new PyJSLogLevelWrapper( (ed.log.Level)o );
        }

        if ( o instanceof JSDate ){
            String datetimeS = "datetime".intern();
            PyModule mod = (PyModule)__builtin__.__import__( datetimeS );
            PyObject datetime = mod.__findattr__( datetimeS );
            PyObject fromtimestamp = datetime.__findattr__( "fromtimestamp".intern() );
            return fromtimestamp.__call__( Py.newLong( ((JSDate)o).getTime() / 1000 ) );
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

        return Py.java2py( o );
    }

    /**
     * {@inheritDoc}
     */
    public JxpSource getAdapter(AdapterType type, File f, AppContext context, JSFileLibrary lib) {

        /*
         *  if we're still in init, treat everything as a .py - for example, an import initialized
         *  in an _init.py would be mightily disturbed to be treated like a CGI script
         */
        if (context != null && context.inScopeSetup()) {
            return new ed.lang.python.PythonJxpSource(f, lib);
        }

        switch(type) {
            case CGI :
                return new PythonCGIAdapter(f, lib);
            case DIRECT_10GEN :
                return new PythonJxpSource(f, lib);
            case WSGI :
                return new PythonWSGIAdapter(context, f, lib);
            default :
                throw new RuntimeException("ERROR : unsupported AdapterType : " + type);
        }
    }

    public JSFunction compileLambda( final String source ){
        return extractLambda( source );
    }

    public boolean isComplete( String code ){
        // Be careful! Right now ed.js.Shell doesn't leave \n at the EOL, so we
        // signify the user having typed a blank line by just one \n!
        return Py.compile_command_flags( code, "<input>", "single", Py.getCompilerFlags(), false) != Py.None;
    }

    public Object eval( Scope s , String code , boolean[] hasReturn ){
        if( D )
            System.out.println( "Doing eval on " + code );

        SiteSystemState sss = getSiteSystemState( null , s );
        PySystemState oldPyState = Py.getSystemState();

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

            pycode = (PyCode)Py.compile_flags( expr , filename , "eval" , null);
        }
        else {
            // Otherwise compile the whole module
            pycode = (PyCode)Py.compile_flags( mod , filename , "exec" , null );
        }

        try {
            Py.setSystemState( sss.getPyState() );
            return toJS( __builtin__.eval( pycode , globals ) );
        }
        finally {
            Py.setSystemState( oldPyState );
        }
    }

    public void repl( Scope s ){
        PyObject globals = getGlobals( s );
        InteractiveConsole ic = new InteractiveConsole(globals);
        ic.interact(null, null);
    }

    public static PyObject getGlobals( Scope s ){
        if( s == null ) throw new RuntimeException("can't construct globals for null");
        Scope pyglobals = s.child( "scope to hold python builtins" );

        PyObject globals = new PyJSScopeWrapper( pyglobals , false );

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

        return theFunc != null ? new JSPyObjectWrapper(theFunc.getContained(), true) : null;
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
     * @param ac app context
     * @param s place to store the python state
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
        Object __python__ = s.getAttribute( "__python__" , true );
        if( __python__ != null && __python__ instanceof SiteSystemState ){
            return (SiteSystemState)__python__;
        }

        SiteSystemState state = new SiteSystemState( ac , getGlobals( s ) , s );
        if( D )
            System.out.println("Making a new PySystemState "+ __python__ + " in " + s + " " + __builtin__.id( state.getPyState() ));

        s.setAttribute( "__python__" , state );

        return state;
    }

    public static long size( PyObject o , IdentitySet seen ){
        return _size( o , seen );
    }

    public static long _size( PyObject o , IdentitySet seen ){
        // PyObject: private PyType objtype

        // PyInteger: private int value
        if ( o instanceof PyBoolean || o instanceof PyInteger )
            return JSObjectSize.OBJ_OVERHEAD + 8;

        // PySequence
        // public int gListAllocatedStatus

        // PyString
        // transient int cached_hashcode
        // transient boolean interned
        if ( o instanceof PyString ){
            PyString s = (PyString)o;
            return JSObjectSize.OBJ_OVERHEAD + (long)(s.toString().length() * 2) + 12;
        }

        // PyList
        // PySequenceList: protected PyObjectList list
        // PyObjectList: PyObjectArray array
        // PyObjectArray:
        //    protected int capacity
        //    protected int size
        //    protected int modCountIncr
        // PySequence: public int gListAllocatedStatus
        if( o instanceof PySequenceList ){
            PySequenceList l = (PySequenceList)o;
            int n = l.size();
            long size = 4*JSObjectSize.OBJ_OVERHEAD;
            for( int i = 0; i < n; ++i){
                PyObject foo = l.pyget(i);
                size += 4; // pointer to an object?
                size += JSObjectSize.size( foo, seen );
            }
            return size;
        }

        if( o instanceof PyNone ){
            return JSObjectSize.OBJ_OVERHEAD;
        }

        // PyDictionary: protected final ConcurrentMap table
        if( o instanceof PyDictionary ){
            long temp = 0;
            temp += 32; // sizeof ConcurrentMap?
            PyList list = ((PyDictionary)o).keys();
            for( int i = 0 ; i < list.size(); ++i ){
                temp += JSObjectSize.OBJ_OVERHEAD; // hash table entry
                PyObject key = list.pyget(i);
                temp += JSObjectSize.size( key , seen );
                temp += JSObjectSize.size( ((PyDictionary)o).__finditem__( (PyObject)key ) , seen );
            }
            return temp;
        }

        if( o instanceof PyFunction ){
            // public String __name__
            // public PyObject __doc__
            // public PyObject func_globals
            // public PyObject[] func_defaults
            // public PyObject __dict__
            // public PyObject func_closure
            // public PyObject __module__
            PyFunction f = (PyFunction)o;
            long temp = JSObjectSize.OBJ_OVERHEAD;
            temp += JSObjectSize.size( f.__name__ , seen ) + 4;
            temp += JSObjectSize.size( f.__doc__ , seen ) + 4;
            temp += JSObjectSize.size( f.func_globals , seen ) + 4;
            temp += 4; // pointer to func_defaults
            if( f.func_defaults != null )
                for( PyObject d : f.func_defaults ){
                    temp += JSObjectSize.size( d , seen ) + 4;
                }
            temp += JSObjectSize.size( f.__dict__ , seen ) + 4;
            temp += JSObjectSize.size( f.func_closure , seen ) + 4;
            temp += JSObjectSize.size( f.__module__ , seen ) + 4;
            return temp;
        }

        if( o instanceof PyModule ){
            // public PyObject __dict__
            PyModule m = (PyModule)o;
            long temp = JSObjectSize.OBJ_OVERHEAD;
            temp += JSObjectSize.size( m.__dict__ , seen ) + 4;
            return temp;
        }

        if( o instanceof PyTableCode ){
            PyTableCode t = (PyTableCode)o;
            long temp = JSObjectSize.OBJ_OVERHEAD;
            // PyCode: public String co_name
            temp += JSObjectSize.size( t.co_name , seen ) + 4;
            // PyTableCode:
            // public int co_argcount
            // int nargs
            // public int co_firstlineno
            temp += 12;
            // public String co_varnames[]
            temp += JSObjectSize.size( t.co_varnames , seen ) + 4;

            // public String co_cellvars[]
            temp += JSObjectSize.size( t.co_cellvars , seen ) + 4;

            // public int jy_npurecell
            temp += 4;

            // public String co_freevars[]
            temp += JSObjectSize.size( t.co_freevars , seen ) + 4;

            // public String co_filename
            temp += JSObjectSize.size( t.co_filename , seen ) + 4;

            // public int co_flags
            // public int co_nlocals
            // public boolean varargs
            // public boolean varkwargs
            temp += 10;

            // PyFunctionTable funcs
            //temp += JSObjectSize.size( t.funcs , seen ) + 4;

            // int func_id
            temp += 4;
            // public String co_code
            temp += JSObjectSize.size( t.co_code , seen ) + 4;
            return temp;
        }

        if( o instanceof PyFile ){
            long temp = JSObjectSize.OBJ_OVERHEAD;
            PyFile f = (PyFile)o;
            // public PyObject name
            temp += JSObjectSize.size( f.name , seen ) + 4;
            // public String mode
            temp += JSObjectSize.size( f.mode , seen ) + 4;
            // public boolean softspace
            // private boolean reading
            // private boolean writing
            // private boolean appending
            // private boolean updating
            // private boolean binary
            // private boolean universal
            temp += 7; // seven booleans?
            // private TextIOBase file
            // FIXME: probably have to pass this through again

            // private Closer closer
            temp += JSObjectSize.OBJ_OVERHEAD;
            return temp;
        }

        /*
        if( o instanceof PySystemStateFunctions ){
            return JSObjectSize.OBJ_OVERHEAD;
        }
        */

        if( o instanceof PySystemState ){
            return systemStateSize( (PySystemState)o , seen );
        }

        String blah = o.getClass().toString();
        if ( ! _seenClasses.contains( blah ) ){
            System.out.println("Python bridge couldn't figure out size of " + blah);
            _seenClasses.add( blah );
        }

        return 0;
    }

    public static long systemStateSize( PySystemState p , IdentitySet seen ){
        long temp = JSObjectSize.OBJ_OVERHEAD;

        // public PyList argv
        temp += JSObjectSize.size( p.argv , seen ) + 4;

        // public PyObject modules
        temp += JSObjectSize.size( p.modules , seen ) + 4;

        // public PyList path
        temp += JSObjectSize.size( p.path , seen ) + 4;

        // public PyList meta_path
        temp += JSObjectSize.size( p.meta_path , seen ) + 4;

        // public PyList path_hooks
        temp += JSObjectSize.size( p.path_hooks , seen ) + 4;

        // public PyObject path_importer_cache
        temp += JSObjectSize.size( p.path_importer_cache , seen ) + 4;

        // public PyObject ps1
        temp += JSObjectSize.size( p.ps1 , seen ) + 4;

        // public PyObject ps2
        temp += JSObjectSize.size( p.ps2 , seen ) + 4;

        // public PyObject executable
        temp += JSObjectSize.size( p.executable , seen ) + 4;

        // private String currentWorkingDir
        temp += JSObjectSize.size( p.getCurrentWorkingDir() , seen ) + 4;

        // private PyObject environ
        temp += JSObjectSize.size( p.getEnviron() , seen ) + 4;

        // private ClassLoader classLoader
        temp += JSObjectSize.size( p.getClassLoader() , seen ) + 4;

        // public PyObject stdout, stderr, stdin
        temp += JSObjectSize.size( p.stdout , seen ) + 4;
        temp += JSObjectSize.size( p.stderr , seen ) + 4;
        temp += JSObjectSize.size( p.stdin , seen ) + 4;

        // public PyObject __stdout__, __stderr__, __stdin__
        temp += JSObjectSize.size( p.__stdout__ , seen ) + 4;
        temp += JSObjectSize.size( p.__stderr__ , seen ) + 4;
        temp += JSObjectSize.size( p.__stdin__ , seen ) + 4;

        // public PyObject __displayhook__, __excepthook__
        temp += JSObjectSize.size( p.__displayhook__ , seen ) + 4;
        temp += JSObjectSize.size( p.__excepthook__ , seen ) + 4;

        // public PyObject last_value
        temp += JSObjectSize.size( p.last_value , seen ) + 4;

        // public PyObject last_type
        temp += JSObjectSize.size( p.last_type , seen ) + 4;

        // public PyObject last_traceback
        temp += JSObjectSize.size( p.last_traceback , seen ) + 4;

        // public PyObject __name__
        temp += JSObjectSize.size( p.__name__ , seen ) + 4;
        // public PyObject __dict__
        temp += JSObjectSize.size( p.__dict__ , seen ) + 4;

        // __builtins__?

        return temp;
    }

    /**
     * Exposes a Java type to Jython using the ExposedTypeProcessor.
     *
     * The alternative is to use PyType.fromClass(), which seems to be more
     * for "ordinary" Java classes.
     *
     * @param c class to expose
     * @return jythonic interpretation of the class c
     */
    public static PyType exposeClass( Class c ){
        String fileName = c.getName().replaceAll( "\\.", "/" ) + ".class";
        try {
            ExposedTypeProcessor etp = new ExposedTypeProcessor( c.getClassLoader()
                                                                 .getResourceAsStream( fileName ));
            TypeBuilder t = etp.getTypeExposer().makeBuilder();
            PyType.addBuilder( c, t );
        }
        catch( IOException e ){
            throw new RuntimeException( "Couldn't expose " + c.getName() );
        }
        return PyType.fromClass( c );
    }

    private static Scope _extractGlobals;
    private static Set<String> _seenClasses = Collections.synchronizedSet( new HashSet<String>() );

    public static class PyStackTraceFixer implements StackTraceFixer {
        public StackTraceElement fixSTElement( StackTraceElement element ){
            String cn = element.getClassName();
            String fn = element.getFileName();
            int ln = element.getLineNumber();

            if( cn.startsWith("org.python.pycode._pyx") || cn.endsWith("$py") )
                return new StackTraceElement(fn, "___", fn, ln);
            return element;
        }

        public boolean removeSTElement( StackTraceElement element ){
            return false;
        }
    }

    public static void checkSafeImport( PyObject m ){
        if( ! isSafeImport( m ) ){
            throw new RuntimeException( "can't import Java files from "  + Security.getTopJS() );
        }
    }

    public static boolean isSafeImport( PyObject m ){
        if( m instanceof PyJavaPackage || m instanceof PyJavaClass ){
            PyObject __name__ = m.__findattr__( "__name__" );
            if( ! ( __name__ instanceof PyString ) ){
                throw new RuntimeException("Ethan's code got confused -- how can " + m + " have a name of " + __name__ + "?");
            }

            if( ImportHelper.getBuiltin( __name__.toString() ) != null )
                // Safe -- builtin that Jython recognizes
                return true;

            if( ! Security.inTrustedCode() )
                return false;
        }
        return true;

    }
}
