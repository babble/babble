// HtmlLikeConverter.java

package ed.appserver.templates;

import java.util.*;

import ed.lang.*;
import ed.util.*;

public abstract class HtmlLikeConverter implements TemplateConverter {

    static final boolean DEBUG = Boolean.getBoolean( "DEBUG.TEMPLATES" );

    static class CodeMarker {

        CodeMarker( String startTag , String endTag ){
            _startTag = startTag;
            _endTag = endTag;
        }
        
        int findEnd( String data , int start ){
            return data.indexOf( _endTag , start );
        }

        final String _startTag;
        final String _endTag;
    }
    
    /**
     * @param codeTagName for jxp would be "%" for php ?
     */
    protected HtmlLikeConverter( String extension , List<CodeMarker> markers , Language sourceLanguage ){
        _extension = extension.replaceAll( "^\\." , "" );
        _markers = markers;
        _sourceLanguage = sourceLanguage;
    }

    // ----------------------
    // -- things to override --
    // ----------------------

    /**
     * override if you want to do any pre-processing
     */
    protected String getContent( Template t ){
        return t.getContent();
    }

    protected void start( Generator g ){}
    protected void end( Generator g ){}

    protected boolean wants( Template t ){
        return _extension.equals( t.getExtension() );
    }

    protected abstract String getNewName( Template t );

    protected abstract void gotCode( Generator g , CodeMarker cm , String code );
    
    /**
     * @return true if handled by subclass
     */
    protected abstract boolean gotStartTag( Generator g , String tag , State state );
    /**
     * @return true if handled by subclass
     */
    protected abstract boolean gotEndTag( Generator g , String tag , State state );
    
    
    /**
       very well may want to override this
     */
    protected void gotText( Generator g , String text ){
        final boolean endsWithNewLine = text.endsWith( "\n" );

        final String lines[] = text.split( "[\r\n]+" );
        for ( int i=0; i<lines.length; i++ ){
            String line = lines[i];

            line = StringUtil.replace( line , "\\" , "\\\\" );
            line = StringUtil.replace( line , "\"" , "\\\"" );
            g.append( "print( \"" + line + ( i + 1 < lines.length || endsWithNewLine ? "\\n" : "" )  + "\" );\n" , i + 1 != lines.length );
        }
    }

    protected Generator createGenerator( Template t , State s ){
        return new Generator( s );
    }


    // ----------------------
    // -- internal --
    // ----------------------

    public Result convert( final Template t ){
        
        if ( ! wants( t ) )
            return null;
        
        final State state = new State( getContent( t ) );
        final Generator g = createGenerator( t , state );
        
        start( g );

        StringBuilder text = new StringBuilder();

        // the main loop just goes through html. 
        htmlloop:
        while ( state.hasNext() ){
            final char c = state.next();

            if ( c == '\r' ) 
                continue;
            
            for ( CodeMarker cm : _markers ){
                if ( state.startsWith( cm._startTag ) ){

                    gotText( g , text.toString() );
                    text.setLength( 0 );

                    state.skip( cm._startTag.length() - 1 );
                    
                    final int end = cm.findEnd( state.data , state.pos );
                    final String code = state.data.substring( state.pos , end );
                    gotCode( g , cm , code );
                    
                    state.skip( ( end - state.pos ) );
                    if ( cm._endTag != null )
                        state.skip( cm._endTag.length() );

                    continue htmlloop;
                }
            }
            
            if ( c == '<' && state.hasNext() ){ // start of a html tag
                final String tag = _readTag( state );
                if ( tag == null || tag.length() == 0 ){
                    text.append( c );
                    continue;
                }
                
                state.eatWhiteSpace();
                if ( tag.startsWith( "/" ) && state.peek() != '>' ){
                    text.append( "<" + tag + " " );
                    continue;
                }
                    

                gotText( g , text.toString() );
                text.setLength( 0 );
                
                
                if ( tag.startsWith( "/" ) ){
                    final String tagName = tag.substring(1);
                    if ( ! gotEndTag( g , tagName , state ) )
                        text.append( "</" + tagName );
                }
                else 
                    if ( ! gotStartTag( g , tag , state ) )
                        text.append( "<" + tag + " " );
                
                continue;
            }
            
            text.append( c );
        }

        gotText( g , text.toString() );
        text.setLength( 0 ); // not needed, but might as well be safe
        
        end( g );

        if ( DEBUG ){
            System.out.println( "----" );
            System.out.println( g._buf );
            System.out.println( "----" );
            System.out.println( g._lineMapping );
        }

        return new Result( new Template( getNewName( t ) , g._buf.toString() , _sourceLanguage ) , g._lineMapping );
    }

    final String _readTag( State state ){
        char c = state.peek();

        // cases where its not really a tag
        if ( Character.isWhitespace( c ) || 
             c == '=' )
            return null;
        
        // now we're sure its actually a tag, so its safe to move state forward
        StringBuilder buf = new StringBuilder();
        while ( state.hasNext() ){
            c = state.peek();
            if ( Character.isWhitespace( c ) 
                 || c == '>' 
                 || c == '"'
                 )
                break;
            buf.append( state.next() );
        }
        return buf.toString();
    }

    class State {
        
        State( String data ){
            this.data = data;
            this.dataLength = data.length();
        }
        
        final boolean hasNext(){
            return pos < dataLength;
        }

        final char next(){
            lastChar = curChar;
            curChar = data.charAt( pos++ );
            if ( curChar == '\n' )
                line++;
            return curChar;
        }
        
        final void eatWhiteSpace(){
            while ( pos < data.length() && 
                    Character.isWhitespace( data.charAt( pos ) ))
                pos++;
        }

        final String readRestOfTag(){

            StringBuilder buf = new StringBuilder();
            
            boolean inquote = false;
            
            while ( hasNext() ){
                char c = next();
                
                if ( c == '"' ){
                    inquote = ! inquote;
                    buf.append( c );
                    continue;
                }
                
                if ( inquote ){
                    buf.append( c );
                    continue;
                }
                
                if ( c == '>' )
                    break;
                
                buf.append( c );
            }
            
            return buf.toString();
        }

        final char peek(){
            return data.charAt( pos );
        }

        final boolean newLine(){
            return lastChar == '\n';
        }

        final boolean startsWith( String s ){
            return data.startsWith( s , pos - 1 );
        }

        final void skip( int num ){
            for ( int i=0; i<num; i++ )
                next();
        }

        final String data;
        final int dataLength;
        
        int pos = 0;
        int line = 1;

        char lastChar = '\n';
        char curChar = '\n';
    }

    class Generator {
        Generator( State state ){
            _state = state;
            _buf = new StringBuilder( _state.data.length() );
        }
        
        public void append( String code ){
            append( code , false );
        }

        public void append( String code , boolean incLineNumbers ){
            _buf.append( code );
            
            for ( int i=0; i<code.length(); i++ ){
                if ( code.charAt( i ) == '\n' ){
                    _lineMapping.put( _outLine++ , _inLine );
                    
                    if ( incLineNumbers ) 
                        _inLine++;

                    if ( DEBUG ) 
                        System.out.println( " : " + ( _outLine - 1 ) + " -->> " + _inLine );
                }
            }
            if ( ! incLineNumbers )
                _inLine = _state.line;
        }

        int _outLine = 1;
        int _inLine = 1;
        final State _state;
        final StringBuilder _buf;
        final Map<Integer,Integer> _lineMapping = new TreeMap<Integer,Integer>();
    }

    final String _extension;
    final List<CodeMarker> _markers;
    final Language _sourceLanguage;
}
