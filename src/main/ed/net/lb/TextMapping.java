// TextMapping.java

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

package ed.net.lb;

import java.io.*;
import java.net.*;
import java.util.*;

import ed.js.*;
import ed.io.*;
import ed.db.*;
import ed.log.*;
import ed.net.*;
import ed.net.httpserver.*;
import ed.util.*;
import ed.cloud.*;


public class TextMapping extends MappingBase {

    public static class Factory implements MappingFactory {
        
        Factory( String f )
            throws IOException {
            this( new File( f ) );
        }

        Factory( File f )
            throws IOException {
            _file = f;
            if ( ! _file.exists() )
                throw new IllegalArgumentException( "doesn't exist [" + f + "]" );

            if ( _file.isDirectory() )
                throw new IllegalArgumentException( "isn't a normal file [" + f + "]" );

            _mapping = new TextMapping( _file );
        }
        
        public Mapping getMapping(){
            return _mapping;
        }

        public long refreshRate(){
            return Long.MAX_VALUE;
        }

        final File _file;
        final TextMapping _mapping;
    }

    public TextMapping( File f )
        throws IOException {
        this( new LineReader( f ) );
    }

    TextMapping( LineReader in )
        throws IOException {
        super( "TextMapping" );
        
        TextSimpleConfig config = TextSimpleConfig.read( in );

        for ( String site : config.getNames( "site" ) )
            for ( Map.Entry<String,String> entry : config.getMap( "site" , site ).entrySet() )
                addSiteMapping( site , entry.getKey() , entry.getValue() );

        for ( String site : config.getNames( "site-alias" ) )
            for ( Map.Entry<String,String> entry : config.getMap( "site-alias" , site ).entrySet() )
                addSiteAlias( site , entry.getKey() , entry.getValue() );        
        
        for ( String pool : config.getNames( "pool" ) )
            for ( String node : config.getValues( "pool" , pool ) )
                addAddressToPool( pool , node );
        
        for ( String ip : config.getValues( "block" , "ip" ) )
            blockIp( ip );

        for ( String url : config.getValues( "block" , "url" ) )
            blockUrl( url );
        
        for ( String def : config.getValues( "default" , "pool" ) )
            setDefaultPool( def );

    }

    static Pair<String,String> parseNameValue( String line ){
        line = line.trim();
        
        int idx = line.indexOf( ":" );
        if ( idx < 0 ){
            idx = line.indexOf( " " );
            if ( idx < 0 )
                throw new RuntimeException( "illegel site line [" + line  + "] has to be <env> : <pool>" );
        }        
        
        return new Pair<String,String>( line.substring( 0 , idx ).trim() , 
                                        line.substring( idx + 1 ).trim() );
    }

    public static void main( String args[] )
        throws IOException {
        System.out.println( (new Factory( args[0] )).getMapping() );
    }
}
