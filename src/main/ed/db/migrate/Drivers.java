// Drivers.java

package ed.db.migrate;

import java.sql.*;
import java.util.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.util.*;

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

        s.put( "jdbc" , new JSFunctionCalls3(){

                public Object call( Scope s , Object nameObject , Object userObject , Object passObject , Object[] extra ){
                    String url = "jdbc:" + nameObject.toString();
                    String user = userObject == null ? "" : userObject.toString();
                    String pass = passObject == null ? "" : passObject.toString();

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
	
	public MyPreparedStatement prepareStatement( String sql )
	    throws SQLException {
	    return new MyPreparedStatement( _conn.prepareStatement( sql ) );
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
		    if ( _stmt != null )
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
		    if ( _stmt != null )
			_stmts.add( _stmt );
                _addedBack = true;
            }

            public Set<String> keySet( boolean includePrototype ){
                return _fields;
            }

            private final Statement _stmt;
            private final ResultSet _res;
            private final Set<String> _fields = new OrderedSet<String>();

            private boolean _addedBack = false;
            private boolean _doneAnything = false;
        }

	class MyPreparedStatement extends JSObjectLame {
	    MyPreparedStatement( PreparedStatement ps ){
		_ps = ps;
	    }

	    public Object get( Object key ){
		return null;
	    }

	    public Object set( Object key , Object v ){
		if ( key instanceof Number )
		    return setInt( ((Number)key).intValue() , v );
		throw new RuntimeException( "can't set things on a prepared statement except for numbers" );
	    }

	    public Object setInt( int i, Object v ){
		v = NativeBridge.toJavaObject( v );

		try {
		    _ps.setObject( i , v );
		    return v;
		}
		catch ( SQLException se ){
		    throw new RuntimeException( "can't set something.  num:" + i + " value:" + ( v == null ? null : v.getClass() ) , se );
		}
	    }
	    
	    public int exec()
		throws SQLException {
		_checkClose();
		return _ps.executeUpdate();
	    }
	    
	    public MyResult query()
		throws SQLException {
		_checkClose();
		return new MyResult( null , _ps.executeQuery() );
	    }

	    public void close()
		throws SQLException {
		if ( _closed )
		    return;
		_closed = true;
		_ps.close();
	    }

	    void _checkClose(){
		if ( _closed )
		    throw new RuntimeException( "already closed" );
	    }

	    final PreparedStatement _ps;
	    private boolean _closed = false;
	}
    }
}
