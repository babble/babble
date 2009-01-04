// S3Server.java

package ed.db;

import ed.js.*;
import ed.log.*;
import ed.net.httpserver.*;

public abstract class S3Server implements HttpHandler {

    S3Server( int verboseLevel ){
        _logger = Logger.getLogger( "s3server" );
	_logger.setLevel( Level.forDebugId( verboseLevel ) );
    }
    
    public boolean handles( HttpRequest request , Info info ){
        info.fork = true;
        return true;
    }
    
    public boolean handle( HttpRequest request , HttpResponse response ){
        try {
            _handle( request , response );
            return true;
        }
        catch ( IllegalArgumentException iae ){
            response.setResponseCode( 400 );
            response.getJxpWriter().print( "invalide request : " + iae );
            return true;
        }
    }
    
    
    void _handle( HttpRequest request , HttpResponse response ){    
        final Location loc = Location.parse( request );
    
        _logger.debug( 1 , request.getMethod() + " " + loc );
    
        if ( loc._key == null ){
            if ( request.getMethod().equalsIgnoreCase( "GET" ) ){
                response.getJxpWriter().print( "don't support listings yet" );
                response.setResponseCode( 501 );
                return;
            }
            
            if ( request.getMethod().equalsIgnoreCase( "PUT" ) ){
                // basically a no-op because buckets are created on the fly
                response.setHeader( "Location" , "/" + loc._bucket );
                response.setResponseCode( 200 );
                return;
            }
            
            throw new IllegalArgumentException( "don't know how to handle " + request.getMethod() + " without a key" );
        }

        DBCollection files = getFileCollection( loc._bucket );
        
        if ( request.getMethod().equalsIgnoreCase( "GET" ) ){
            JSFile file = (JSFile)(files.findOne( JSDictBuilder.start().put( "key" , loc._key ).get() ) );
            if ( file == null ){
                response.setResponseCode( 404 );
                return;
            }
            
            for ( String h : _s3Headers )
                response.setHeader( h , JS.toString( file.get( h ) ) );

            response.sendFile( file );
            return;
        }
        
        if ( request.getMethod().equalsIgnoreCase( "PUT" ) ){
            JSFile f = request.getPostData().getAsFile();
            f.set( "Content-Type" , request.getHeader( "Content-Type" ) );
            f.set( "filename" , request.getHeader( loc.toString() ) );

            f.set( "key" , loc._key );
            
            JSFile old = (JSFile)(files.findOne( JSDictBuilder.start().put( "key" , loc._key ).get() ) );            
            if ( old != null )
                f.set( "_id" , old.get( "_id" ) );
            
            f.save( files );

            return;
        }
        

        throw new IllegalArgumentException( "don't know how to handle " + request.getMethod() );
    }
    
    public double priority(){
        return 100;
    }

    protected abstract DBCollection getFileCollection( String bucket );
    
    static class Location {

        Location( String bucket , String key ){
            _bucket = bucket;
            _key = key;
            if ( _bucket == null || _bucket.length() == 0 )
                throw new IllegalArgumentException( "need a bucket" );
        }
        
        static Location parse( HttpRequest request ){
            return parse( request.getURI() );
        }

        static Location parse( String uri ){
            if ( ! uri.startsWith( "/" ) )
                throw new IllegalArgumentException( "bad url [" + uri + "]" );
            
            while ( uri.startsWith( "/" ) )
                uri = uri.substring(1);
            
            int idx = uri.indexOf( "/" );
            if ( idx < 0 )
                return new Location( uri , null );

            String bucket = uri.substring( 0 , idx );
            String key = uri.substring( idx + 1 );
            return new Location( bucket , key );
        }
        
        public String toString(){
            return _bucket + "." + _key;
        }

        final String _bucket;
        final String _key;
    }

    final Logger _logger;
    
    static String[] _s3Headers = new String[]{ "x-amz-meta-title" };

    static class Remote extends S3Server {
        
        Remote( int verboseLevel ){
            super( verboseLevel );
        }

        protected DBCollection getFileCollection( String bucket ){
            try {
                return DBProvider.get( bucket ).getCollection( "_files" );
            }
            catch ( Exception e ){
                throw new RuntimeException( e );
            }
        }
    }

    public static void main( String args[] )
        throws Exception {
        Remote r = new Remote( 5 );
        HttpServer s = new HttpServer( 8080 );
        s.addHandler( r );
        s.start();
        s.join();
    }
}
