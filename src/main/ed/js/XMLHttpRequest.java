// XMLHttpRequest.java

package ed.js;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import javax.net.ssl.*;

import ed.js.func.*;
import ed.js.engine.*;

public class XMLHttpRequest extends JSObjectBase {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.XHR" );
    
    public static final int UNSENT = 0;
    public static final int OPENED = 1;
    public static final int HEADERS_RECEIVED = 2;
    public static final int LOADING = 3;
    public static final int DONE = 4;

    public final static JSFunction _cons = new JSFunctionCalls3(){

            public JSObject newOne(){
                return new XMLHttpRequest();
            }

            public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                return open( s , methodObj , urlObj , asyncObj , args );
            }
            
            public Object open( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                
                XMLHttpRequest r = (XMLHttpRequest)s.getThis();
                if ( r == null )
                    r = new XMLHttpRequest();
                
                if ( urlObj != null )
                    r.open( methodObj , urlObj , asyncObj );

                return r;
            }    

            protected void init(){
                
                _prototype.set( "open" , new JSFunctionCalls3() {
                        public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                            return open( s , methodObj , urlObj , asyncObj , args );
                        }
                    } );
                
            }
        };

    public XMLHttpRequest(){
        super( _cons );
    }

    public XMLHttpRequest( String method , String url , boolean async ){
        super( _cons );
        init( method , url , async );
    }

    void open( Object method , Object url , Object async ){
        init( method.toString() , url.toString() , JSInternalFunctions.JS_evalToBool( async ) ); 
    }

    void init( String method , String url , boolean async ){
        
        if ( ! url.substring( 0 , 4 ).equalsIgnoreCase( "http" ) )
            url = "http://" + url;

        _method = method;
        _urlString = url;
        _async = async;
        
        set( "method" , _method );
        set( "url" , _urlString );
        set( "async" , _async );
        setStatus( UNSENT );
    }
    
    void setStatus( int status ){
        set( "readyState" , status );

        JSFunction onreadystatechange = (JSFunction)get( "onreadystatechange" );
        if ( onreadystatechange != null && status > 0 )
            onreadystatechange.call( _onreadystatechangeScope , null );
             
    }

    public Object set( Object n , Object v ){
        String name = n.toString();

       if ( n.equals( "onreadystatechange" ) ){

           JSFunction f = (JSFunction)v;

           if ( f != null )
               _async = true;
           
           _onreadystatechangeScope = f.getScope().child();
           _onreadystatechangeScope.setThis( this );
           
           return super.set( n , v );
        }
       
       return super.set( n , v );
    }
    

    public XMLHttpRequest send()
        throws IOException {
        return send( null );
    }
    
    public XMLHttpRequest send( final String post )
        throws IOException {
        
        if ( ! _async )
            return _doSend( post );
        
        final Thread t = new Thread( "XMLHttpRequest-sender" ){
                public void run(){
                    try {
                        if ( DEBUG ) System.out.println( "starting async" );
                        _doSend( post );
                        if ( DEBUG ) System.out.println( "done with async" );
                    }
                    catch ( Throwable t ){
                        t.printStackTrace();
                    }
                }
            };
        
        t.start();
        return this;
    }
    
    private XMLHttpRequest _doSend( String post )
        throws IOException {

        // TODO: more real http client
        
	Socket sock = null;

        try {
            setStatus( OPENED );
            
            sock = new Socket();
            if ( get( "timeout" ) != null ){
                sock.setSoTimeout( ((Number)get( "timeout" )).intValue() );
            }
            
            if ( DEBUG ) 
                System.out.println( "connecting to [" + getHost() + ":" + getPort() + "]" );

            sock.connect( new InetSocketAddress( getHost() , getPort() ) );

            if ( isSecure() ){
                if ( DEBUG ) System.out.println( "\t making secure" );
                sock = ((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket( sock , getHost() , getPort() , true );
            }
            
            if ( DEBUG ) System.out.println( "\t" + sock );

            byte postData[] = null;
            
            if ( post != null )
                postData = post.getBytes();
            
            if ( postData != null )
                setRequestHeader( "Content-Length" , String.valueOf( postData.length ) );
            
            sock.getOutputStream().write( getRequestHeader().getBytes() );
            if ( postData != null )
                sock.getOutputStream().write( postData );
            
            ByteBuffer buf = ByteBuffer.wrap( new byte[1024 * 1024] );
            {
                byte temp[] = new byte[2048];
                InputStream in = sock.getInputStream();
                while ( true ){
                    int len = in.read( temp );
                    if ( len < 0 )
                        break;
                    
                    buf.put( temp , 0 , len );
                }
                
            }
            
            buf.flip();
            
            int headerEnd = startsWithHeader( buf );
            if ( headerEnd < 0 ){
                if ( DEBUG ){
                    byte b[] = new byte[buf.limit()];
                    buf.get( b );
                    System.out.println( new String( b ) );
                }
                throw new JSException( "no header :(" );
            }
            
            if ( DEBUG ) System.out.println( buf );
            
            byte headerBytes[] = new byte[headerEnd];
            buf.get( headerBytes );
            String header = new String( headerBytes );
            set( "header" , header.trim() );
            
            String headerLines[] = header.split( "[\r\n]+" );
            if ( headerLines.length > 0 ) {
                String firstline = headerLines[0];
                int start = firstline.indexOf( " " ) + 1;
                int end = firstline.indexOf( " " , start );
                
                set( "status" , Integer.parseInt( firstline.substring( start , end ) ) );
                set( "statusText" , firstline.substring( end + 1 ).trim() );
            }
            
            JSObject headers = new JSObjectBase();
            set( "headers" , headers );
            
            for ( int i=1; i<headerLines.length; i++ ){
                int idx = headerLines[i].indexOf( ":" );
                if ( idx < 0 )
                    continue;
                
                String n = headerLines[i].substring( 0 , idx ).trim();
                String v = headerLines[i].substring( idx + 1 ).trim();
                
                if ( DEBUG ) System.out.println( "\t got header [" + n + "] -> [" + v + "]" );
                headers.set( n , v );
                
            }
            
            setStatus( HEADERS_RECEIVED );
            setStatus( LOADING );
            
            byte bodyBytes[] = new byte[buf.limit()-headerEnd];
            buf.get( bodyBytes );
            String body = new String( bodyBytes );
            set( "responseText" , new JSString( body ) );
            
            if ( DEBUG ) System.out.println( buf );
            
        }
        finally {
	    try {
		sock.close();
	    }
	    catch ( Throwable t ){}
            setStatus( DONE );        
        }

        return this;
    }

    int startsWithHeader( ByteBuffer buf ){
        boolean lastNewLine = false;

        int max = 
            buf.limit() == buf.capacity() ? 
            buf.position() : 
            buf.limit();
        
        for ( int i=0; i < max; i++ ){
            char c = (char)(buf.get(i));

            if ( c == '\r' )
                continue;

            if ( c == '\n' ){
                if ( lastNewLine )
                    return i + 1;
                lastNewLine = true;
                continue;
            }

            lastNewLine = false;
        }
        return -1;
    }
    
    public String toString(){
        return _method + " " + _urlString;
    }
    
    public String getRequestHeader(){
        StringBuffer buf = new StringBuffer();
        
        buf.append( _method ).append( " " ).append( getLocalURL() ).append( " " ).append( "HTTP/1.0\r\n" );

        buf.append( "Host: " ).append( getHost() );
        if ( _url.getPort() > 0 )
            buf.append( ":" ).append( getPort() );
        buf.append( "\r\n" );
        
        buf.append( "Connection: Close\r\n"  );

	for( String x : _extraHeaders ) 
	    buf.append(x);

        buf.append( "\r\n" );
        
        return buf.toString();
    }

    List<String> _extraHeaders = new ArrayList<String>();

    public void setRequestHeader(String label, String value) { 
	_extraHeaders.add(label + ": " + value + "\r\n");
    }

    public String getLocalURL(){
        _checkURL();

        String path = _url.getPath();
        if ( path.length() == 0 )
            path = "/";

        if ( _url.getQuery() == null )
            return path;
        return path + "?" + _url.getQuery();
    }

    public String getHost(){
        _checkURL();
        return _url.getHost();
    }

    public int getPort(){
        _checkURL();
        int port = _url.getPort();
        if ( port > 0 )
            return port;
        
        return isSecure() ? 443 : 80;
    }

    
    public boolean isSecure(){
        return _urlString.startsWith( "https:/" );
    }

    public Object getJSON(){
        return JSON.parse( get( "responseText" ).toString() );
    }

    private void _checkURL(){
        if ( _urlString == null || _urlString.trim().length() == 0 )
            throw new JSException( "no url" );

        if ( _url == null ){
            try {
                _url = new URL( _urlString );
            }
            catch ( MalformedURLException e ){
                throw new JSException( "bad url [" + _urlString + "]" );
            }
        }
    }


    String _method;
    String _urlString;
    boolean _async;

    URL _url;
    Scope _onreadystatechangeScope;
}
