// HttpDownload.java

package ed.net;

import java.io.*;
import java.net.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class HttpDownload {

    public static class downloadFunc extends JSFunctionCalls1 {
            public Object call( Scope s , Object foo , Object extra[] ){
                try {
                    return downloadToJS( new URL( foo.toString() ) );
                }
                catch ( Exception e ){
                    throw new RuntimeException( e );
                }
            }
        };

    public static JSNewFile downloadToJS( URL url )
        throws IOException {
        
        URLConnection conn = url.openConnection();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtil.pipe( conn.getInputStream() , out );

        final byte data[] = out.toByteArray();

        return new JSNewFile( url.getFile() , conn.getContentType() , data.length ){
            protected JSFileChunk newChunk( int num ){

                final int start = num * getChunkSize();
                final int end = Math.min( start + getChunkSize() , data.length );

                JSFileChunk chunk = new JSFileChunk( (JSFile)this , num );
                chunk.setData( new JSBinaryData.ByteArray( data , start , end - start ) );
                return chunk;
            }
        };
    }

    public static void main( String args[] )
        throws Exception {
        JSNewFile foo = downloadToJS( new URL( "http://shopwiki.com/images/logo.gif" ) );
        System.out.println( foo );
    }

}
