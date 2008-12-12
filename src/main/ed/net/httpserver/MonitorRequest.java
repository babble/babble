// MonitorRequest.java

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

package ed.net.httpserver;

import java.util.*;
import javax.servlet.http.*;

import ed.js.*;
import ed.log.*;
import ed.net.*;
import ed.lang.*;
import ed.util.*;
import static ed.net.httpserver.HttpMonitor.Status;


public class MonitorRequest {
	

    public MonitorRequest( JxpWriter out , HttpRequest request , HttpResponse response ){
        _out = out;
        _request = request;
        _response = response;

        _json = _request.getBoolean( "json" , false );

        if ( _json ){
            _data = new JSObjectBase();
            _cur = new Stack<JSObject>();
            _cur.push( _data );
            _response.setContentType( "application/json" );
        }
        else {
            _data = null;
            _cur = null;
        }

    }

    // DATA API

    public void startData(){
        startData( null );
    }

    public void startData( String type , String ... fields ){
        if ( _json ){
            JSObject next = _cur.peek();
            if ( type != null ){
                JSObject o = next;
                next = (JSObject)(o.get( type ));
                if ( next == null ){
                    next = new JSObjectBase();
                    o.set( type , next );
                }
            }
            _cur.push( next );
            next.set( "_fields" , fields );
        }
        else {
            if ( type != null )
                addHeader( type );
            startTable();
            if ( fields.length > 0 )
                addTableRow( "" , (Object[])fields );
        }
	    
    }
	
    public void endData(){
        if ( _json ){
            _cur.pop().removeField( "_fields" );
        }
        else {
            endTable();
        }
    }
	
    public void addData( String name , Object ... values ){
        addData( name , Status.OK , values );
    }
	
    public void addData( String name , Status status , Object ... values ){
        if ( _json ){
            JSObject o = _cur.peek();

            if ( values.length == 0 ){}
            else if ( values.length == 1 ){
                o.set( name.toString() , values[0] );
            }
            else {
                String[] fields = (String[])o.get( "_fields" );
                if ( fields == null )
                    throw new RuntimeException( "no _fields" );
                    
                JSObject foo = new JSObjectBase();
                int max = Math.min( fields.length , values.length );
                for ( int i=0; i<max; i++ )
                    foo.set( fields[i] , values[i] );

                o.set( name.toString() , foo );
            }

        }
        else {
            addTableRow( name , status , values );
        }
    }

    public void addMessage( String message ){
        if ( html() ){
            _out.print( "<B>" );
            _out.print( message );
            _out.print( "</B>" );
        }
        else {
            JSObject o = _cur.peek();
            if ( o.get( "messages" ) == null )
                o.set( "messages" , new JSArray() );
            ((JSArray)o).add( new JSString( message ) );
        }
    }



    // RAW HTML API
        
    public void addHeader( String header ){
        if ( html() ){
            _out.print( "<h3>" );
            _out.print( header );
            _out.print( "</h3>" );
        }
    }

    public void addSpacingLine(){
        if ( _json )
            return;
        _out.print( "<br>" );
    }
	
    public void startTable(){
        _assertIfJson();
        _out.print( "<table border='1' >" );
    }
	
    public void endTable(){
        _assertIfJson();
        _out.print( "</table>" );
    }
	

	
    public void addTableCell( Object data ){
        addTableCell( data , null );
    }
        
    public void addTableCell( Object data , String cssClass ){
        _assertIfJson();
        _out.print( "<td " );
        if ( cssClass != null )
            _out.print( " class='" + cssClass + "' " );
        _out.print( ">" );

        if ( data == null )
            _out.print( "null" );
        else {
            String s = HttpMonitor.escape( data.toString() ).trim();
            if ( s.length() == 0 )
                s = "&nbsp;";
            _out.print( s );
        }
        
        _out.print( "</td>" );
    }
    
    public void addTableRow( String header , Object ... datas ){
        addTableRow( header , Status.OK ,  datas  );
        _assertIfJson();
    }
	
    public void addTableRow( String header , Status status , Object ... datas ){
        _assertIfJson();
        _out.print( "<tr><th>" );
        _out.print( header == null ? "null" : header.toString() );
        _out.print( "</th>" );

        for ( int i=0; i<datas.length; i++ ){
            Object data = datas[i];
                
            _out.print( "<td " );
            if ( status != null )
                _out.print( "class=\"" + status.name().toLowerCase() + "\" " );
            _out.print( ">" );
                
            _out.print( data == null ? "null" : data.toString() );
                
            _out.print( "</td>" );
        }
            
        _out.print( "</tr>" );
    }
	
    // BASICS

    public boolean json(){
        return _json;
    }
	
    public boolean html(){
        return ! _json;
    }

    public JxpWriter getWriter(){
        _assertIfJson();
        return _out;
    }
	
    public HttpRequest getRequest(){
        return _request;
    }

    public HttpResponse getResponse(){
        return _response;
    }

    private void _assertIfJson(){
        if ( _json )
            throw new RuntimeException( "this is a json request, and you're trying to do a non-json thing" );
    }

    public void print( Object o ){
        _assertIfJson();
        if ( o == null )
            o = "null";
        _out.print( o.toString() );
    }
	
    private final JxpWriter _out;
    private final HttpRequest _request;
    private final HttpResponse _response;
	
    protected final boolean _json;

    final JSObject _data;
    private final Stack<JSObject> _cur;
}

    
