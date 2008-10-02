// HttpLoadTracker.java

package ed.net;

import java.util.*;
import java.text.*;

import ed.util.*;
import ed.net.httpserver.*;

public class HttpLoadTracker {
    public HttpLoadTracker( String name , int secondsInInterval , int minutesToGoBack ){
        _name = name;
        _sliceTime = 1000 * secondsInInterval;
        _intervals = ( minutesToGoBack * 60 ) / secondsInInterval;

        _requests = new ThingsPerTimeTracker( _sliceTime , _intervals );
        _dataIn = new ThingsPerTimeTracker( _sliceTime , _intervals );
        _dataOut = new ThingsPerTimeTracker( _sliceTime , _intervals );
        _errors = new ThingsPerTimeTracker( _sliceTime , _intervals );
        _totalTime = new ThingsPerTimeTracker( _sliceTime , _intervals );
    }

    public void hit( HttpRequest request , HttpResponse response ){
        _requests.hit();
        _dataIn.hit( request.totalSize() );
        _dataOut.hit( response.totalSize() );
        _errors.hit( response.getResponseCode() >= 500 ? 1 : 0 );
        _totalTime.hit( response.handleTime() );
    }

    public void displayGraph( JxpWriter out ){
        displayGraph( out , DEFAULTS );
    }

    public void displayGraph( JxpWriter out , GraphOptions options ){
        out.print( "\n<div class='loadGraph'>\n" );

        out.print( "<h3>" );
        out.print( _name );
        out.print( "</h3>" );
        
        if ( options._requestsAndErrors )
            printGraph( out , "Requests" , options , _requests , _errors , "requests" , "errors" );
        
        if ( options._data )
            printGraph( out , "Data" , options , _dataOut , _dataIn , "data out" , "data in" );
        
        if ( options._time )
            printGraph( out , "Time" , options , _totalTime , null , "time" , null );
        
        out.print( "\n</div>\n" );
    }

    void printGraph( JxpWriter out , String name , GraphOptions options , ThingsPerTimeTracker t1 , ThingsPerTimeTracker t2 , String n1 , String n2 ){
	out.print( "<h3>" );
	out.print( name );
    	out.print( "</h3>" );
	
        out.print( "<img width=" + options._width + " height=" + options._height + " src=\"http://chart.apis.google.com/chart?cht=lc&chd=t:" );
	
	double max = Math.max( t1.max() , t2.max() );
	
	printGraphList( out , max , t1 );
	
        if ( t2 != null ){
            out.print( "|" );
	    printGraphList( out , max , t2 );
        }
        
        out.print( "&chs=" );
        out.print( options._width );
        out.print( "x" );
        out.print( options._height );
	
	if ( n1 != null ){
	    out.print( "&chdl=" );
	    out.print( n1 );
	    if ( n2 != null ){
		out.print( "|" );
		out.print( n2 );
	    }
	}

	if ( name.equalsIgnoreCase( "data" ) ){
	    max = max / 1024;
	}
	
        out.print( "&chm=r&chco=00ff00,0000ff&chxt=y,x&" );
	out.print( "chxl=0:|0|" + Math.round( max ) + "|1:|" +  _format( t1.beginning() )  + "|" + _format( t1.bucket() ) );
	out.print( "\" ><br>" );
    }

    void printGraphList( JxpWriter out , double max , ThingsPerTimeTracker t ){
	for ( int i=t.size()-1; i>=0; i-- ){
	    out.print( Math.round( ( 100 * t.get( i ) ) / max ) );
	    if ( i > 0 )
		out.print( "," );
	}
    }
    
    String _format( long t ){
	Date d = new Date( t );
	synchronized ( DATE_TIME ){
	    return DATE_TIME.format( d );
	}
    }

    final String _name;
    final long _sliceTime;
    final int _intervals;

    final ThingsPerTimeTracker _requests;
    final ThingsPerTimeTracker _dataIn;
    final ThingsPerTimeTracker _dataOut;
    final ThingsPerTimeTracker _errors;
    final ThingsPerTimeTracker _totalTime;

    public static class GraphOptions {
        
        public GraphOptions(){
            this( 600 , 100 , true , true , false );
        }

        public GraphOptions( int width , int height , boolean requestsAndErrors , boolean data , boolean time ){
            _width = width;
            _height = height;
            _requestsAndErrors = requestsAndErrors;
            _data = data;
            _time = time;

        }

        int _width;
        int _height;
        boolean _requestsAndErrors;
        boolean _data;
        boolean _time;
        
    }

    static final GraphOptions DEFAULTS = new GraphOptions();
    static final DateFormat DATE_TIME = new SimpleDateFormat( "hh:mm:ss" );
}
