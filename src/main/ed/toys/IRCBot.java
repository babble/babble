// IRCBot.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.toys;

import java.io.*;
import java.util.*;

import org.jibble.pircbot.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;

public class IRCBot extends PircBot implements JSObject {
    
    public static IRCBot connect( String host , String nick )
        throws Exception {
        return new IRCBot( host , nick );
    }

    public IRCBot( String host , String nick ) 
        throws IOException , IrcException {
        setName( nick );
        setVerbose( true );
        connect( host );
    }

    protected void onDisconnect(){
        JSFunction onDisconnect = (JSFunction)_things.get( "onDisconnect" );
        if ( onDisconnect == null )
            return;
	
	onDisconnect.call( onDisconnect.getScope() );
    }

    public void onMessage( String channel, String sender,
                           String login, String hostname, String message) {
        
        
        JSFunction onMessage = (JSFunction)_things.get( "onMessage" );
        if ( onMessage == null )
            return;
        
        JSObjectBase o = new JSObjectBase();
        o.set( "channel" , channel );
        o.set( "sender" , sender );
        o.set( "login" , login );
        o.set( "hostname" , hostname );
        o.set( "message" , message );
        
        onMessage.call( onMessage.getScope() , o );
    }
    
    // ----
    // JSObject stuff
    // ----
    
    public Object set( Object n , Object v ){
        _things.put( n.toString() , v );
        return v;
    }
    public Object get( Object n ){
        return _things.get( n.toString() );
    }

    public Object setInt( int n , Object v ){
        throw new RuntimeException( "you are stupid" );
    }
    public Object getInt( int n ){
        throw new RuntimeException( "you are stupid" );
    }
    
    public Object removeField( Object n ){
        return _things.remove( n );
    }

    public boolean containsKey( String s ){
        return _things.containsKey( s );
    }
    
    public Collection<String> keySet(){
        return _things.keySet();
    }

    public JSFunction getConstructor(){
        return null;
    }
    public JSObject getSuper(){
        return null;
    }

    private Map<String,Object> _things = new TreeMap<String,Object>();

}
