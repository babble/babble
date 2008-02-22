// JSData.java

package ed.js;

import java.util.*;
import java.text.*;

import ed.js.func.*;
import ed.js.engine.*;

public class JSDate extends JSObjectBase implements Comparable {

    public static JSFunction _cons = 
        new JSFunctionCalls1(){
            
            public JSObject newOne(){
                return new JSDate();
            }
            
            public Object call( Scope s , Object foo , Object[] args ){
                
                Object o = s.getThis();
                if ( o == null || ! ( o instanceof JSDate ) )
                    return new JSString( (new JSDate( foo )).toString() );

                JSDate d = (JSDate)o;
                long l = parse( foo , d._time );
                d._time = l;
                
                return d;
            }
            
            
            protected void init(){
                
                _prototype.set( "getTime" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            return ((JSDate)s.getThis())._time;
                        }
                    } );
            }
            
        };
    
    static long parse( Object o ){
        return parse( o , System.currentTimeMillis() );
    }
    
    static long parse( Object o , long def ){

        if ( o == null )
            return def;
	
        if ( o instanceof Date )
	    return ((Date)o).getTime();
        
        if ( o instanceof String || o instanceof JSString )
            return parseDate( o.toString() , def );
        
        if ( ! ( o instanceof Number ) )
            return def;
        return ((Number)o).longValue();
    }

    public static long parseDate( String s , long def ){
        if ( s == null )
            return def;
        s = s.trim();
        if ( s.length() == 0 )
            return def;
        
        for ( int i=0; i<DATE_FORMATS.length; i++ ){
            try {
                synchronized ( DATE_FORMATS[i] ) {
                    return DATE_FORMATS[i].parse( s ).getTime();
                }
            }
            catch ( java.text.ParseException e ){
            }
        }
        
        return def;
    }
    

    public JSDate(){
        this( System.currentTimeMillis() );
    }

    public JSDate( long t ){
        super( _cons );
        _time = t;
    }

    public JSDate( Calendar c ){
        this( c.getTimeInMillis() );
    }

    public JSDate( Object foo ){
        this( parse( foo ) );
    }

    public long getTime(){
        return _time;
    }

    public int getYear(){
        _cal();
        int y = _c.get( Calendar.YEAR );
        if ( y >= 0 && y < 200 )
            return 1900 + y;
        return y;
    }

    public int getMonth(){
        _cal();
        return 1 + _c.get( Calendar.MONTH );
    }

    public int getDay(){
        _cal();
        return _c.get( Calendar.DAY_OF_MONTH );
    }

    public String toString(){
        return new Date( _time ).toString();
    }

    public String format( String theFormat ){
        SimpleDateFormat df = new SimpleDateFormat( theFormat );
        return df.format( new Date( _time ) );
    }

    public String webFormat(){
        return format( _webFormat );
    }

    public String simpleFormat(){
        return format( _simpleFormat );
    }

    public String format( DateFormat df ){
        synchronized ( df ){ 
            return df.format( new Date( _time ) );
        }
    }

    public JSDate roundMonth(){
        return new JSDate( _roundMonth() );
    }
    
    public JSDate roundWeek(){
	return new JSDate( _roundWeek() );
    }

    public JSDate roundDay(){
        return new JSDate( _roundDay() );
    }
    
    public JSDate roundHour(){
        return new JSDate( _roundHour() );
    }

    public Calendar _roundMonth(){
        Calendar c = _roundDay();
        c.set( c.DAY_OF_MONTH , 1 );
        return c;
    }

    public Calendar _roundWeek(){
	Calendar c = _roundDay();
	while ( c.get( c.DAY_OF_WEEK ) != c.MONDAY )
	    c.setTimeInMillis( c.getTimeInMillis() - ( 1000 * 60 * 60 * 24 ) );
	return c;
    }

    public Calendar _roundDay(){
        Calendar c = _roundHour();
        c.set( c.HOUR_OF_DAY , 0 );
        return c;
    }

    public Calendar _roundHour(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( _time );
        c.set( c.MILLISECOND , 0 );
        c.set( c.SECOND , 0 );
        c.set( c.MINUTE , 0 );
        return c;
    }

    public JSDate roundMinutes( int min ){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( _time );
        c.set( c.MILLISECOND , 0 );
        c.set( c.SECOND , 0 );
        
        double m = c.get( c.MINUTE );
        m = m / min;
        
        c.set( c.MINUTE , min * (int)m );
        
        return new JSDate( c );
    }

    private void _cal(){
        if ( _c != null )
            return;
        _c = Calendar.getInstance();
        _c.setTimeInMillis( _time );
    }

    public int compareTo( Object o ){
        long t = -1;
        if ( o instanceof JSDate )
            t = ((JSDate)o)._time;
        
        if ( t < 0 )
            return 0;
        
        long diff = _time - t;
        if ( diff == 0 )
            return 0;
        if ( diff < 0 )
            return -1;
        return 1;
    }

    public int hashCode(){
        return (int)_time;
    }
    
    public boolean equals( Object o ){
        return 
            o instanceof JSDate && 
            _time == ((JSDate)o)._time;
    }

    long _time;
    Calendar _c;

    public static final DateFormat _simpleFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static final DateFormat _webFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    static {
	_webFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
    }

    private final static DateFormat[] DATE_FORMATS = new DateFormat[]{
        _webFormat , _simpleFormat
    };

}
