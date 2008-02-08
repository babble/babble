// JSCaptcha.java

package ed.js;

import java.io.*;
import java.nio.*;
import java.util.*;

import nl.captcha.servlet.*;

import ed.net.httpserver.*;

public class JSCaptcha {
    
    public synchronized void img( String s , HttpResponse response )
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        _producer.createImage( out , s );
        byte bb[] = out.toByteArray();
        
        response.setHeader( "Content-Type" , "image/jpeg" );
        response.setData( ByteBuffer.wrap( bb ) );
    }

    final CaptchaProducer _producer = new DefaultCaptchaIml( new Properties() );
}
