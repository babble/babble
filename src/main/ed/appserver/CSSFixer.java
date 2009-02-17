// CSSFixer.java

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

    /**
     * Remove quotes surrounding a url in a CSS url() declaration.
     *
     * @see <a href="http://www.w3.org/TR/CSS2/syndata.html#uri">CSS Spec</a>
     */
    private String removeSurroundingQuotes(String url) {
        url = url.trim();
        if (url.startsWith("'")) {
            if (url.endsWith("'")) {
                url = url.substring(1, url.length() - 1);
            }
        } else if (url.startsWith("\"")) {
            if (url.endsWith("\"")) {
                url = url.substring(1, url.length() - 1);
            }
        }
        return url;
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
            url = removeSurroundingQuotes(url);
            _fixer.fix( url , out );

            line = line.substring( end );
        }

    }

    final URLFixer _fixer;
}
