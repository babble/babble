// Generator.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import org.mozilla.javascript.*;

import ed.appserver.jxp.Block.Type.*;

public class Generator {

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
            
        default :
            throw new RuntimeException( "can't handle : " + b.getType() );
        }
    }

    void addHtmlBlock( HtmlBlock b ){
        for ( String line : b.getRaw().split( "[\r\n]+" ) ){
            _append( "print( \"" , b );
            _append( line , b );
            _append( " \\\\n\" );\n " , b );
        }
    }

    void addCodeBlock( CodeBlock b ){
        _append( b.getRaw() , b );
    }

    void addOutputBlock( CodeBlock b ){
        _append( "print( " , b );
        _append( b.getRaw()   , b);
        _append( " );\n  " , b );
    }

    public String toString(){
        return _secretBuf.toString();
    }

    final void _append( String s , Block b ){
        _secretBuf.append( s );

        int numLines = 0;
        for ( int i=0; i<s.length(); i++ )
            if ( s.charAt( i ) == '\n' )
                numLines++;


        final int start = _currentLineNumber;
        final int end = _currentLineNumber + numLines;
        
        for ( int i=start; i<end; i++ ){
            List<Block> l = _jsCodeToLines.get( i );
            if ( l == null ){
                l = new ArrayList<Block>();
                _jsCodeToLines.put( i , l );
            }
            l.add( b );
        }


        _currentLineNumber = end;

    }

    final StringBuilder _secretBuf = new StringBuilder();
    
    private int _currentLineNumber = 0;
    final Map<Integer,List<Block>> _jsCodeToLines = new TreeMap<Integer,List<Block>>();

    public static void main( String args[] )
        throws Exception {

        JxpSource js = JxpSource.getSource( new java.io.File( "crap/www/index.jxp" ) );
        
        System.out.println( genJavaScript( js.getBlocks() ) );
        
    }
}
