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
import ed.cloud.*;
import static ed.appserver.AppContextHolder.*;

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

        final File _file;
        final TextMapping _mapping;
    }

    TextMapping( File f )
        throws IOException {
        super( "TextMapping" );
        _file = f;
        
        String current = null;
        boolean site = false;
            

        for ( String line : new LineReader( _file ) ){
            if ( line.length() == 0 )
                continue;

            if ( line.startsWith( "site " ) ){
                site = true;
                current = line.substring( 5 ).trim();
                continue;
            }
            
            if ( line.startsWith( "pool " ) ){
                site = false;
                current = line.substring( 5 ).trim();
                continue;
            }
            
            if ( ! Character.isWhitespace( line.charAt(0) ) )
                throw new RuntimeException( "invalid starting line [" + line + "]" );
            
            if ( site ){
                int idx = line.indexOf( ":" );
                if ( idx < 0 )
                    throw new RuntimeException( "illegel site line [" + line  + "] has to be <env> : <pool>" );
                addSiteMapping( current , line.substring( 0 , idx ).trim() , line.substring( idx + 1 ).trim() );
                continue;
            }
            
            addAddressToPool( current , line.trim() );
        }
    }

    final File _file;

    public static void main( String args[] )
        throws IOException {
        System.out.println( (new Factory( args[0] )).getMapping() );
    }
}
