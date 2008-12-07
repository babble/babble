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

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import ed.js.*;
import ed.log.*;
import ed.net.*;
import ed.lang.*;
import ed.util.*;


public abstract class HttpMonitor implements HttpHandler {
    
    public static enum Status { OK , WARN , ERROR , FATAL };
    static String _applicationType = null;
    public static void setApplicationType( String s ){
        _applicationType = s;
    }

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

        buf.append( "<link rel=\"shortcut icon\" href=\"http://static.10gen.com/www.10gen.com/assets/images/favicon.ico\" />\n" );
        buf.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/~_admin.css\" >\n" );
        buf.append( "<script src=\"/~_admin.js\" ></script>\n" );

        buf.append( "<style>\n" );
        addStyle( buf );
        buf.append( "</style>\n" );
        
        buf.append( "</head>\n" );            
        buf.append( "<body onLoad='adminOnLoad()'>\n" );
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
            if ( _applicationType != null ){
                out.print( "<span id='appType'>" );
                out.print( _applicationType );
                out.print( "</span>\n" );
            }
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

            out.print( "\n<div id='debugjs'></div></body></html>" );
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
                out.print( escape( e.getThrowable() ) + "<BR>" );
                for ( StackTraceElement element : e.getThrowable().getStackTrace() )
                    out.print( element + "<BR>\n" );
                out.print( "</td></tr>" );
            }
        }
        
        out.print( "</table>" );        
    }

    public static String escape( Object o ){
        if ( o == null )
            return "null";
        return ed.js.Encoding._escapeHTML( o.toString() );
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
            
            out.print( "<div id='threads'>\n" );

            for ( final Map.Entry<Thread,StackTraceElement[]> t : all.entrySet() ){
                
                final Thread it = t.getKey();

                if ( it == cur )
                    continue;
                
                if ( filter != null && ! _match( t , filter ) )
                    continue;
                
                out.print( it.getName() ).print( " " ).print( it.getId() ).print( " " ).print( it.getState().toString() ).print( "<br>" );
                
                FastStack<String> status = ThreadUtil.getStatus( it );
                if ( status.size() > 0 ){
                    out.print( "<div class='threadStatus'>" );
                    out.print( status.toString() );
                    out.print( "</div>" );
                }

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

            out.print( "</div>" );
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
    
    public static class AdminStaticFile implements HttpHandler {

        public AdminStaticFile( String name ){
            this( name , new File( "src/main/ed/net/httpserver/" + name ) );
        }

        public AdminStaticFile( String name , File file ){
            _name = name;
            _uri = "/~_" + _name;
            _file = file;
        }

        public boolean handles( HttpRequest request , Info info ){
            if ( ! request.getURI().equals( _uri ) )
                return false;
            
            info.fork = false;
            info.admin = true;
            
            return true;
        }
        
        public boolean handle( HttpRequest request , HttpResponse response ){
            response.setCacheTime( 3600 );
            response.sendFile( _file );
            return true;
        }
        
        public double priority(){
            return Double.MIN_VALUE;
        }

        final String _name;
        final String _uri;
        final File _file;
    }

}
