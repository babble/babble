// HelloMongo2.java

import java.util.Iterator;

import ed.db.Mongo;
import ed.db.DBCollection;

import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.js.JavaJSObject;

public class HelloMongo2 {
    
    public static class Person extends JavaJSObject {
        
        public Person(){

        }

        Person( String name ){
            _name = name;
        }

        public String getName(){
            return _name;
        }

        public void setName(String name){
            _name = name;
        }

        String _name;
    }

    public static void main( String args[] )
        throws java.net.UnknownHostException {
        
        Mongo m = new Mongo( "localhost" , "test" );
        DBCollection c = m.getCollection( "things" );
        c.remove( new JSObjectBase() );
        c.setConstructor( Person.getWrapper( Person.class ).getConstructor() );

        Person p = new Person( "eliot" );
        c.save( p );
        
        System.out.println( c.findOne().get( "Name" ) );
        Person out = (Person)c.findOne();
        System.out.println( out.getName() );


    }
}
