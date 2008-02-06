// XMLHttpRequest.java

package ed.js;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import ed.js.func.*;
import ed.js.engine.*;

public class XMLHttpRequest extends JSObjectBase {
    
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
        
        _method = method;
        _urlString = url;
        _async = async;
        
        set( "method" , _method );
        set( "url" , _urlString );
        set( "async" , _async );

        if ( _async )
            throw new JSException( "async not done yet" );
    }

    public XMLHttpRequest send()
        throws IOException {
        return send( null );
    }
    
    public XMLHttpRequest send( String post )
        throws IOException {
        
        // TODO: rewrite this for real

        SocketChannel sock = SocketChannel.open();
        sock.connect( new InetSocketAddress( getHost() , getPort() ) );
        
        byte postData[] = null;

        if ( post != null )
            postData = post.getBytes();
        
        if ( postData != null )
            setRequestHeader( "Content-Length" , String.valueOf( postData.length ) );

        ByteBuffer toSend[] = new ByteBuffer[ postData == null ? 1 : 2 ];
        toSend[0] = ByteBuffer.wrap( getRequestHeader().getBytes() );
        if ( postData != null )
            toSend[1] = ByteBuffer.wrap( postData );
        sock.write( toSend );

        ByteBuffer buf = ByteBuffer.allocateDirect( 1024 * 1024 );
        while( sock.read( buf ) >= 0 );
        
        buf.flip();
        
        int headerEnd = startsWithHeader( buf );
        if ( headerEnd < 0 )
            throw new JSException( "no header :(" );
        
        System.out.println( buf );

        byte headerBytes[] = new byte[headerEnd];
        buf.get( headerBytes );
        String header = new String( headerBytes );
        set( "header" , header.trim() );

        System.out.println( buf );

        byte bodyBytes[] = new byte[buf.limit()-headerEnd];
        buf.get( bodyBytes );
        String body = new String( bodyBytes );
        set( "responseText" , new JSString( body ) );

        System.out.println( buf );

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
        if ( _url.getQuery() == null )
            return _url.getPath();
        return _url.getPath() + "?" + _url.getQuery();
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

        if ( _urlString.startsWith( "http:/" ) )
            return 80;

        if ( _urlString.startsWith( "https:/" ) )
            return 443;

        return 80;
    }

    private void _checkURL(){
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
}
