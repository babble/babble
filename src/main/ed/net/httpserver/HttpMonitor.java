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

import ed.lang.*;
import ed.util.*;
import ed.net.*;

public abstract class HttpMonitor implements HttpHandler {
    

    public HttpMonitor( String name ){
        this( name , false );
    }

    public HttpMonitor( String name , boolean plainText ){
        _name = name;
        _plainText = plainText;
        _uri = "/~" + name;

        if ( _plainText )
            _header = null;
        else {
            StringBuilder buf = new StringBuilder();
            buf.append( "<html>" );
            
            buf.append( "<head>" );
            buf.append( "<title>" ).append( DNSUtil.getLocalHost() ).append( " " ).append( _name ).append( "</title>" );
            buf.append( "<style>\n" );
            buf.append( " body { font-size: .65em; font-family: Monaco; }\n" );
            buf.append( " table { font-size: 10px; }\n" );
            buf.append( " th { backgroud: #dddddd; text-align:left; }\n" );
            buf.append( " .floatingList li { float: left; list-style-type:none; }\n" );
            buf.append( " bottomLine { border-bottom: 1px solid black; }\n" );
	    buf.append( " .warn { color: orange; }\n" );
	    buf.append( " .error { color: red; font-decoration: bold; }\n" );
            addStyle( buf );
            buf.append( "</style>\n" );
            buf.append( "</head>" );            
            
            buf.append( "<body>" );
            _header = buf.toString();
        }
        
        _addAll( name );
    }
    
    protected boolean allowed( HttpRequest request ){
        String h = request.getHost();
        if ( h == null )
            return false;
	
	if ( h.equals( "127.0.0.1" ) )
	    return true;

        if ( ! h.endsWith( "." + Config.getInternalDomain() ) )
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
    
    protected void addStyle( StringBuilder buf ){}
    public abstract void handle( MonitorRequest request );
    
    public boolean handles( HttpRequest request , Info info ){
        
        if ( ! request.getURI().equalsIgnoreCase( _uri ) )
            return false;

        if ( ! allowed( request ) )
            return false;

        info.fork = false;
        info.admin = true;
        
        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){

        if ( AUTH_COOKIE != null && AUTH_COOKIE.equalsIgnoreCase( request.getParameter( "auth" ) ) ){
            Cookie c = new Cookie( "auth" , AUTH_COOKIE );
            c.setDomain( "10gen.cc" );
            c.setPath( "/" );
            c.setMaxAge( 86400 * 30 );
	    
            response.addCookie( c );
            response.sendRedirectTemporary( request.getFullURL().replaceAll( "auth=" + AUTH_COOKIE , "" ) );
            return;
        }

        JxpWriter out = response.getJxpWriter();
	
	MonitorRequest mr = new MonitorRequest( out , request , response );

        if ( _plainText )
            response.setHeader( "Content-Type" , "text/plain" );
        else {
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
        }
        catch ( Exception e ){
            e.printStackTrace();
            out.print( e.toString() ).print( "<br>" );
            for ( StackTraceElement element : e.getStackTrace() )
                out.print( element + "<br>\n" );
        }

        if ( ! _plainText )
            out.print( "</body></html>" );
    }
    
    public double priority(){
        return Double.MIN_VALUE;
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
	}

	public void addHeader( String header ){
	    _out.print( "<h3>" );
	    _out.print( header );
	    _out.print( "</h3>" );
	}

	public void addSpacingLine(){
	    if ( _json )
		return;
	    _out.print( "<br>" );
	}
	
	public void startTable(){
	    _out.print( "<table border='1' >" );
	}
	
	public void endTable(){
	    _out.print( "</table>" );
	}
	
	public void addTableRow( Object header , Object data ){
	    addTableRow( header , data , null );
	}
	
	public void addTableCell( Object data ){
	    _out.print( "<td>" );
	    if ( data == null )
		_out.print( "null" );
	    else 
		_out.print( data.toString() );
	    _out.print( "</td>" );
	}
	
	
	public void addTableRow( Object header , Object data , String valueClass ){
	    _out.print( "<tr><th>" );
	    _out.print( header == null ? "null" : header.toString() );
	    _out.print( "</th><td " );
	    if ( valueClass != null )
		_out.print( "class=\"" + valueClass + "\" " );
	    _out.print( ">" );
	    _out.print( data == null ? "null" : data.toString() );
	    _out.print( "</td></tr>" );
	}

	public JxpWriter getWriter(){
	    if ( _json )
		throw new RuntimeException( "this is a json request so can't get a raw writer" );
	    return _out;
	}

	public HttpRequest getRequest(){
	    return _request;
	}

	public HttpResponse getResponse(){
	    return _response;
	}
	
	private final JxpWriter _out;
	private final HttpRequest _request;
	private final HttpResponse _response;
	
	protected final boolean _json ;
    }

    
    final boolean _plainText;
    final String _name;
    final String _uri;
    final String _header;
    
    static final List<String> _all = new ArrayList<String>();
    static String _allContent = "";
    static final Map<String,List<String>> _subs = new HashMap<String,List<String>>();
    static final Map<String,String> _subContent = new HashMap<String,String>();
    
    static final String AUTH_COOKIE = Config.get().getProperty( "authCookie" , null );
    
    // ----------------------------------------
    // Some Basic Monitors
    // ----------------------------------------

    
    public static final class MemMonitor extends HttpMonitor {

        MemMonitor(){
            super( "mem" , false );
            _r = Runtime.getRuntime();
        }

        public void handle( MonitorRequest request ){
            print( request );
            
            if ( request.getRequest().getBoolean( "gc" , false ) ){
                System.gc();
                request.getWriter().print( "\n\n after gc\n\n" );
                
                print( request );
            }
            
        }
        
        void print( MonitorRequest request ){
            request.startTable();
            request.addTableRow( "max" , MemUtil.bytesToMB( _r.maxMemory() ) );
            request.addTableRow( "total" , MemUtil.bytesToMB( _r.totalMemory() ) );
            request.addTableRow( "free" , MemUtil.bytesToMB( _r.freeMemory() ) );
            request.addTableRow( "used" , MemUtil.bytesToMB( _r.totalMemory() - _r.freeMemory() ) );
            request.endTable();
        }

        final Runtime _r;
    }

    
    public static class ThreadMonitor extends HttpMonitor {

        ThreadMonitor(){
            super( "threads" , false );
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
        
        public void handle( HttpRequest request , HttpResponse response ){
            response.setResponseCode( 404 );
            response.setCacheTime( 86400 );
        }

        public double priority(){
            return Double.MIN_VALUE;
        }
    }
}
