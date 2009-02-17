// JSDBFile.java

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
