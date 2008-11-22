// HttpMonitor.java

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


public abstract class HttpMonitor implements HttpHandler {
    
    public static enum Status { OK , WARN , ERROR , FATAL };

    public HttpMonitor( String name ){
        this( name , false );
    }

    public HttpMonitor( String name , boolean fork ){
        _name = name;
        _fork = fork;
        _uri = "/~" + name;

        StringBuilder buf = new StringBuilder();
        buf.append( "<html>" );
        
        buf.append( "<head>" );
        buf.append( "<title>" ).append( DNSUtil.getLocalHost() ).append( " " ).append( _name ).append( "</title>" );
        buf.append( "<style>\n" );
        buf.append( " body { font-size: .65em; font-family: Monaco; }\n" );
        buf.append( " table { font-size: 10px; }\n" );
        buf.append( " th { backgroud: #dddddd; text-align:left; }\n" );
        buf.append( " form { display: inline; }\n" );
        buf.append( " .floatingList li { float: left; list-style-type:none; }\n" );
        buf.append( " bottomLine { border-bottom: 1px solid black; }\n" );
        buf.append( " .warn { color: #FF6600; }\n" );
        buf.append( " .error { color: red; font-decoration: bold; }\n" );
        buf.append( " .fatal { color: red; font-decoration: bold; }\n" );
        addStyle( buf );
        buf.append( "</style>\n" );
        
        buf.append( "<link rel=\"shortcut icon\" href=\"http://static.10gen.com/www.10gen.com/assets/images/favicon.ico\" />" );
        
        
        buf.append( "</head>" );            
        
        buf.append( "<body>" );
        _header = buf.toString();
        
        _addAll( name );
    }
    
    protected boolean allowed( HttpRequest request ){
        String h = request.getHost();
        if ( h == null )
            return false;
	
	if ( h.equals( "127.0.0.1" ) )
	    return true;
        
        if ( ! acceptableHost( h ) )
            return false;

        if ( AUTH_COOKIE == null ){
            System.err.println( "WARNING: no cookie info, letting everyone in" );
            return true;
        }
        
        if ( AUTH_COOKIE.equalsIgnoreCase( request.getCookie( "auth" ) ) )
            return true;
	
        if ( AUTH_COOKIE.equalsIgnoreCase( request.getParameter( "auth" ) ) )
            return true;

        return false;
    }

    protected boolean acceptableHost( String host ){
        if (  host.endsWith( "." + Config.getInternalDomain() ) )
            return true;
        
        if ( DNSUtil.isDottedQuad( host ) )
            return true;

        System.out.println( "bad host [" + host + "]" );
        return false;
    }
    
    protected boolean uriOK( String uri ){ return false; };
    protected void addStyle( StringBuilder buf ){}
    public abstract void handle( MonitorRequest request );
    
    public boolean handles( HttpRequest request , Info info ){
        
        if ( ! ( request.getURI().equalsIgnoreCase( _uri ) || uriOK( request.getURI() ) ) )
            return false;

        if ( ! allowed( request ) )
            return false;

        info.fork = _fork;
        info.admin = true;
        
        return true;
    }
    
    public boolean handle( HttpRequest request , HttpResponse response ){

        if ( AUTH_COOKIE != null && AUTH_COOKIE.equalsIgnoreCase( request.getParameter( "auth" ) ) ){
            Cookie c = new Cookie( "auth" , AUTH_COOKIE );
            if ( request.getHost().endsWith( Config.getInternalDomain() ) )
                c.setDomain( Config.getInternalDomain() );
            c.setPath( "/" );
            c.setMaxAge( 86400 * 30 );
	    
            response.addCookie( c );
        }
	
        final JxpWriter out = response.getJxpWriter();
	final MonitorRequest mr = new MonitorRequest( out , request , response );
	
	boolean html = mr.html();

        if ( ! html )
            response.setHeader( "Content-Type" , "text/plain" );
        else if ( html ) {
            out.print( _header );
            out.print( _allContent );
            String section = _section();
            if ( section != null ){
                String sc = _subContent.get( section );
                if ( sc != null )
                    out.print( sc );
            }
        }
	
        try {
            handle( mr );
	    if ( mr.json() ){
		out.print( JSON.serialize( mr._data ) );
	    }
        }
        catch ( Exception e ){
            e.printStackTrace();
            out.print( e.toString() ).print( "<br>\n" );
            for ( StackTraceElement element : e.getStackTrace() )
                out.print( element + "<br>\n" );
        }
	
        if ( html ){
	    
	    out.print( "\n<hr>\n" );
	    
	    final String full = request.getFullURL();
	    
	    int refresh = request.getInt( "refresh" , 0 );
	    if ( refresh > 0 ){
		out.print( "<meta http-equiv='refresh' content='" + refresh + "'/>\n" );
		out.print( "<br><a href=\"" );
		out.print( full.replace( "refresh=\\d+" , "" ) );
		out.print( "\">stop refresh</a>" );
	    }
	    else {
		out.print( "<a href=\"" );
		out.print( full );
		if ( full.indexOf( "?" ) < 0 )
		    out.print( "?" );
		else 
		    out.print( "&" );
		out.print( "refresh=10\" >auto refresh this page</a>" );
	    }
		
	    out.print("<br>" );
	    out.print( (new java.util.Date()).toString() );
	    out.print( "<bR>" );
	    out.print( DNSUtil.getLocalHostString() );

            out.print( "\n</body></html>" );
	}
        
        return true;
    }
    
    public double priority(){
        return Double.MIN_VALUE;
    }
    
    public String getName(){
        return _name;
    }
    
    public String getURI(){
        return _uri;
    }

    String _section(){
        return _section( _name );
    }

    private static String _section( String name ){
        int idx = name.indexOf( "-" );
        if ( idx < 0 )
            return name.toLowerCase();
        return name.substring( 0 , idx ).toLowerCase();
    }

    private static void _addAll( String name ){

        if ( name.contains( "-" ) ){
            // sub menu item
            String section = _section( name );
            List<String> sub = _subs.get( section );
            if ( sub == null ){
                sub = new ArrayList<String>();
                _subs.put( section , sub );
            }
            if ( ! sub.contains( name ) )
                sub.add( name );
            Collections.sort( sub );

            StringBuilder buf = new StringBuilder( section + " : " );
            for ( String t : sub ){
                buf.append( "<a href='/~" + t + "'>" + t.substring( section.length() + 1 ) + "</a> | " );
            }
            buf.append( "<hr>" );            
            _subContent.put( section , buf.toString() );
            return;
        }

        _all.add( name );
        Collections.sort( _all );
        
        StringBuilder buf = new StringBuilder();
        for ( String t : _all ){
            buf.append( "<a href='/~" + t + "'>" + t + "</a> | " );
        }
        buf.append( "<hr>" );
        _allContent = buf.toString();
    }

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
	    else 
		_out.print( data.toString() );
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

	private final JSObject _data;
	private final Stack<JSObject> _cur;
    }

    
    final String _name;
    final boolean _fork;
    final String _uri;
    final String _header;
    
    static final List<String> _all = new ArrayList<String>();
    static String _allContent = "";
    static final Map<String,List<String>> _subs = new HashMap<String,List<String>>();
    static final Map<String,String> _subContent = new HashMap<String,String>();
    
    public static final String AUTH_COOKIE = Config.get().getProperty( "authCookie" , null );
    
    public static final String getAuthCookie(){
        return AUTH_COOKIE;
    }

    // ----------------------------------------
    // Some Basic Monitors
    // ----------------------------------------

    
    public static final class MemMonitor extends HttpMonitor {

        MemMonitor(){
            super( "mem" );
            _r = Runtime.getRuntime();
        }

        public void handle( MonitorRequest request ){
            print( request , "before" );
            
            if ( request.getRequest().getBoolean( "gc" , false ) ){
                System.gc();
                
                print( request , "after" );
            }
            
        }
	
        void print( MonitorRequest request , String name ){
            request.startData( name );
            request.addData( "max" , MemUtil.bytesToMB( _r.maxMemory() ) );
            request.addData( "total" , MemUtil.bytesToMB( _r.totalMemory() ) );
            request.addData( "free" , MemUtil.bytesToMB( _r.freeMemory() ) );
            request.addData( "used" , MemUtil.bytesToMB( _r.totalMemory() - _r.freeMemory() ) );
            request.endData();
        }

        final Runtime _r;
    }

    public static final class LogMonitor extends HttpMonitor {
        LogMonitor(){
            super( "logs" );
        }

        public void handle( MonitorRequest mr ){
            printLastLogMessages( mr , -1 );
        }        
        
    }
    
    /**
     * @param max -1 means infinite
     */
    public static void printLastLogMessages( MonitorRequest mr , int max ){
        CircularList<Event> l = InMemoryAppender.getInstance().getRecent();
        int size = l.size();
        if ( max > 0 )
            size = Math.min( max , size );

        JxpWriter out = mr.getWriter();
        
        out.print( "<table border='1'>" );
        
        for ( int i=0; i<size; i++ ){
            Event e = l.get( i );
            out.print( "<tr>" );
            mr.addTableCell( e.getDate() );
            mr.addTableCell( e.getLoggerName() );
            mr.addTableCell( e.getLevel() , e.getLevel().toString().toLowerCase() );
            mr.addTableCell( e.getMsg() );
            out.print( "</tr>" );
            
            if ( e.getThrowable() != null ){
                out.print( "<tr><td colspan=4'>" );
                out.print( e.getThrowable() + "<BR>" );
                for ( StackTraceElement element : e.getThrowable().getStackTrace() )
                    out.print( element + "<BR>\n" );
                out.print( "</td></tr>" );
            }
        }
        
        out.print( "</table>" );        
    }
    
    
    public static class ThreadMonitor extends HttpMonitor {

        ThreadMonitor(){
            super( "threads" );
        }

        protected void addStyle( StringBuilder buf ){
            
            buf.append( ".js { color: red; }\n" );
            buf.append( ".ed { color: blue; }\n" );

        }

        public void handle( MonitorRequest mr ){
            
	    mr.addHeader( "Threads" );
	    final JxpWriter out = mr.getWriter();

            final Map<Thread,StackTraceElement[]> all = Thread.getAllStackTraces();
            final Thread cur = Thread.currentThread();
            
            final String filter = getFilter( mr.getRequest() );
            
            if ( filter != null )
                out.print( "filter : <b>" ).print( filter ).print( "</b><br>" );

            final StackTraceHolder holder = StackTraceHolder.getInstance();

            for ( final Map.Entry<Thread,StackTraceElement[]> t : all.entrySet() ){
                
                final Thread it = t.getKey();

                if ( it == cur )
                    continue;
                
                if ( filter != null && ! _match( t , filter ) )
                    continue;
                
                out.print( it.getName() ).print( " " ).print( it.getId() ).print( " " ).print( it.getState().toString() ).print( "<br>" );
                
                out.print( "<ul>" );
                final StackTraceElement[] es = t.getValue();
                for ( int i=0; i<es.length; i++){
                    es[i] = holder.fix( es[i] );
                    if ( es[i] == null )
                        continue;

                    final String str = es[i].toString();
                    
                    String cls = "";
                    if ( str.startsWith( "ed.js.gen" ) )
                        cls = "js";
                    else if ( str.startsWith( "ed." ) )
                        cls = "ed";
                    
                    out.print( "<li" );
                    out.print( " class='" ).print( cls ).print( "' " );
                    out.print(" >" ).print( str ).print( "</li>" );
                }
                out.print( "</ul>" );

                out.print( "<hr>" );
            }
        }

        boolean _match( Map.Entry<Thread,StackTraceElement[]> t , String filter ){
            if ( t.getKey().getName().contains( filter ) )
                return true;

            final StackTraceElement[] es = t.getValue();

            for ( int i=0; i<es.length; i++ ){
                final StackTraceElement e = es[i];
                if ( e == null )
		    continue;
		
                if ( ( e.getClassName() != null && e.getClassName().contains( filter )  )
                     || ( e.getFileName() != null && e.getFileName().contains( filter ) )
                     || ( e.getMethodName() != null && e.getMethodName().contains( filter ) )
                     )
                    return true;
            }

            return false;
        }
        
        public static String getFilter( HttpRequest request ){
            return request.getParameter( "f" );
        }

    }


    public static class FavIconHack implements HttpHandler {
        
        public boolean handles( HttpRequest request , Info info ){
            if ( ! request.getURI().equals( "/favicon.ico" ) )
                return false;
            
            String host = request.getHost();
            if ( host == null )
                return false;
            
            if ( ed.appserver.AppContextHolder.isCDNHost( host ) )
                return true;

            if ( ! host.contains( "10gen" ) )
                return false;

            String ref = request.getHeader( "Referer" );
            if ( ref == null )
                return false;
            
            int idx = ref.indexOf( "/~" );
            if ( idx < 0 || idx + 3 >= ref.length() )
                return false;
            
            return Character.isLetter( ref.charAt( idx + 2 ) );
        }
        
        public boolean handle( HttpRequest request , HttpResponse response ){
            response.setResponseCode( 404 );
            response.setCacheTime( 86400 );
            return true;
        }

        public double priority(){
            return Double.MIN_VALUE;
        }
    }
}
