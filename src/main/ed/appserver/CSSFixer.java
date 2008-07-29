// CSSFixer.java

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

package ed.appserver;

import java.io.*;

import ed.appserver.*;

public class CSSFixer {

    public CSSFixer( URLFixer fixer ){
        _fixer = fixer;
    }

    public String fixSingeLine( String line ){
        StringBuilder buf = new StringBuilder();
        try {
            fix( line , buf );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "impossible" );
        }
        return buf.toString();
    }
    
    public void fix( InputStream inRaw , Appendable out )
        throws IOException {
        
        BufferedReader in = new BufferedReader( new InputStreamReader( inRaw ) );
        String line;
        while ( ( line = in.readLine() ) != null ){
            fix( line , out ).append( "\n" );
        }
        
    }
    
    public Appendable fix( String line , Appendable out )
        throws IOException {
        
        if ( line.indexOf( "\n" ) > 0 )
            throw new IllegalArgumentException( "line has to be 1 line" );
        
        while ( true ){
            int idx = line.indexOf( "url(" );
            if ( idx < 0 )
                idx = line.indexOf( "URL(" );
            
            if ( idx < 0 )
                return out.append( line );

            int end = line.indexOf( ")" , idx );
            if ( end < 0 )
                return out.append( line );

            out.append( line.substring( 0 , idx + 4 ) );

            String url = line.substring( idx + 4 , end );
            _fixer.fix( url , out );

            line = line.substring( end );
        }

    }

    final URLFixer _fixer;
}
