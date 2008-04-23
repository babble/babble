// JxpConverter.java

package ed.appserver.templates;

import java.util.*;

import ed.util.*;

public class JxpConverter extends HtmlLikeConverter {

    public JxpConverter(){
        super( _codeTags );
    }

    protected boolean wants( Template t ){
        return t.getName().endsWith( ".jxp" );
    }
    
    protected String getNewName( Template t ){
        return t.getName().replace( "\\.jxp$" , "_jxp.js" );
    }

    protected void gotCode( Generator g , CodeMarker cm , String code ){

        if ( cm._startTag.equals( "<%=" ) ){
            g.append( "print( " );
            g.append( code );
            g.append( " );\n" );
            return;
        }

        if ( cm._startTag.equals( "<%" ) ){
            g.append( code );
            g.append( "\n" );
            return;
        }

        throw new RuntimeException( "can't handle : " + cm._startTag );
    }

    protected void gotStartTag( Generator g , String tag , String restOfTag ){
        gotText( g , "<" + tag + " " + restOfTag + ">" );
    }
    
    protected void gotEndTag( Generator g , String tag ){
        gotText( g , "</" + tag + ">" );
    }
    
    protected void gotText( Generator g , String text ){
        String lines[] = text.split( "[\r\n]+" );
        for ( String line : lines ){
            line = StringUtil.replace( line , "\\" , "\\\\" );
            line = StringUtil.replace( line , "\"" , "\\\"" );
            g.append( "print( \"" + line + "\" );\n" );
        }
    }

    static List<CodeMarker> _codeTags = new ArrayList<CodeMarker>();
    static {
        _codeTags.add( new CodeMarker( "<%=" , "%>" ) );
        _codeTags.add( new CodeMarker( "<%" , "%>" ) );
    }
}
