// HelloMongo.java

import java.util.Iterator;

import ed.db.Mongo;
import ed.db.DBCollection;

import ed.js.JSObject;
import ed.js.JSObjectBase;

public class HelloMongo {
    public static void main( String args[] )
        throws java.net.UnknownHostException {
        
        Mongo m = new Mongo( "localhost" , "test" );
        DBCollection c = m.getCollection( "things" );
        
        JSObject o = new JSObjectBase();
        o.set( "name" , "eliot" );
        
        c.save( o );
        
        System.out.println( c.findOne().get( "name" ) );

        Iterator<JSObject> i = c.find();
        while ( i.hasNext() ){
            JSObject found = i.next();
            System.out.println( found.get( "name" ) );
        }

    }
}
