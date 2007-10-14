// Parser.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.util.*;

public class Parser {
    
    static List<Block> parse( JxpSource s )
        throws IOException {

        String data = s.getContent();
        
        Block.Type curType = Block.Type.HTML;
        StringBuilder buf = new StringBuilder();
        
        List<Block> blocks = new ArrayList<Block>();

        for ( int i=0; i<data.length(); i++ ){
            char c = data.charAt( i );
            
            if ( curType == Block.Type.HTML ){
                
                if ( c == '<' && 
                     i + 1 < data.length() &&
                     data.charAt( i + 1 ) == '%' ){
                    
                    blocks.add( Block.create( curType , buf.toString() ) );
                    buf.setLength( 0 );
                    
                    curType = Block.Type.CODE;
                    i++;
                    if ( i + 1 < data.length() &&
                         data.charAt( i + 1 ) == '=' ){
                        i++;
                        curType = Block.Type.OUTPUT;
                    }
                    
                    continue;
                }
            }
            
            if ( curType == Block.Type.CODE || 
                 curType == Block.Type.OUTPUT ){
                
                if ( c == '"' ){
                    buf.append( c );
                    i++;
                    for ( ; i<data.length(); i++ ){
                        c = data.charAt( i );
                        buf.append( c );
                        if ( c == '"' )
                            break;
                    }
                    
                    continue;
                }

                if ( c == '%' &&
                     i + 1 < data.length() &&
                     data.charAt( i + 1 ) == '>' ){
                    
                    blocks.add( Block.create( curType , buf.toString() ) );
                    
                    buf.setLength( 0 );
                    i++;
                    curType = Block.Type.HTML;

                    continue;
                }
                     
            }

            buf.append( c );
        }

        blocks.add( Block.create( curType , buf.toString() ) );
        
        return blocks;
    }

    public static void main( String args[] )
        throws Exception {

        System.out.println( parse( JxpSource.getSource( new File( "crap/www/index.jxp" ) ) ) );
        
    }

}
