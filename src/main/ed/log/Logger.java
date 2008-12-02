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
import ed.util.*;

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
        this( parent , name , true );
    }

    public Logger( Logger parent , String name , boolean cache ){
        _parent = parent;
        _name = name;
        _fullName = _parent == null ? name : _parent._fullName + "." + _name;
        _sentEmail = new JSDate(0);

        if ( cache && ! _fullName.contains( "." ) )
            _fullNameToLogger.put( _fullName , this );

        if ( _parent == null )
            _appenders = new ArrayList<Appender>( _defaultAppenders );

	if ( _parent == null )
	    _level = Level.DEBUG;
    }

    public void makeThreadLocal(){
	_tl.set( this );
    }


    // --------------------

    public void debug( int debugLevel , Object o1 , Object o2 ){
	debug( debugLevel , o1 , o2 , null , null , null );
    }

    public void debug( int debugLevel , Object o1 , Object o2 , Object o3 ){
	debug( debugLevel , o1 , o2 , o3 , null , null );
    }

    public void debug( int debugLevel , Object o1 , Object o2 , Object o3 , Object o4 ){
	debug( debugLevel , o1 , o2 , o3 , o4 , null );
    }

    public void debug( int debugLevel , Object o1 , Object o2 , Object o3 , Object o4 , Object o5 ){
	Level l = Level.forDebugId( debugLevel );
	if ( ! shouldLog( l ) )
	    return;
	
	StringBuilder buf = new StringBuilder( o1.toString() );
	if ( o2 != null )
	    buf.append( " [" ).append( o2 ).append( "] " );
	if ( o3 != null )
	    buf.append( " [" ).append( o3 ).append( "] " );
	if ( o4 != null )
	    buf.append( " [" ).append( o4 ).append( "] " );
	if ( o5 != null )
	    buf.append( " [" ).append( o5 ).append( "] " );

	log( l , buf );
    }

    public void debug( int debugLevel , Object msg ){
	log( Level.forDebugId( debugLevel ) , msg );
    }

    public void debug( Object msg ){
        log( Level.DEBUG , msg , null );
    }
    public void debug( Object msg , Throwable t ){
        log( Level.DEBUG , msg , t );
    }

    public void info( Object msg ){
        log( Level.INFO , msg , null );
    }
    public void info( Object msg , Throwable t ){
        log( Level.INFO , msg , t );
    }

    public void warn( Object msg ){
        log( Level.WARN , msg , null );
    }
    public void warn( Object msg , Throwable t ){
        log( Level.WARN , msg , t );
    }

    public void error( Object msg ){
        log( Level.ERROR , msg , null );
    }
    public void error( Object msg , Throwable t ){
        log( Level.ERROR , msg , t );
    }

    public void alert( Object msg ){
        log( Level.ALERT , msg , null );
    }
    public void alert( Object msg , Throwable t ){
        log( Level.ALERT , msg , t );
    }

    public void fatal( Object msg ){
        log( Level.FATAL , msg , null );
    }
    public void fatal( Object msg , Throwable t ){
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
             || s.equals( "alert" )
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
            JSArray a = JSArray.wrap( (ArrayList)_appenders );
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

    public boolean shouldLog( Level l ){
        Level eLevel = getEffectiveLevel();
        return eLevel.compareTo( l ) <= 0;
    }

    public void log( Level level , Object msg ){
        log( level , msg , null );
    }

    public void log( Level level , Object msgObject , Throwable throwable ){
	
	if ( ! shouldLog( level ) )
	    return;

        String msg = msgObject == null ? null : msgObject.toString();

        try {
            StackTraceHolder.getInstance().fix( throwable );
        }
        catch ( Throwable t ){
            System.err.println( "couldn't fix stack trace" );
            t.printStackTrace();
        }

        final Event e = new Event( _fullName , new JSDate() , level , msg , throwable , Thread.currentThread() );
        
        Appender tl = _tlAppender.get();
        if ( tl != null ){
            try {
                tl.append( e );
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
                        l._appenders.get(i).append( e );
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
            
            if ( ! l._inherit )
                break;

            l = l._parent;
        }
    }

    public void addAppender( Appender a ){
        if ( _appenders == null )
            _appenders = new ArrayList<Appender>();
        _appenders.add( a );
    }

    public void setLevel( Level l ){
        _level = l;
    }

    public void setInherit( boolean inherit ){
        _inherit = inherit;
    }

    public boolean inherits(){
        return _inherit;
    }

    public String _getName(){
        return getFullName();
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

    public long approxSize( SeenPath seen ){
        seen.visited( this );

        long size = super.approxSize( seen );
        size += JSObjectSize.size( _appenders , seen , this );
        size += JSObjectSize.size( _levelLoggers , seen , this );

        return size;
    }
    
    final Logger _parent;
    final String _name;
    final String _fullName;

    private boolean _inherit = true;
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
        lst.add( InMemoryAppender.getInstance() );
        _defaultAppenders = Collections.unmodifiableList( lst );
    }
}
