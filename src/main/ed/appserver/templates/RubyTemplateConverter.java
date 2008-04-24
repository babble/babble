// RubyTemplateConverter.java

package ed.appserver.templates;

import java.util.*;

import ed.util.*;

public class RubyTemplateConverter extends HtmlLikeConverter {

    public RubyTemplateConverter( String ext ){
        super( ext , _codeTags );
    }

    protected String getNewName( Template t ){
        return t.getName().replaceAll( "\\.(\\w+)+$" , "_$1.rb" );
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
    }

    protected boolean gotStartTag( Generator gg , String tag , State state ){
        return false;
    }
    
    protected boolean gotEndTag( Generator gg , String tag , State state ){
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
        _codeTags.add( new CodeMarker( "<%=" , "%>" ) );
        _codeTags.add( new CodeMarker( "<%" , "%>" ) );
    }

}
