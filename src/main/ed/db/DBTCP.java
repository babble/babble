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
        _portPool = DBPortPool.get( ip );
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
        DBPort port = _portPool.get();
                
        try {
            port.say( new DBMessage( op , buf ) );
            _portPool.done( port );
        }
        catch ( IOException ioe ){
            throw new JSException( "can't say something" );
        }
    }
    
    private int call( int op , ByteBuffer out , ByteBuffer in ){
        DBPort port = _portPool.get();
        
        try {
            DBMessage a = new DBMessage( op , out );
            DBMessage b = port.call( a , in );
            _portPool.done( port );
            return b.dataLen();
        }
        catch ( IOException ioe ){
            throw new JSException( "can't call something" );
        }
    }

    private final DBPortPool _portPool;
}
