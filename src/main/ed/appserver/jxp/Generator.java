// Generator.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import org.mozilla.javascript.*;

import ed.util.*;
import ed.appserver.jxp.Block.Type.*;

public class Generator {
    
    static boolean DEBUG = false;

    static Generator genJavaScript( List<Block> blocks ){
        Generator g = new Generator();
        for ( Block b : blocks )
            g.add( b );
        return g;
    }

    private Generator(){
        
    }

    void add( Block b ){
        switch ( b.getType() ){
        case HTML:
            addHtmlBlock( (HtmlBlock)b );
            break;
        case CODE:
            addCodeBlock( (CodeBlock)b );
            break;
        case OUTPUT:
            addOutputBlock( (CodeBlock)b );
            break;
        case WIKI:
            addWikiBlock( (WikiBlock)b );
            break;
        default :
            throw new RuntimeException( "can't handle : " + b.getType() );
        }
    }
    
    void addWikiBlock( WikiBlock b ){
        _append( "print( \"<div style='border: red 1px solid;'>\" );" , b );
        _append( "print( \"" + b.getRaw().replaceAll( "[\r\n]+" , "" ) + "\" ); " , b );
        _append( "print( \"</div>\" );" , b );
    }
    
    void addHtmlBlock( HtmlBlock b ){
        String lines[] = b.getRaw().split( "[\r\n]+" );
        for ( int i=0; i<lines.length; i++ ){
            String line = lines[i];
            _append( "print( \"" , b );
            line = StringUtil.replace( line , "\\" , "\\\\" );
            line = StringUtil.replace( line , "\"" , "\\\"" );
            _append( line , b );
            if ( i + 1 < lines.length )
                _append( " \\n " , b );
            _append( "\" );\n " , b );
        }
    }

    void addCodeBlock( CodeBlock b ){
        _append( b.getRaw() , b );
    }

    void addOutputBlock( CodeBlock b ){
        _append( "print( " , b );
        _append( b.getRaw()   , b);
        _append( " );\n" , b );
    }

    public String toString(){
        return _secretBuf.toString();
    }

    final void _append( String s , Block b ){
        if ( s.matches( ".*\n *$" ) ){
            s = s.trim() + "\n";
        }
        _secretBuf.append( s );
        
        int numLines = 0;
        for ( int i=0; i< ( s.length() - 1 ); i++ )
            if ( s.charAt( i ) == '\n' )
                numLines++;
        
        if ( DEBUG ) System.out.println( "numLines : " + numLines );

        final int start = _currentLineNumber;
        final int end = _currentLineNumber + numLines;
        
        for ( int i=start; i<=end; i++ ){
            List<Block> l = _jsCodeToLines.get( i );
            if ( l == null ){
                l = new ArrayList<Block>();
                _jsCodeToLines.put( i , l );
            }
            l.add( b );
            if ( DEBUG ) System.out.println( "\t" + i + "\t" + b + " || " + s );
            
        }
        
        _currentLineNumber = end;
        if ( s.endsWith( "\n" ) )
            _currentLineNumber++;

    }

    final StringBuilder _secretBuf = new StringBuilder();
    
    private int _currentLineNumber = 1;
    final Map<Integer,List<Block>> _jsCodeToLines = new TreeMap<Integer,List<Block>>();

    public static void main( String args[] )
        throws Exception {

        JxpSource js = JxpSource.getSource( new java.io.File( "crap/www/index.jxp" ) );
        
        System.out.println( genJavaScript( js.getBlocks() ) );
        
    }
}
