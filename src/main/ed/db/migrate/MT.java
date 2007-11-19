// MT.java

package ed.db.migrate;

import java.sql.*;

import ed.db.*;
import ed.js.*;

public class MT {
    static { 
        Drivers.init();
    }

    public static void migrate( Connection conn , DBBase db )
        throws Exception {

        DBCollection coll = db.getCollection( "posts" );

        Statement stmt = conn.createStatement( ResultSet.TYPE_FORWARD_ONLY,
                                               ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        ResultSet res = stmt.executeQuery( "SELECT * FROM mt_entry , mt_author WHERE entry_author_id = author_id ORDER BY entry_authored_on DESC " );
        
        while ( res.next() ){
            System.out.println( res.getString("entry_title" ) ); 

            JSObject o = new JSObjectBase();
            JSDate d = new JSDate( res.getTimestamp( "entry_authored_on" ).getTime() );

            o.set( "name"  , d.getYear() + "/" + d.getMonth() + "/" + res.getString("entry_basename") );
            o.set( "title"  , res.getString("entry_title") );
            o.set( "content" , res.getString( "entry_text" ) + "\n\n---JUMP---\n\n" + res.getString( "entry_text_more" ) );
            o.set( "author" , res.getString( "author_name" ) );
            o.set( "ts" , d );
            o.set( "live" , true );

            coll.save( o );
        }
        
        res.close();
        stmt.close();
        
    }

    public static void main( String args[] )
        throws Exception {
        migrate( DriverManager.getConnection( "jdbc:mysql://www.alleyinsider.com/alleyinsider_mt" , "dev" , "dv12" ) ,
                 DBJni.get( "alleyinsider" ) );

    }
}
