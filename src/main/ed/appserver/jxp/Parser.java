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
        int lastline = 1;
        int line = 1;
        
        final boolean isTemplate = s.getName().endsWith( ".html" );

        Block.Type curType = s.getName().endsWith( ".jxp" ) || s.getName().endsWith( ".html" ) ? Block.Type.HTML : Block.Type.CODE;
        StringBuilder buf = new StringBuilder();
        
        boolean newLine = true;
        char lastChar = '\n';
        char codeOpening = ' ';
        int numBrackets = 0;
        
        List<Block> blocks = new ArrayList<Block>();

        if ( isTemplate )
            blocks.add( Block.create( Block.Type.CODE , "var obj = arguments[0];\n" , -1 ) );
        
        for ( int i=0; i<data.length(); i++ ){
            lastChar =  i == 0 ? '\n' : data.charAt( i - 1 );
            char c = data.charAt( i );
            if ( c == '\n' )
                line++;
            newLine = lastChar == '\n';
            
            if ( curType == Block.Type.WIKI && data.startsWith( "</wiki>" , i ) ){
                blocks.add( Block.create( curType , buf.toString() , lastline ) );
                buf.setLength( 0 );
                lastline = line;
                curType = Block.Type.HTML;
                i += 6;
                continue;
            }
            
            if ( curType == Block.Type.HTML ){
                
                if ( data.startsWith( "<wiki>" , i ) ){
                    blocks.add( Block.create( curType , buf.toString() , lastline ) );
                    buf.setLength( 0 );
                    lastline = line;
                    curType = Block.Type.WIKI;
                    i += 5;
                    continue;
                }
                
                if ( isTemplate && c == '$' ){
                    blocks.add( Block.create( curType , buf.toString() , lastline ) );
                    buf.setLength( 0 );
                    i++;
                    int end = i;
                    int parens = 0;
                    for ( ; end < data.length(); end ++ ){
                        char temp = data.charAt( end );
                        if ( temp == '(' ){
                            parens++;
                            continue;
                        }
                        if ( temp == ')' ){
                            parens--;
                            continue;
                        }
                        if ( Character.isWhitespace( temp ) || temp == '<' && parens == 0 )
                            break;
                    }
                    
                    blocks.add( Block.create( Block.Type.OUTPUT , "obj." + data.substring( i , end ) , lastline ) );
                    i = end - 1;
                    curType = Block.Type.HTML;
                    continue;
                }
                
                if ( 
                    ( newLine && c == '{' ) 
                    || 
                    ( c == '<' && 
                      i + 1 < data.length() &&
                      data.charAt( i + 1 ) == '%' ) 
                     ) {
                    
                    codeOpening = c;
                    numBrackets = 0;
                    
                    blocks.add( Block.create( curType , buf.toString() , lastline ) );
                    buf.setLength( 0 );
                    
                    lastline = line;
                    curType = Block.Type.CODE;
                    if ( c == '<' )
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
                
                if ( ( numBrackets == 0 && codeOpening == '{' && c == '}' ) 
                     ||
                     ( codeOpening == '<' &&
                       c == '%' &&
                       i + 1 < data.length() &&
                       data.charAt( i + 1 ) == '>' )
                     ) {
                    
                    blocks.add( Block.create( curType , buf.toString() , lastline ) );

                    lastline = line;
                    buf.setLength( 0 );
                    if ( codeOpening == '<' )
                        i++;
                    curType = Block.Type.HTML;

                    continue;
                }
                
                if ( codeOpening == '{' ){
                    if ( c == '}' && numBrackets > 0 )
                        numBrackets --;
                    if ( c == '{' )
                        numBrackets++;
                }
                
            }

            buf.append( c );
        }

        blocks.add( Block.create( curType , buf.toString() , lastline ) );
        
        return blocks;
    }


    public static void main( String args[] )
        throws Exception {

        System.out.println( parse( JxpSource.getSource( new File( "crap/www/index.jxp" ) ) ) );
        
    }

}
