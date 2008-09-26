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
    
    protected void addStyle( StringBuilder buf ){}
    public abstract void handle( JxpWriter out , HttpRequest request , HttpResponse response );
    
    public boolean handles( HttpRequest request , Info info ){
        if ( ! request.getURI().equals( _uri ) )
            return false;

        info.fork = false;
        info.admin = true;
        
        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){
        JxpWriter out = response.getJxpWriter();
        
        if ( _plainText )
            response.setHeader( "Content-Type" , "text/plain" );
        else {
            out.print( _header );
            out.print( _allContent );
        }
        try {
            handle( out , request , response );
        }
        catch ( Exception e ){
            out.print( e.toString() ).print( "<br>" );
            for ( StackTraceElement element : e.getStackTrace() )
                out.print( element + "<br>\n" );
        }
        if ( ! _plainText )
            out.print( "</body></html>" );
    }
    
    protected void startTable( JxpWriter out ){
        out.print( "<table border='1' >" );
    }

    protected void endTable( JxpWriter out ){
        out.print( "</table>" );
    }

    protected void addTableRow( JxpWriter out , Object header , Object data ){
	addTableRow( out , header , data , null );
    }
    
    protected void addTableRow( JxpWriter out , Object header , Object data , String valueClass ){
        out.print( "<tr><th>" );
        out.print( header == null ? "null" : header.toString() );
        out.print( "</th><td " );
	if ( valueClass != null )
	    out.print( "class=\"" + valueClass + "\" " );
	out.print( ">" );
        out.print( data == null ? "null" : data.toString() );
        out.print( "</td></tr>" );
    }

    public double priority(){
        return Double.MIN_VALUE;
    }

    private static void _addAll( String name ){
        _all.add( name );
        Collections.sort( _all );

        StringBuilder buf = new StringBuilder();
        for ( String t : _all )
            buf.append( "<a href='/~" + t + "'>" + t + "</a> | " );
        buf.append( "<hr>" );
        _allContent = buf.toString();
    }
    
    final boolean _plainText;
    final String _name;
    final String _uri;
    final String _header;
    static final List<String> _all = new ArrayList<String>();
    static String _allContent = "";
    
    // ----------------------------------------
    // Some Basic Monitors
    // ----------------------------------------

    
    public static final class MemMonitor extends HttpMonitor {

        MemMonitor(){
            super( "mem" , false );
            _r = Runtime.getRuntime();
        }

        public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
            print( out );
            
            Object gc = request.get("gc");
            if ( gc != null && gc.equals( "t" ) ){
                System.gc();
                out.print( "\n\n after gc\n\n" );
                
                print( out );
            }
            
        }
        
        void print( JxpWriter out ){
            startTable( out );
            addTableRow( out , "max" , MemUtil.bytesToMB( _r.maxMemory() ) );
            addTableRow( out , "total" , MemUtil.bytesToMB( _r.totalMemory() ) );
            addTableRow( out , "free" , MemUtil.bytesToMB( _r.freeMemory() ) );
            addTableRow( out , "used" , MemUtil.bytesToMB( _r.totalMemory() - _r.freeMemory() ) );
            endTable( out );
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

        public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
            
            out.print( "Threads<br>" );

            final Map<Thread,StackTraceElement[]> all = Thread.getAllStackTraces();
            final Thread cur = Thread.currentThread();
            
            final String filter = getFilter( request );
            
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
            String f = request.getParameter( "f" );
            if ( f != null )
                return f;
            
            f = request.getHost();
            if ( f == null )
                return null;
            
            if ( f.endsWith( ".10gen.cc" ) )
                return null;

            if ( f.endsWith( ".10gen.com" ) )
                f = f.substring( 0 , f.length() - 10 ) + ".com";
            
            f = ed.net.DNSUtil.getJustDomainName( f );
            
            if ( f.startsWith( "local" ) )
                return null;

            return f;
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
