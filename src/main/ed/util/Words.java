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
        
        try {
            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );
            String line;
            while ( ( line = in.readLine() ) != null ){
                _words.add( line );
            }
        }
        catch ( IOException ioe ){
            throw new RuntimeException("can't load : " + f , ioe );
        }
    }
    
    public String getRandomWord(){
        int num = (int)(Math.random() * _words.size());
        Iterator<String> i = _words.iterator();
        while ( --num > 0 )
            i.next();
        return i.next();
    }

    public boolean isWord( String s ){
        return _words.contains( s );
    }
    
    final String _localeString;
    final Set<String> _words = new HashSet<String>();
}

