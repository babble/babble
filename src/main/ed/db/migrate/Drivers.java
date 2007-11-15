// Drivers.java

package ed.db.migrate;

public class Drivers {

    static {
        try {
	    Class.forName( "com.mysql.jdbc.Driver");
	}
	catch ( ClassNotFoundException e ){
	    throw new RuntimeException( e );
	}
    }
    
    public static void init(){
    }
}
