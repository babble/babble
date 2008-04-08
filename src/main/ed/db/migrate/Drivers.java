// Drivers.java

package ed.db.migrate;

import java.sql.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

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

    public static void init( Scope s ){
        init();
        
        s.put( "jdbc" , new JSFunctionCalls1(){
                public Object call( Scope s , Object nameObject , Object[] extra ){

                    String url = "jdbc:" + nameObject.toString();
                    String user = extra != null && extra.length > 0 ? extra[0].toString() : null;
                    String pass = extra != null && extra.length > 1 ? extra[1].toString() : null;
                    
                    try {
                        Connection conn = DriverManager.getConnection( url , user , pass );
                        return new JDBCConnection( url , conn );
                    }
                    catch ( SQLException se ){
                        throw new RuntimeException( se );
                    }
                }
            } , true );
    }
    
    static class JDBCConnection extends JSObjectBase {

        JDBCConnection( String url , Connection conn )
            throws SQLException {
            _url = url;
            _conn = conn;
        }
        
        public MyResult query( String s )
            throws SQLException {
            Statement stmt = null;
            if ( _stmts.size() > 0 )
                stmt = _stmts.remove(0);
            else
                stmt = _conn.createStatement();
            return new MyResult( stmt , stmt.executeQuery( s ) );
        }
        
        public String toString(){
            return _url;
        }

        private final Connection _conn;
        private final String _url;
        private List<Statement> _stmts = new LinkedList<Statement>();
        
        class MyResult extends JSObjectBase {

            MyResult( Statement stmt , ResultSet res ){
                _stmt = stmt;
                _res = res;
            }

            public boolean hasNext()
                throws SQLException {
                boolean b = _res.next();
                if ( ! b ){
                    _res.close();
                    _stmts.add( _stmt );
                }

                return b;
            }

            public Object get( Object o ){
                String name = o.toString();
                if ( name.equals( "hasNext" ) )
                    return null;

                try {
                    Object foo = _res.getObject( name );
                    if ( foo instanceof String) 
                        foo = new JSString( foo.toString() );
                    else if ( foo instanceof java.util.Date )
                        foo = new JSDate( (java.util.Date)foo );
                    return foo;
                }
                catch ( SQLException se ){
                    throw new RuntimeException( se );
                }
            }

            private Statement _stmt;
            private ResultSet _res;
        }
    }
}
