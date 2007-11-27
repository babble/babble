// MT.java

package ed.db.migrate;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

import ed.db.*;
import ed.js.*;

public class MT {
    static { 
        Drivers.init();
    }

    public static void migrate( Connection conn , DBBase db )
        throws Exception {

        DBCollection coll = db.getCollection( "blog" ).getCollection( "posts" );

        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery( "SELECT * FROM mt_entry , mt_author WHERE entry_author_id = author_id ORDER BY entry_id DESC LIMIT 10 " );
        
        Statement commentsStmt = conn.createStatement();
        ResultSet comments = commentsStmt.executeQuery( "SELECT * FROM mt_comment WHERE comment_visible = 1 ORDER BY comment_entry_id DESC" ); 
        comments.next();

        PreparedStatement catQuery = conn.prepareStatement( "SELECT category_basename FROM mt_placement , mt_category " + 
                                                            " WHERE placement_category_id = category_id AND placement_entry_id = ? " );

        boolean moreComments = true;
        
        while ( res.next() ){
            System.out.println( res.getString("entry_title" ) ); 

            JSObject search = new JSObjectBase();
            JSObject o = new JSObjectBase();

            JSDate d = new JSDate( res.getTimestamp( "entry_authored_on" ).getTime() );

            o.set( "name"  , d.getYear() + "/" + d.getMonth() + "/" + res.getString("entry_basename") );
            search.set( "name" , o.get( "name" ) );

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
            

            // categories

            catQuery.setInt( 1 , res.getInt( "entry_id" ) );
            ResultSet catRS = catQuery.executeQuery();
            JSArray cats = new JSArray();
            while ( catRS.next() )
                cats.add( catRS.getString(1) );
            catRS.close();
            o.set( "categories" , cats );
            
            // images
            doImages( o.get( "content" ).toString() );

            //System.out.println( JSON.serialize( o ) );
            coll.update( search , o , true );
        }
        
        res.close();
        stmt.close();
        
    }

    static void doImages( String content ){
        Matcher m = Pattern.compile( "<img.*?src=['\"](.*?)['\"]" , Pattern.CASE_INSENSITIVE ).matcher( content );
        while ( m.find() ){
            String url = m.group(1);
            System.out.println( url );
            if (  url.startsWith( "/" ) )
                url = "http://static.alleyinsider.com" + url;
            else
                url = url.replaceAll( "www." , "static." );
            System.out.println( url );
            
        }
    }

    public static void main( String args[] )
        throws Exception {
        migrate( DriverManager.getConnection( "jdbc:mysql://www.alleyinsider.com/alleyinsider_mt" , "dev" , "dv12" ) ,
                 DBJni.get( "alleyinsider" ) );

    }
}
