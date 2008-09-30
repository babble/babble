// HttpLoadTracker.java

package ed.net;

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
            printGraph( out , options , _requests , _errors );
        
        if ( options._data )
            printGraph( out , options , _dataIn , _dataOut );
        
        if ( options._time )
            printGraph( out , options , _totalTime , null );
        
        out.print( "\n</div>\n" );
    }

    void printGraph( JxpWriter out , GraphOptions options , ThingsPerTimeTracker t1 , ThingsPerTimeTracker t2 ){
        out.print( "<img src=\"http://chart.apis.google.com/chart?cht=lc&chd=t:" );

        for ( int i=t1.size(); i>=0; i-- ){
            out.print( t1.get( i ) );
            if ( i > 0 )
                out.print( "," );
        }
        
        if ( t2 != null ){
            out.print( "|" );
            for ( int i=t2.size(); i>=0; i-- ){
                out.print( t2.get( i ) );
                if ( i > 0 )
                    out.print( "," );
            }
        }
        
        out.print( "&chs=" );
        out.print( options._width );
        out.print( "x" );
        out.print( options._height );
        
        out.print( "\" >" );
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
            this( 600 , 200 , true , true , false );
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
}
