// WorkingFiles.java

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

package ed.io;

import java.io.*;

public class WorkingFiles {
    
    public static final String TMP_DIR = "/tmp/jxp/";
    public static final File TMP_FILE = new File( TMP_DIR );
    static {
        TMP_FILE.mkdirs();
    }

    public static File getTypeDir( String type ){
        File f = new File( TMP_FILE , type );
        f.mkdirs();
        return f;
    }

    public static File getTMPFile( String type , String name ){
        name = FileUtil.clean( name );

        while ( name.startsWith( "/" ) )
            name = name.substring(1);
        
        File f = new File( getTypeDir( type ) , name );
        
        if ( name.contains( "/" ) )
            f.getParentFile().mkdirs();

        return f;
    }
    
}
