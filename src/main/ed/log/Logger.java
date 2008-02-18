// Logger.java

package ed.log;

import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class Logger extends JSFunctionCalls2 {

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

    // -------------------

    public Logger( Logger parent , String name ){
        _parent = parent;
        _name = name;
        _fullName = _parent == null ? name : _parent._fullName + "." + _name;

        if ( ! _fullName.contains( "." ) )
            _fullNameToLogger.put( _fullName , this );

        if ( _parent == null )
            _appenders = new ArrayList<Appender>( _defaultAppenders );
        
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
        log( Level.INFO , oName.toString() , (Throwable)oT );
        return null;
    }

    public Object get( Object n ){
        String s = n.toString();
        
        if ( s.equals( "log" )
             || s.equals( "debug" ) 
             || s.equals( "info" )
             || s.equals( "error" )
             || s.equals( "fatal" ) )
            return null;
        
        Object foo = super.get( n );
        if ( foo != null )
            return foo;
        
        if ( s.equals( "appenders" ) ){
            if ( _appenders == null )
                _appenders = new ArrayList<Appender>();
            JSArray a = new JSArray( _appenders );
            set( "appenders" , a );
            return a;
        }

        Logger child = new Logger( this , s );
        set( s , child );
        return child;
    }

    // --------------------

    public void log( Level level , String msg , Throwable throwable ){
        JSDate date = new JSDate();
        Thread thread = Thread.currentThread();
        
        Logger l = this;
        while ( l != null ){

            if ( l._appenders != null )
                for ( int i=0; i < l._appenders.size(); i++ )
                    l._appenders.get(i).append( _fullName , date , level , msg , throwable , thread );
            
            l = l._parent;
        }
    }

    public String getFullName(){
        return _fullName;
    }

    public String toString(){
        return "Logger:" + _fullName;
    }

    final Logger _parent;
    final String _name;
    final String _fullName;
    List<Appender> _appenders;

    private final static Map<String,Logger> _fullNameToLogger = Collections.synchronizedMap( new HashMap<String,Logger>() );
    private final static List<Appender> _defaultAppenders;
    static {
        List<Appender> lst = new ArrayList<Appender>();
        lst.add( new ConsoleAppender() );
        _defaultAppenders = Collections.unmodifiableList( lst );
    }
}
