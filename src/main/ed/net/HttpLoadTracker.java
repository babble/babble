// HttpLoadTracker.java

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

package ed.net;

import java.util.*;
import java.text.*;

import ed.util.*;
import ed.net.httpserver.*;

public class HttpLoadTracker {

    static final int _shortSeconds = 10;

    public static class Rolling {
        
        public Rolling( String name ){
            _name = name;
            _seconds = new HttpLoadTracker( name + " per Second" , 1 , 1 );
            _short = new HttpLoadTracker( name + " per " + _shortSeconds + " Seconds" , _shortSeconds , _shortSeconds );
            _minutes = new HttpLoadTracker( name + " per Minute" , 60 , 60 );
        }

        public void hit( HttpRequest request , HttpResponse response ){
            _seconds.hit( request , response );
            _short.hit( request , response );
            _minutes.hit( request , response );
        }

	public void networkEvent(){
	    _seconds.networkEvent();
	    _short.networkEvent();
	    _minutes.networkEvent();
	}

        public void displayGraph( JxpWriter out ){
            displayGraph( out , DEFAULTS );
        }
        
        public void displayGraph( JxpWriter out , GraphOptions options ){
            _seconds.displayGraph( out , options );
            _short.displayGraph( out , options );
            _minutes.displayGraph( out , options );
        }

	public HttpLoadTracker getSeconds(){
	    return _seconds;
	}

	public HttpLoadTracker getShort(){
	    return _short;
	}

	public HttpLoadTracker getMinutes(){
	    return _minutes;
	}
            
        final String _name;
        final HttpLoadTracker _seconds;
        final HttpLoadTracker _short;
        final HttpLoadTracker _minutes;
    }
    
    public HttpLoadTracker( String name ){
        this( name , 30 , 30 );
    }

    public HttpLoadTracker( String name , int secondsInInterval , int minutesToGoBack ){
        _name = name;
        _sliceTime = 1000 * secondsInInterval;
        _intervals = ( minutesToGoBack * 60 ) / secondsInInterval;

        _requests = new ThingsPerTimeTracker( "requests" , _sliceTime , _intervals );
        _errors = new ThingsPerTimeTracker( "errors" , _sliceTime , _intervals );
        _networkEvents = new ThingsPerTimeTracker( "network events" , _sliceTime , _intervals );

        _dataIn = new ThingsPerTimeTracker( "data in" , _sliceTime , _intervals );
        _dataOut = new ThingsPerTimeTracker( "data out" , _sliceTime , _intervals );

        _totalTime = new ThingsPerTimeTracker( "time" , _sliceTime , _intervals );
    }
    
    public void networkEvent(){
	_networkEvents.hit();
    }
    
    public void hit( HttpRequest request , HttpResponse response ){
        _requests.hit();

        if ( request != null ){
            _dataIn.hit( request.totalSize() );
        }
        
        if ( response != null ){
            _dataOut.hit( response.totalSize() );
            _errors.hit( response.getResponseCode() >= 500 ? 1 : 0 );
            _totalTime.hit( response.handleTime() );
        }
    }

    public void cycle(){
        _requests.hit(0);
        _dataIn.hit(0);
        _dataOut.hit(0);
        _errors.hit(0);
	_networkEvents.hit(0);
        _totalTime.hit(0);
    }

    public void displayGraph( JxpWriter out ){
        displayGraph( out , DEFAULTS );
    }

    public void displayGraph( JxpWriter out , GraphOptions options ){
        cycle();
        out.print( "\n<div class='loadGraph' style='clear: both; height:" + ( ( options._height * 1.4 ) + 25 ) + ";'>\n" );
        
        out.print( "<h4>" );
        out.print( _name );
        out.print( "</h4>" );
        
        out.print( "<ul class='floatingList'>" );
        out.print( "<li>" );

        if ( options._requestsAndErrors ){
            printGraph( out , "Requests" , options , _requests , _errors , _networkEvents );
            out.print( "</li><li>" );
        }
        
        if ( options._data ){
            printGraph( out , "Data" , options , _dataOut , _dataIn );
            out.print( "</li><li>" );
        }
        
        if ( options._time ){
            printGraph( out , "Time" , options , _totalTime );
            out.print( "</li><li>" );
        }

        out.print( "</li>" );
        out.print( "</ul>" );
        
        out.print( "\n</div>\n" );
    }

    public static void printGraph( JxpWriter out , String name , GraphOptions options , ThingsPerTimeTracker ... ts  ){
        if ( name != null ){
            out.print( "<h5>" );
            out.print( name );
            out.print( "</h5>" );
        }
	
        out.print( "<img width=" + options._width + " height=" + options._height + " src=\"http://chart.apis.google.com/chart?cht=lc&chd=t:" );
	
	double max = 0;
	for ( ThingsPerTimeTracker t : ts )
	    max = Math.max( max , t.max() );
	
	
	for ( int i=0; i<ts.length; i++ ){
	    if ( i > 0 )
		out.print( "|" );	
	    printGraphList( out , max , ts[i] );
        }
        
        out.print( "&chs=" );
        out.print( options._width );
        out.print( "x" );
        out.print( options._height );

	out.print( "&chdl=" );
	for ( int i=0; i<ts.length; i++ ){
	    if ( i > 0 )
		out.print( "|" );
	    out.print( ts[i].getName() );
	}

	if ( name != null && name.equalsIgnoreCase( "data" ) ){
	    max = max / 1024;
	}
	
        out.print( "&chm=r&chco=0000ff,00ff00,ff0000&chxt=y,x&chg=10,25&" );
	out.print( "chxl=0:|0|" + Math.round( max ) + "|1:|" +  _format( ts[0].beginning() )  + "|" + _format( ts[0].bucket() ) );
	out.print( "\" ><br>" );
    }

    static void printGraphList( JxpWriter out , double max , ThingsPerTimeTracker t ){
	for ( int i=t.size()-1; i>=0; i-- ){
	    out.print( Math.round( ( 100 * t.get( i ) ) / max ) );
	    if ( i > 0 )
		out.print( "," );
	}
    }
    
    static String _format( long t ){
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
    final ThingsPerTimeTracker _networkEvents;
    final ThingsPerTimeTracker _totalTime;

    public static class GraphOptions {
        
        public GraphOptions(){
            this( 600 , 150 , true , true , false );
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

    public static final GraphOptions DEFAULTS = new GraphOptions();
    private static final DateFormat DATE_TIME = new SimpleDateFormat( "hh:mm:ss" );
}
