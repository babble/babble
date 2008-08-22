// CloudTest.java


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

package ed.cloud;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.testng.annotations.Test;

import ed.js.engine.*;

public class CloudTest extends ed.TestCase {

    static {
        System.setProperty( "FORCE-GRID" , "true" );
    }

    public void testJS()
        throws IOException {
        
        Cloud c = Cloud.getInstance();

        File dir = new File( "src/test/ed/cloud/" );
        if ( ! ( dir.exists() && dir.isDirectory() ) )
            throw new RuntimeException( "can't find cloud test dir" );

        
	List<File> toLoad = new ArrayList<File>();
	for ( File f : dir.listFiles() ){
	    if ( ! f.getName().matches( "\\w+\\.js" ) )
		continue;
	    toLoad.add( f );
	}

	final Matcher numPattern = Pattern.compile( "(\\d+)\\.js$" ).matcher( "" );
	Collections.sort( toLoad , 
                          new Comparator<File>(){
                              public int compare( File aFile , File bFile ){
                                  int a = Integer.MAX_VALUE;
                                  int b = Integer.MAX_VALUE;
                                  
                                  numPattern.reset( aFile.getName() );
                                  if ( numPattern.find() )
                                      a = Integer.parseInt( numPattern.group(1) );
                                  
                                  numPattern.reset( bFile.getName() );
                                  if ( numPattern.find() )
                                      b = Integer.parseInt( numPattern.group(1) );
                                  
                                  return a - b;
			      }
                              
			      public boolean equals( Object o ){
				  return o == this;
			      }
			  } );
        
	for ( File f : toLoad ){
            System.out.println( f );
            c.getScope().eval( f );
	}

    }

    public static void main( String args[] ){
        (new CloudTest()).runConsole();
    }
    
}
