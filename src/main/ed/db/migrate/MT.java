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

        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery( "SELECT * FROM mt_entry , mt_author WHERE entry_author_id = author_id ORDER BY entry_id DESC LIMIT 10 " );
        
        Statement commentsStmt = conn.createStatement();
        ResultSet comments = commentsStmt.executeQuery( "SELECT * FROM mt_comment WHERE comment_visible = 1 ORDER BY comment_entry_id DESC" ); 
        comments.next();

        boolean moreComments = true;
        
        while ( res.next() ){
            System.out.println( res.getString("entry_title" ) ); 

            JSObject o = new JSObjectBase();
            JSDate d = new JSDate( res.getTimestamp( "entry_authored_on" ).getTime() );

            o.set( "name"  , d.getYear() + "/" + d.getMonth() + "/" + res.getString("entry_basename") );
            o.set( "title"  , res.getString("entry_title") );
            o.set( "content" , res.getString( "entry_text" ) + "\n\n---JUMP---\n\n" + res.getString( "entry_text_more" ) );
            o.set( "author" , res.getString( "author_name" ) );
            o.set( "ts" , d );
            o.set( "live" , res.getInt( "entry_status" ) == 2 );
            
            while ( moreComments && comments.getInt( "comment_entry_id" ) > res.getInt( "entry_id" ) ){
                if ( ! comments.next() )
                    moreComments = false;
            }

            JSArray ca = null;
            
            while ( moreComments && comments.getInt( "comment_entry_id" ) == res.getInt( "entry_id" ) ){
                System.out.println( "\t found a comment" );
                if ( ca == null ){
                    ca = new JSArray();
                    o.set( "comments" , ca );
                }
                
                JSObject c = new JSObjectBase();
                c.set( "author" , comments.getString( "comment_author" ) );
                c.set( "email" , comments.getString( "comment_email" ) );
                c.set( "ip" , comments.getString( "comment_ip" ) );
                c.set( "text" , comments.getString( "comment_text" ) );
                c.set( "ts" , new JSDate( comments.getTimestamp( "comment_created_on" ).getTime() ) );
                ca.add( c );
                
                if ( ! comments.next() )
                    moreComments = false;
            }
            
            //System.out.println( JSON.serialize( o ) );
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
