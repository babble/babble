// HttpExceptions.java

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

public class HttpExceptions {
    
    /**
     * use this to get an error back to the client
     */
    public static class BadRequest extends RuntimeException {
        public BadRequest( int responseCode , String body ){
            super( responseCode + ": " + body );
            _responseCode = responseCode;
            _body = body;
        }

        public int getResponseCode(){
            return _responseCode;
        }

        public String getBodyContent(){
            return toString();
        }

        final int _responseCode;
        final String _body;
        
    }

    public static class ConnectionError extends RuntimeException {
        public ConnectionError( String msg , InetSocketAddress addr , IOException cause ){
            super( "[" + addr + "] : " + msg + " " + cause , cause );
            _addr = addr;
        }
        
        final InetSocketAddress _addr;
    }
    
    public static class CantOpen extends ConnectionError {
        public CantOpen( InetSocketAddress addr , IOException ioe ){
            super( "can't open" , addr , ioe );
            _ioe = ioe;
        }

        public IOException getIOException(){
            return _ioe;
        }

        final IOException _ioe;
    }

    public static class ClientError extends IOException {
        public ClientError( String msg ){
            super( "ClientError : " + msg );
        }
    }

    public static class UnexpectedConnectionClosed extends EOFException {
        public UnexpectedConnectionClosed( int numRequestsBeforeError ){
            super( "UnexpectedConnectionClosed numRequestsBeforeError:" + numRequestsBeforeError );
            _numRequestsBeforeError = numRequestsBeforeError;
        }
        
        public int getNumRequestsBeforeError(){
            return _numRequestsBeforeError;
        }

        final int _numRequestsBeforeError;
    }
}
