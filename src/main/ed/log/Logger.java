// Logger.java

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

package ed.log;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.lang.*;

public class Logger extends JSFunctionCalls2 {

    public static Logger getLogger( Class c ){
        return getLogger( c.getName() );
    }

    public static Logger getLogger( String fullName ){
        Logger l = _fullNameToLogger.get( fullName );
        if ( l != null )
            return l;


        String base = fullName;

        int idx = fullName.indexOf( "." );
        if ( idx > 0 )
            base = fullName.substring( 0 , idx );

        l = _fullNameToLogger.get( base );
        if ( l == null )
            l = new Logger( null , base );

        String pcs[] = fullName.split( "\\." );
        for ( int i=1; i<pcs.length; i++ )
            l = (Logger)l.get( pcs[i] );

        _fullNameToLogger.put( fullName , l );
        return l;
    }

    public static Logger getRoot(){
	final Logger l = getThreadLocalRoot();
	return l == null ? getLogger( "noroot" ) : l;
    }

    public static Logger getThreadLocalRoot(){
	return _tl.get();
    }

    // -----
    
    public static void setThreadLocalAppender( Appender a ){
        _tlAppender.set( a );
    }
    
    private static final ThreadLocal<Appender> _tlAppender = new ThreadLocal<Appender>();

    // -------------------

    public Logger( Logger parent , String name ){
        _parent = parent;
        _name = name;
        _fullName = _parent == null ? name : _parent._fullName + "." + _name;
        _sentEmail = new JSDate(0);

        if ( ! _fullName.contains( "." ) )
            _fullNameToLogger.put( _fullName , this );

        if ( _parent == null )
            _appenders = new ArrayList<Appender>( _defaultAppenders );
    }

    public void makeThreadLocal(){
	_tl.set( this );
    }


    // --------------------

    public void debug( String msg ){
        log( Level.DEBUG , msg , null );
    }
    public void debug( String msg , Throwable t ){
        log( Level.DEBUG , msg , t );
    }

    public void info( String msg ){
        log( Level.INFO , msg , null );
    }
    public void info( String msg , Throwable t ){
        log( Level.INFO , msg , t );
    }

    public void warn( String msg ){
        log( Level.WARN , msg , null );
    }
    public void warn( String msg , Throwable t ){
        log( Level.WARN , msg , t );
    }

    public void error( String msg ){
        log( Level.ERROR , msg , null );
    }
    public void error( String msg , Throwable t ){
        log( Level.ERROR , msg , t );
    }

    public void fatal( String msg ){
        log( Level.FATAL , msg , null );
    }
    public void fatal( String msg , Throwable t ){
        log( Level.FATAL , msg , t );
    }

    // --------------------

    public Object call( Scope s , Object oName , Object oT , Object foo[] ){
        log( Level.INFO , (oName != null) ? oName.toString() : "null" , _makeThrowable( oT ) );
        return null;
    }

    public Logger getChild( String s ){
        return (Logger)get( s );
    }

    public Object get( Object n ){
        String s = n.toString();

        if ( s.equals( "log" ) )
            return null;

        if ( s.equals( "debug" )
             || s.equals( "info" )
             || s.equals( "error" )
             || s.equals( "fatal" ) ){
            return _getLevelLogger( s );
        }
        //return Level.forName( s ).func;

        if ( s.equals( "LEVEL" ) )
            return Level.me;

        if ( s.equals( "level" ) )
            return _level;

        Object foo = super.get( n );
        if ( foo != null )
            return foo;

        if ( s.equals( "appenders" ) ){
            if ( _appenders == null )
                _appenders = new ArrayList<Appender>();
            JSArray a = new JSArray( _appenders );
            super.set( "appenders" , a );
            return a;
        }

        Logger child = new Logger( this , s );
        set( s , child );
        return child;
    }

    public Object set( Object n , Object v ){
        String s = n.toString();

        if ( s.equals( "level" ) ){
            _level = (Level)v;
            return _level;
        }

        if ( s.equals( "appenders" ) )
            throw new RuntimeException( "can't change" );

        return super.set( n , v );
    }

    JSFunction _getLevelLogger( String levelName ){
        final Level level = Level.forName( levelName );
        if ( level == null )
            throw new RuntimeException( "don't know about level [" + levelName + "]" );
        JSFunction f = _levelLoggers.get( level );
        if ( f == null ){
            f = new JSFunctionCalls2(){
                    public Object call( Scope s , Object msgObject , Object excObject , Object extra[] ){

                        if ( msgObject == null )
                            msgObject = "null";
                        
                        log( level , msgObject.toString() , _makeThrowable( excObject ) );
                        return true;
                    }

                    public Object get( Object n ){
                        if ( n.toString().equals( "__puts__" ) ||
                             n.toString().equals( "puts" ) ||
                             n.toString().equals( "print" ) )
                            return this;
                        return super.get( n );
                    }
                };
            _levelLoggers.put( level , f );
        }
        return f;
    }
    

    // --------------------

    public Level getEffectiveLevel(){
        if ( _level != null )
            return _level;

        Logger t = this._parent;
        while ( t != null ){
            if ( t._level != null )
                return t._level;
            t = t._parent;
        }

        return Level.DEBUG;
    }

    public void log( Level level , String msg ){
        log( level , msg , null );
    }

    public void log( Level level , String msg , Throwable throwable ){
        Level eLevel = getEffectiveLevel();
        if ( eLevel.compareTo( level ) > 0 )
            return;

        JSDate date = new JSDate();
        Thread thread = Thread.currentThread();
        
        try {
            StackTraceHolder.getInstance().fix( throwable );
        }
        catch ( Throwable t ){
            System.err.println( "couldn't fix stack trace" );
            t.printStackTrace();
        }
        
        Appender tl = _tlAppender.get();
        if ( tl != null ){
            try {
                tl.append( _fullName , date , level , msg , throwable , thread );
            }
            catch ( Throwable t ){
                System.err.println( "error running tl appender" );
                t.printStackTrace();
            }
        }
            

        Logger l = this;
        while ( l != null ){

            if ( l._appenders != null ){
                for ( int i=0; i < l._appenders.size(); i++ ){
                    try {
                        l._appenders.get(i).append( _fullName , date , level , msg , throwable , thread );
                    }
                    catch ( Throwable t ){
                        System.err.println( "error running appender" );
                        try {
                            StackTraceHolder.getInstance().fix( t );
                        }
                        catch ( Throwable tt ){
                            tt.printStackTrace();
                        }
                        
                        t.printStackTrace(); // Logger
                    }
                }
            }
            
            l = l._parent;
        }
    }

    public void setLevel( Level l ){
        _level = l;
    }

    public String getFullName(){
        return _fullName;
    }

    public String toString(){
        return "Logger:" + _fullName;
    }

    Throwable _makeThrowable( Object o ){
        if ( o == null )
            return null;

        if ( o instanceof Throwable )
            return (Throwable)o;

        JSException e = new JSException( o );
        e.fillInStackTrace();
        return e;
    }

    final Logger _parent;
    final String _name;
    final String _fullName;
    private JSDate _sentEmail;
    List<Appender> _appenders;
    Level _level = null;
    final Map<Level,JSFunction> _levelLoggers = Collections.synchronizedMap( new TreeMap<Level,JSFunction>() );

    static final ThreadLocal<Logger> _tl = new ThreadLocal<Logger>();

    private final static Map<String,Logger> _fullNameToLogger = Collections.synchronizedMap( new HashMap<String,Logger>() );
    private final static List<Appender> _defaultAppenders;
    static {
        List<Appender> lst = new ArrayList<Appender>();
        lst.add( new ConsoleAppender() );
        _defaultAppenders = Collections.unmodifiableList( lst );
    }
}
