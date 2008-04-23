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

    protected Generator createGenerator( Template t , State s ){
        return new MyGenerator( s );
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

    protected void gotStartTag( Generator gg , String tag , String restOfTag ){
        MyGenerator g = (MyGenerator)gg;
        
        TagEnd te = null;
        
        if ( restOfTag.trim().startsWith( "?" ) ){
            restOfTag = restOfTag.trim().substring(1).trim();
            
            int end = getJSTokenEnd( restOfTag , 0 );

            String cond = restOfTag.substring( 0 , end );
            restOfTag = restOfTag.substring( end );
            
            g.append( "if ( " + cond + " ){\n" );
            
            te = new TagEnd( " }\n" );
        }
        
        g.tagPush( tag , te );

        gotText( g , "<" + tag + " " + restOfTag + ">" );
    }
    
    protected void gotEndTag( Generator gg , String tag ){
        MyGenerator g = (MyGenerator)gg;
        
        gotText( g , "</" + tag + ">" );

        TagEnd te = g.tagPop( tag );
        if ( te != null )
            g.append( te._code );
    }
    
    protected void gotText( Generator g , String text ){
        String lines[] = text.split( "[\r\n]+" );
        for ( String line : lines ){
            line = StringUtil.replace( line , "\\" , "\\\\" );
            line = StringUtil.replace( line , "\"" , "\\\"" );
            g.append( "print( \"" + line + "\" );\n" );
        }
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
                 || temp == '"'
                 || temp == ':' )
                return end;
        }
        
        return end;
    }

    class TagEnd {
        TagEnd( String code ){
            _code = code;
        }
        
        final String _code;
    }

    class MyGenerator extends Generator {
        MyGenerator( State s ){
            super( s );
        }
        
        void tagPush( String tag , TagEnd te ){
            tag = tag.toLowerCase();

            Stack<TagEnd> s = _tagToStack.get( tag );
            if ( s == null ){
                s = new Stack<TagEnd>();
                _tagToStack.put( tag , s );
            }
            s.push( te );
        }
        
        TagEnd tagPop( String tag ){ 
            tag = tag.toLowerCase();
            
            Stack<TagEnd> s = _tagToStack.get( tag );
            if ( s == null )
                return null;
            if ( s.size() == 0 )
                return null;
            return s.pop();
        }

        final Map<String,Stack<TagEnd>> _tagToStack = new HashMap<String,Stack<TagEnd>>();
    }

    static List<CodeMarker> _codeTags = new ArrayList<CodeMarker>();
    static {
        _codeTags.add( new CodeMarker( "<%=" , "%>" ) );
        _codeTags.add( new CodeMarker( "<%" , "%>" ) );
    }
}
