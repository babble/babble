// Parser.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.io.*;
import ed.util.*;
import ed.appserver.jxp.JxpSource.Language.*;

public class Parser {

    static final boolean DEBUG = false;

    static class MacroEnd {
        MacroEnd( Block b , boolean printTag ){
            _block = b;
            _printTag = printTag;
        }

        final Block _block;
        final boolean _printTag;
    }
    
    static List<Block> parse( JxpSource s )
        throws IOException {
        
        String data = s.getContent();
        int lastline = 1;
        int line = 1;
        
        final boolean isTemplate = s.isTemplate();

        Block.Type curType = 
            s.getName().endsWith( ".jxp" ) || 
            s.getName().endsWith( ".rhtml" ) || 
            s.getName().endsWith( ".html" ) ? 
            Block.Type.HTML : Block.Type.CODE;
        
        StringBuilder buf = new StringBuilder();
        
        boolean newLine = true;
        char lastChar = '\n';
        char codeOpening = ' ';
        int numBrackets = 0;
        
        Map<String,Stack<MacroEnd>> tagToStack = new HashMap<String,Stack<MacroEnd>>();
        List<Block> blocks = new ArrayList<Block>();
        
        if ( isTemplate ){
            switch ( s.getLanguage() ){

            case JS:
                blocks.add( Block.create( Block.Type.CODE , "var obj = arguments[0];\n" , -1 ) );
                break;

            case RUBY:
                //blocks.add( Block.create( Block.Type.CODE , "var obj = arguments[0];\n" , -1 ) );
                break;
                
            default:
                throw new RuntimeException( "what? " + s.getName() );
            }

        }
        
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
                
                String tag = getTag( data , i );
                if ( tag != null ){
                    boolean startTag = ! tag.startsWith( "/" );
                    if ( ! startTag )
                        tag = tag.substring( 1 );
                    tag = tag.toLowerCase();
                    
                    if ( DEBUG ) System.out.println( "found tag [" + tag + "] start : " + startTag );
                    
                    Stack<MacroEnd> stk = tagToStack.get( tag );
                    if ( stk == null ){
                        stk = new Stack<MacroEnd>();
                        tagToStack.put( tag , stk );
                    }

                    if ( startTag ){
                        
                        Block special = null;
                        Block mySpecial = null;
                    
                        int skip = 0;
                        boolean printTag = true;

                        if ( TagMacros.getBlocks( tag ) != null ){
                            String temp[] = TagMacros.getBlocks( tag );
                            String open = temp[0];
                            String close = temp[1];

                            if ( DEBUG ) System.out.println( "found tag macro for : " + tag );
                            
                            int tokenNumbers = 1;
                            
                            int start = i + tag.length() + 2;
                            while ( true ){
                                
                                while ( Character.isWhitespace( data.charAt( start ) ) )
                                    start++;
                                
                                if ( data.charAt( start ) == '>' )
                                    break;
                                
                                int end = getJSTokenEnd( data , start );
                                String token = data.substring( start , end ).trim();

                                if ( token.length() == 0 )
                                    break;
                                
                                if ( DEBUG ) System.out.println( "TOKEN:" + token );
                                open = open.replaceAll( "\\$" + tokenNumbers , token );
                                
                                start = end;
                                tokenNumbers++;
                            }
                            
                            skip = ( start - ( i + tag.length() + 1 ) );
                            printTag = false;
                            
                            mySpecial = Block.create( Block.Type.CODE , open , lastline );
                            special = Block.create( Block.Type.CODE , close , lastline );
                        }
                        else if ( i + tag.length() + 2 < data.length() && 
                             data.charAt( i + tag.length() + 1 ) == ' ' ){
                            
                            char temp = data.charAt( i + tag.length() + 2 );
                            
                            if ( temp == '?' ){
                                if ( DEBUG ) System.out.println( "found ?" );
                                
                                skip = 2;
                                int end = getJSTokenEnd( data , i + tag.length() + 2 + skip );
                                String token = data.substring( i + tag.length() + 2 + skip , end );
                                
                                mySpecial = Block.create( Block.Type.CODE , "if ( " + token + " ){ " , lastline );
                                special = Block.create( Block.Type.CODE , "}" , lastline );
                                
                                skip += token.length();
                            }
                        }
                        
                        stk.push( new MacroEnd( special , printTag ) );
                        
                        if ( mySpecial != null ){
                            blocks.add( Block.create( curType , buf.toString() , lastline ) );
                            buf.setLength( 0 );
                            blocks.add( mySpecial );
                            if ( printTag )
                                blocks.add( Block.create( Block.Type.HTML , data.substring( i , i + tag.length() + 1 ) , lastline ) );
                            i += tag.length() + 1 + skip;
                            continue;
                        }
                    }
                    else {
                        
                        if ( stk.size() > 0 ){
                            MacroEnd special = stk.pop();
                            if ( DEBUG ) System.out.println( "\t" + special );
                            
                            if ( special != null && special._block != null ){
                                while ( i < data.length() && data.charAt( i ) != '>' ){
                                    if ( special._printTag )
                                        buf.append( data.charAt( i ) );
                                    i++;
                                }
                                if ( special._printTag )
                                    buf.append( data.charAt( i ) );
                                
                                blocks.add( Block.create( curType , buf.toString() , lastline ) );
                                buf.setLength( 0 );
                                blocks.add( special._block );
                                
                                continue;
                            }
                            
                        }
                    }
                    
                }

                // do i have a template include
                if ( isTemplate && c == '$' ){
                    blocks.add( Block.create( curType , buf.toString() , lastline ) );
                    buf.setLength( 0 );
                    i++;
                    int end = getJSTokenEnd( data , i );

                    blocks.add( Block.create( Block.Type.OUTPUT , "obj." + data.substring( i , end ) , lastline ) );
                    i = end - 1;
                    curType = Block.Type.HTML;
                    continue;
                }
                
                // am i entering a jxp block?
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

    static int getJSTokenEnd( String data , final int start ){
        
        int parens = 0;
        int end = start;

        for ( ; end < data.length(); end++ ){
            char temp = data.charAt( end );
            
            if ( temp == '(' ){
                parens++;
                continue;
            }
            
            if ( temp == ')' ){
                parens--;
                continue;
            }
            
            if ( parens > 0 )
                continue;

            if ( Character.isLetterOrDigit( temp ) 
                 || temp == '.' 
                 || temp == '_' )
                continue;

            if ( Character.isWhitespace( temp ) )
                return end;
            
            if ( temp == '\''
		 || temp == ';' 
		 || temp == '#' 
                 || temp == '<'
                 || temp == '>'
                 || temp == '"' )
                return end;
        }
        
        return end;
    }


    static String getTag( String data , final int start ){
        int i = start;
        
        if ( data.charAt( i ) != '<' )
            return null;
        
        i++;
        if ( i >= data.length() )
            return null;
        
        if ( data.charAt( i ) == '/' )
            i++;
        
        if ( i >= data.length() )
            return null;
        
        for ( ; i < data.length(); i++ ){
            
            char temp = data.charAt(i);
            
            if ( temp == ' ' || temp == '>' )
                return data.substring( start + 1 , i );
            
            if ( Character.isLetter( temp ) )
                continue;
   
            return null;
        }
        return null;
    }

    public static void main( String args[] )
        throws Exception {

        System.out.println( parse( JxpSource.getSource( new File( "crap/www/index.jxp" ) ) ) );
        
    }

}
