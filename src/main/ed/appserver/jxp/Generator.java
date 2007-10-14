// Generator.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;

import ed.appserver.jxp.Block.Type.*;

public class Generator {

    static String genJavaScript( List<Block> blocks ){
        Generator g = new Generator();
        for ( Block b : blocks )
            g.add( b );
        return g.toString();
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
            _buf.append( "print( \"" );
            _buf.append( line );
            _buf.append( " \\n\" );\n " );
        }
    }

    void addCodeBlock( CodeBlock b ){
        _buf.append( b.getRaw() );
    }

    void addOutputBlock( CodeBlock b ){
        _buf.append( "print( " );
        _buf.append( b.getRaw() );
        _buf.append( " );\n  " );
    }

    public String toString(){
        return _buf.toString();
    }

    final StringBuilder _buf = new StringBuilder();

    public static void main( String args[] )
        throws Exception {

        JxpSource js = JxpSource.getSource( new java.io.File( "crap/www/index.jxp" ) );
        
        System.out.println( genJavaScript( js.getBlocks() ) );
        
    }
}
