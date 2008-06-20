// Session.java

package ed.appserver;

import java.util.*;

import com.twmacinta.util.*;

import ed.db.*;
import ed.js.*;

public class Session extends JSObjectBase {

    static final String DB_COLLECTION_NAME = "_sessions";
    static final String COOKIE_NAME = "__TG_SESSION_ID__";

    static Session get( String s , DBBase db ){
        if ( s == null )
            return new Session();
        
        Session session = new Session( s );

        JSObjectBase ref = new JSObjectBase();
        ref.set( "_key" , s );
        
        final DBCollection coll = db.getCollection( DB_COLLECTION_NAME );
        coll.ensureIndex( ref );

        Iterator<JSObject> cursor = coll.find( ref , null , 0 , 1 );
        if ( cursor != null && cursor.hasNext() )
            session._copy( cursor.next() );
        
        return session;
    }

    Session(){
        this( _genKey() );
    }
    
    Session( String key ){
        _key = key;
        super.set( "_key" , key ); // calling super b/c this dosn't count as dirtying
    }

    String getCookie(){
        return _key;
    }

    private void _copy( JSObject o ){
        if ( o == null )
            return;

        for ( String s : o.keySet() ){
            
            if ( ByteEncoder.dbOnlyField( s ) )
                continue;

            // calling super b/c this dosn't count as dirtying
            super.set( s , o.get( s ) );
        }

    }

    public Object set( Object n , Object v ){
        if ( n.toString().equals( "_key" ) )
            throw new RuntimeException( "can't set _key on a session" );
        _dirty = true;
        return super.set( n , v );
    }

    boolean sync( DBBase db ){
        if ( ! _dirty )
            return false;

        super.set( "_lastModified" , new JSDate() );
        
        DBCollection c = db.getCollection( DB_COLLECTION_NAME );
        c.save( this );
        return true;
    }

    static String _genKey(){
        final StringBuilder buf = new StringBuilder( "as123snc!@$!as" );
        for ( int i=0; i<10; i++ )
            buf.append( Math.random() );
        buf.append( System.currentTimeMillis() );
        
        final String s = buf.toString();
        synchronized( _myMd5 ){
            _myMd5.Init();
            _myMd5.Update( s );
            return _myMd5.asHex();
        }
    }
    
    private final static MD5 _myMd5 = new MD5();
    
    final String _key;
    private boolean _dirty = false;
}
