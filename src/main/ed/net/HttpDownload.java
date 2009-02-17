// HttpDownload.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

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
