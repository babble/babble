// HttpExceptions.java

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
