// HttpMonitor.java

package ed.net.httpserver;

import java.util.*;

import ed.lang.*;
import ed.util.*;

public abstract class HttpMonitor implements HttpHandler {

    public HttpMonitor( String name ){
        _name = name;
        _uri = "/~" + name;
    }
    
    public abstract void handle( JxpWriter out , HttpRequest request , HttpResponse response );
    
    public boolean handles( HttpRequest request , Info info ){
        if ( ! request.getURI().equals( _uri ) )
            return false;

        info.fork = false;
        info.admin = true;
        
        return true;
    }
    
    public void handle( HttpRequest request , HttpResponse response ){
        response.setHeader( "Content-Type" , "text/plain" );
        JxpWriter out = response.getWriter();
        handle( out , request , response );
    }

    public double priority(){
        return Double.MIN_VALUE;
    }

    final String _name;
    final String _uri;

    // ----------------------------------------
    // Some Basic Monitors
    // ----------------------------------------

    
    public static final class MemMonitor extends HttpMonitor {

        MemMonitor(){
            super( "mem" );
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
            out.print( "max   : " ).print( MemUtil.bytesToMB( _r.maxMemory() ) ).print( "\n" );
            out.print( "total : " ).print( MemUtil.bytesToMB( _r.totalMemory() ) ).print( "\n" );
            out.print( "free  : " ).print( MemUtil.bytesToMB( _r.freeMemory() ) ).print( "\n" );
        }

        final Runtime _r;
    }

    
    public static class ThreadMonitor extends HttpMonitor {

        ThreadMonitor(){
            super( "threads" );
            _style =   
                "<style>\n" + 
                ".js { color: red; }\n" + 
                ".ed { color: blue; }\n" +
                "</style>\n";
        }

        public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
            response.setHeader( "Content-Type" , "text/html" );
            
            out.print( "<html><head>" ).print( _style ).print( "</head><body>" );

            final Map<Thread,StackTraceElement[]> all = Thread.getAllStackTraces();
            final Thread cur = Thread.currentThread();
            
            final String filter = getFilter( request );

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
            
            out.print( "</body></html>" );
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

            return f;
        }

        final String _style;
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
