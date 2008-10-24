// JSDBFile.java

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

package ed.js;

import java.io.*;

import ed.db.*;
import ed.js.engine.*;

public class JSDBFile extends JSFile {

    public JSDBFile( DBBase base ){
        _base = base;
    }

    public JSFileChunk getChunk( int num ){
        throw new RuntimeException( "not done yet" );
    }

    public void remove(){
        if ( _base == null )
            throw new NullPointerException( "no base to delete file" );
        
        _base.evalPrototypeFunction( "eval" , _removeFunc , get( "_id" ) );
    }
    
    public void debug(){
        
        System.out.println( "--- START" );
        System.out.println( toString() );
        System.out.println( "-" );
        for ( String n : keySet() ){
            System.out.println( "\t " + n + " : " + get( n ) );
        }
        System.out.println( "-----" );
        try {
            write( System.out );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( ioe );
        }
        System.out.println( "--- END" );

    }

    final DBBase _base;

    static final JSFunction _removeFunc =  
        Convert.makeAnon( "function(z){ " + 
                          "   var f = db._files.findOne( z ); " + 
                          "   if ( ! f ){ return; }; " + 
                          "   var next = f.next; " + 
                          "   db._files.remove( { _id : f._id } ); " + 
                          "   while ( next ){ " + 
                          "      var temp = next.next; " + 
                          "      db._chunks.remove( { _id : next._id } ); " + 
                          "      next = temp; " + 
                          "   } " + 
                          "}"
                          );

}
