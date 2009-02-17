// Session.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.appserver;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.twmacinta.util.*;

import ed.db.*;
import ed.js.*;
import ed.util.*;

/** Keeps track of user sessions.  A Session is just a JavaScript
 * object, and can be treated as such.  It keeps track of whether it's
 * "dirty" and serializes to the database when necessary.  Be careful
 * with embedded objects; setting fields of an embedded object does
 * not mark a Session as dirty.
 * A session is provided in the application context in a variable called
 * "session". If you use it, it will automatically send a cookie to the user
 * to track sessions.
 * @example session.pageviews = session.pageviews + 1;
 * @expose
 * @docmodule system.HTTP.session
 */
public class Session extends JSObjectBase implements HttpSession {

    /** Database collection to track sessions: "_sessions" */
    static final String DB_COLLECTION_NAME = "_sessions";
    /** Session cookie name: "__TG_SESSION_ID__" */
    public static final String COOKIE_NAME = "__TG_SESSION_ID__";
    
    static final int maxKeepAliveSeconds = 60 * 60 * 2;

    /**
     * Returns a session with a given name and database.
     * @param s session name
     * @param db session database
     */
    static Session get( String s , DBBase db ){
        if ( s == null )
            return new Session();

        Session session = new Session( s );

        JSObjectBase ref = new JSObjectBase();
        ref.set( "_key" , s );

        final DBCollection coll = db.getCollection( DB_COLLECTION_NAME );
        coll.ensureIndex( _indexToAdd );

        Iterator<JSObject> cursor = coll.find( ref , null , 0 , 1 );
        if ( cursor != null && cursor.hasNext() )
            session._copy( cursor.next() );
        return session;
    }

    /**
     * Initializes a new session with a randomly generated name.
     */
    Session(){
        this( _genKey() );
        _isNew = true;
        super.set( "_created" , new JSDate() );
    }

    /**
     * Initializes a new session with a given name.
     */
    Session( String key ){
        _key = key;
        _isNew = false;
        super.set( "_key" , key ); // calling super b/c this dosn't count as dirtying
        super.set( "_created" , new JSDate() );
    }

    /** Returns this session's name.
     * @return the name
     */
    String getCookie(){
        return _key;
    }

    private void _copy( JSObject o ){
        if ( o == null )
            return;

        for ( String s : o.keySet() ){

            if ( ByteEncoder.dbOnlyField( s ) )
                continue;

            // calling super b/c this dosn't count as dirtying
            super.set( s , o.get( s ) );
        }

    }

    public Object getAttribute( String name ){
        return get( name );
    }

    public Object getValue( String name ){
        return get( name );
    }

    public void removeAttribute( String name){
        removeField( name );
    }

    public void removeValue( String name){
        removeField( name );
    }

    public Enumeration getAttributeNames(){
        return new CollectionEnumeration( keySet() );
    }

    public String[] getValueNames(){
        Collection<String> keys = keySet();
        return keySet().toArray( new String[keys.size()] );
    }

    public void putValue( String name , Object value ){
        set( name , value );
    }

    public void setAttribute( String name , Object value ){
        set( name , value );
    }

    /**
     * Handler for setting an attribute on a Session.
     * Marks this session as dirty.
     * @jsset
     */
    public Object set( Object n , Object v ){
        if ( n.toString().equals( "_key" ) )
            throw new RuntimeException( "can't set _key on a session" );
        _dirty = true;
        return super.set( n , v );
    }

    /** Deletes an attribute from this session.  Marks this session as dirty.
     * @param n the name of the attribute to remove.
     * @return the value of the attribute removed
     */
    public Object removeField( Object n ){
        _dirty = true;
        return super.removeField( n );
    }

    /** If the session is dirty, saves it to its database collection.
     * @param db the database containing this session's collection
     * @return if the session was dirty
     */
    boolean sync( DBBase db ){
        if ( ! _dirty )
            return false;

        super.set( "_lastModified" , new JSDate() );

        DBCollection c = db.getCollection( DB_COLLECTION_NAME );
        c.save( this );
        return true;
    }

    static String _genKey(){
        final StringBuilder buf = new StringBuilder( "as123snc!@$!as" );
        for ( int i=0; i<10; i++ )
            buf.append( Math.random() );
        buf.append( System.currentTimeMillis() );

        final String s = buf.toString();
        synchronized( _myMd5 ){
            _myMd5.Init();
            _myMd5.Update( s );
            return _myMd5.asHex();
        }
    }

    public boolean isNew(){
        return _isNew;
    }

    public String toString(){
        return "Session";
    }

    public void invalidate(){
        throw new RuntimeException( "not done yet" );
    }
    
    public HttpSessionContext getSessionContext(){
        throw new RuntimeException( "no session context" );
    }

    public ServletContext getServletContext(){
        throw new RuntimeException( "no servlet context" );
    }

    public int getMaxInactiveInterval(){
        return maxKeepAliveSeconds;
    }

    public void setMaxInactiveInterval( int seconds ){
        throw new RuntimeException( "can't set max inactive interval" );
    }

    public long	getLastAccessedTime(){
        throw new RuntimeException( "getLastAccessedTime doesn't make sense" );
    }

    public String getId(){
        return _key;
    }

    public JSDate getCreationDate(){
        Object o = get( "_created" );
        if ( o instanceof JSDate )
            return ((JSDate)o);
        return new JSDate();
    }

    public long getCreationTime(){
        return getCreationDate().getTime();
    }

    private final static MD5 _myMd5 = new MD5();
    
    final String _key;
    private boolean _isNew;
    private boolean _dirty = false;

    static final JSObjectBase _indexToAdd = new JSObjectBase();
    static {
        _indexToAdd.set( "_key" , 1 );
        _indexToAdd.lock();
    }
}
