// Words.java

package ed.util;

import java.io.*;
import java.util.*;

/**
 * this is a overly simplistic start, but solves my needs right now.
 */
public class Words {

    public static Words getWords( String language , String country ){
        // TODO: fix
        return _us;
    }
    
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
    
    public String getRandomWord(){
        int num = (int)(Math.random() * _words.size());
        return _words.get( num );
    }

    public boolean isWord( String s ){
        return Collections.binarySearch( _words , s ) >= 0;
    }
    
    final String _localeString;
    final List<String> _words;
    
    public String toString(){
        return "Words : " + _localeString;
    }
}

