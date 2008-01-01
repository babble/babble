// DBTCP.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;


public class DBTCP extends DBApiLayer {

    DBTCP( String root , String ip ){
        super( root );
        
        try {
            _port = new DBPort( ip );
        }
        catch ( IOException ioe ){
            throw new JSException( "can't connect to : " + ip );
        }
    }

    protected void doInsert( ByteBuffer buf ){
        say( 2002 , buf );
    }
    protected  void doDelete( ByteBuffer buf ){
        say( 2006 , buf );
    }
    protected void doUpdate( ByteBuffer buf ){
        say( 2001 , buf );
    }
    
    protected int doQuery( ByteBuffer out , ByteBuffer in ){
        return call( 2004 , out , in );
    }
    protected int doGetMore( ByteBuffer out , ByteBuffer in ){
        return call( 2005 , out , in );
    }


    private void say( int op , ByteBuffer buf ){
        try {
            _port.say( new DBMessage( op , buf ) );
        }
        catch ( IOException ioe ){
            throw new JSException( "can't say something" );
        }
    }
    
    private int call( int op , ByteBuffer out , ByteBuffer in ){
        try {
            DBMessage a = new DBMessage( op , out );
            DBMessage b = _port.call( a , in );
            return b.dataLen();
        }
        catch ( IOException ioe ){
            throw new JSException( "can't call something" );
        }
    }

    private final DBPort _port;
}
