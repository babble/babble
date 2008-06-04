// Mysql.java

package ed.db.migrate;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;

public class Mysql {

    static {
	try {
	    Class.forName( "com.mysql.jdbc.Driver");
	}
	catch ( ClassNotFoundException e ){
	    throw new RuntimeException( e );
	}
    }

    static Connection _createConnection( final String host , final String database , final String username , final String password )
        throws SQLException {
	
        final String url;
        if ( host.startsWith( "jdbc:" ) ){
            url = host;
        }
        else {
            if ( host == null || database == null )
                throw host == null ? new NullPointerException("passed null host ") : new NullPointerException("passed null database ");
             url = "jdbc:mysql://" + host + "/" + database;
        }
        
	Properties props = new Properties( _defaultProperties );

	props.put( com.mysql.jdbc.NonRegisteringDriver.USER_PROPERTY_KEY , username );
	props.put( com.mysql.jdbc.NonRegisteringDriver.PASSWORD_PROPERTY_KEY , password );
	
	// if this is thread safe can just create one
	com.mysql.jdbc.NonRegisteringDriver driver = new com.mysql.jdbc.NonRegisteringDriver();

	return driver.connect( url , props );
    }

    private static final Properties _defaultProperties = new Properties();

    static {
        // character encoding
        _defaultProperties.put( "useUnicode" , "true" );
        _defaultProperties.put( "characterEncoding" , "utf8" );

        // this allows to have 0000-00-00 in the DB
        _defaultProperties.put( "zeroDateTimeBehavior" , "convertToNull" );

        // connection settings
        _defaultProperties.put( "connectTimeout" , " 6000" );
    }
}
