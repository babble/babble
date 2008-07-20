// Words.java

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

package ed.util;

import java.io.*;
import java.util.*;

/**
 * this is a overly simplistic start, but solves my needs right now.
 * @expose
 */
public class Words {

    /** Returns the words of a given language and country.  Only returns US words, for now.
     * @return US words
     */
    public static Words getWords( String language , String country ){
        // TODO: fix
        return _us;
    }

    /** @unepose */
    static final Words _us = new Words( "en" , "us" );

    private Words( String language , String country ){
        _localeString = ( language + "-" + country ).toLowerCase();
        if ( ! _localeString.equals( "en-us" ) )
            throw new RuntimeException( "only support en-us right now" );

        File f = new File( "/usr/share/dict/words" );

        if ( ! f.exists() )
            throw new RuntimeException( "can't find english dictionary" );

        Set<String> set = new TreeSet<String>();

        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );
            String line;
            while ( ( line = in.readLine() ) != null ){
                set.add( line );
            }
        }
        catch ( IOException ioe ){
            throw new RuntimeException("can't load : " + f , ioe );
        }

        _words = new ArrayList<String>( set );
    }

    /** Returns a random word from this set of words.
     * @return A random word
     */
    public String getRandomWord(){
        int num = (int)(Math.random() * _words.size());
        return _words.get( num );
    }

    /** Checks if a given string is a word in this set of words.
     * @param s String for which to search
     */
    public boolean isWord( String s ){
        return Collections.binarySearch( _words , s ) >= 0;
    }

    /** @unexpose */
    final String _localeString;
    /** @unexpose */
    final List<String> _words;

    /** Returns the locale of this set of words.
     * @return the locale
     */
    public String toString(){
        return "Words : " + _localeString;
    }
}
