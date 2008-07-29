// DBMessageLayer.java

package ed.db;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import ed.js.*;
import ed.lang.*;

public abstract class DBMessageLayer extends DBApiLayer {
    
    DBMessageLayer( String root ){
        super( root );
    }

    protected void doInsert( ByteBuffer buf ){
        final long start = System.currentTimeMillis();
        say( 2002 , buf );
        ProfilingTracker.tlGotTime( "db.insert" , System.currentTimeMillis() - start );
    }
    protected  void doDelete( ByteBuffer buf ){
        final long start = System.currentTimeMillis();
        say( 2006 , buf );
        ProfilingTracker.tlGotTime( "db.delete" , System.currentTimeMillis() - start );
    }
    protected void doUpdate( ByteBuffer buf ){
        final long start = System.currentTimeMillis();
        say( 2001 , buf );
        ProfilingTracker.tlGotTime( "db.update" , System.currentTimeMillis() - start );
    }
    protected void doKillCursors( ByteBuffer buf ){
        final long start = System.currentTimeMillis();
        say( 2007 , buf );
        ProfilingTracker.tlGotTime( "db.killCursors" , System.currentTimeMillis() - start );
    }
    
    protected int doQuery( ByteBuffer out , ByteBuffer in ){
        final long start = System.currentTimeMillis();
        final int res = call( 2004 , out , in );
        ProfilingTracker.tlGotTime( "db.query" , System.currentTimeMillis() - start );
        return res;
    }
    protected int doGetMore( ByteBuffer out , ByteBuffer in ){
        final long start = System.currentTimeMillis();
        final int res = call( 2005 , out , in );
        ProfilingTracker.tlGotTime( "db.getMore" , System.currentTimeMillis() - start );
        return res;
    }
    
    protected abstract void say( int op , ByteBuffer buf );
    protected abstract int call( int op , ByteBuffer out , ByteBuffer in );

}
