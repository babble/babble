// FileTests.java

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

package ed.security;

import java.io.*;

import ed.*;
import ed.appserver.*;

public class FileTests extends TestCase {

    public FileTests(){
        _fs = new FileSecurity();
        _context = new AppContext( new File( "src/test/samplewww" ) );
    }

    public void testHome(){
        String root = (new File(".")).getAbsolutePath().replaceAll( "[\\.\\/]+$" , "" );
        assertTrue( _fs.canRead( _context , root ) );
        assertTrue( _fs.canRead( _context , root + "/" ) );
        assertFalse( _fs.canRead( _context , root.replaceAll( "/.*?$" , "" ) ) );
    }
    
    public void testUnixSystem(){
        assertFalse( _fs.canRead( _context , "/etc/" ) );
        assertFalse( _fs.canRead( _context , "/var/" ) );
    }
    
    public void testContext(){
        assertTrue( _fs.canWrite( _context , _context.getRootFile().toString() ) );
        assertTrue( _fs.canWrite( _context , _context.getRootFile().getAbsolutePath() ) );
    }

    final AppContext _context;
    final FileSecurity _fs;

    public static void main( String args[] ){
        if ( ! (new FileTests()).runConsole() )
            throw new RuntimeException( "broken" );
    }
}

