// Djang10Converter.java

package ed.appserver.templates;

import java.util.*;

import ed.util.*;

public class Djang10Converter extends HtmlLikeConverter {

    public Djang10Converter(){
        super( "djang10" , _codeTags );
    }

    protected boolean wants( Template t ){
        return t.getName().endsWith( ".djang10" );
    }

    protected String getNewName( Template t ){
        return t.getName().replace( "\\.djang10" , "_djang10.js" );
    }

    protected void gotCode( Generator g , CodeMarker cm , String code ){
        if ( cm._startTag.equals( "{{" ) ){
            g.append( "print( " );
            g.append( code );
            g.append( " );\n" );
            return;
        }

        throw new RuntimeException( "can't handle : " + cm._startTag );
    }

    protected boolean gotStartTag( Generator g , String tag , State state ){
        return false;
    }
    
    protected boolean gotEndTag( Generator g , String tag , State state ){
        return false;
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
        _codeTags.add( new CodeMarker( "{{" , "}}" ) );
        //_codeTags.add( new CodeMarker( "{" , "}" ) );

    }

}
