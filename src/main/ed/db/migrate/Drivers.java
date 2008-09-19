// Drivers.java

package ed.db.migrate;

import java.sql.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

/**
 * @anonymous name : {jdbc}, desc : {Attempts to connect to a mySQL database using Java's database connector.}, param : {type : (string), name : (url), desc : (url of database to connect to)}, param : {type: (string) isOptional : (true), name : (username), desc : (username with which to connect to the database)},  param : {type: (string) isOptional : (true), name : (password), desc : (password with which to connect to the database)}, return : { type : (jdbcConnection), desc : (a conneection to the mySQL database specified)}
 * @expose
 * @docmodule system.database.drivers
 */
public class Drivers {

    /** @unexpose */
    public Drivers() {
    }
    
    /** @unexpose */    
    public static void init( Scope s ){

        s.put( "jdbc" , new JSFunctionCalls1(){

                public Object call( Scope s , Object nameObject , Object[] extra ){
                    String url = "jdbc:" + nameObject.toString();
                    String user = extra != null && extra.length > 0 ? extra[0].toString() : null;
                    String pass = extra != null && extra.length > 1 ? extra[1].toString() : null;

                    try {
                        if ( nameObject.toString().startsWith( "mysql" ) ){
                            return new JDBCConnection( url , Mysql._createConnection( url , null , user , pass ) );
                        }
                        throw new RuntimeException( "don't know how to connect to [" + nameObject + "]" );
                    }
                    catch ( SQLException se ){
                        throw new RuntimeException( "can't connect to [" + nameObject + "]" , se );
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

        public int exec( String s )
            throws SQLException {
            Statement stmt = null;
            if ( _stmts.size() > 0 )
                stmt = _stmts.remove(0);
            else
                stmt = _conn.createStatement();
            return stmt.executeUpdate( s );
        }

        public String toString(){
            return _url;
        }

        private final Connection _conn;
        private final String _url;
        private List<Statement> _stmts = new LinkedList<Statement>();

        class MyResult extends JSObjectBase {

            MyResult( Statement stmt , ResultSet res )
                throws SQLException {
                _stmt = stmt;
                _res = res;

                ResultSetMetaData rsmd = res.getMetaData();
                for ( int i=1; i<=rsmd.getColumnCount(); i++ )
                    _fields.add( rsmd.getColumnName( i ) );
            }

            public boolean hasNext()
                throws SQLException {

                _doneAnything = true;

                boolean b = _res.next();
                if ( ! b ){
                    _res.close();
                    _addedBack = true;
                    _stmts.add( _stmt );
                }

                return b;
            }

            public Object get( Object o ){
                String name = o.toString();

                if ( name.equals( "hasNext" ) ||
                     name.equals( "asObject" ) ||
                     name.equals( "toObject" ) ||
                     name.equals( "asArray" ) ||
                     name.equals( "toArray" ) ||
                     name.equals( "toString" ) ||
                     name.equals( "keySet" ) )
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

            public JSObject toObject(){
                return asObject();
            }

            public JSObject asObject(){
                JSObjectBase o = new JSObjectBase();
                for ( String s : _fields )
                    o.set( s , get( s ) );
                return o;
            }

            public JSArray toArray()
                throws SQLException {
                return asArray();
            }

            public JSArray asArray()
                throws SQLException {
                if ( _doneAnything )
                    throw new RuntimeException( "too late to call toArray()" );

                JSArray a = new JSArray();
                while ( hasNext() )
                    a.add( asObject() );
                return a;
            }

            public void close()
                throws SQLException {
                _res.close();
                if ( ! _addedBack )
                    _stmts.add( _stmt );
                _addedBack = true;
            }

            public Collection<String> keySet( boolean includePrototype ){
                return _fields;
            }

            private final Statement _stmt;
            private final ResultSet _res;
            private final List<String> _fields = new ArrayList<String>();

            private boolean _addedBack = false;
            private boolean _doneAnything = false;
        }
    }
}
