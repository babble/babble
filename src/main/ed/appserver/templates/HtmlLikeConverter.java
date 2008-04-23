// HtmlLikeConverter.java

package ed.appserver.templates;

import java.util.*;

public abstract class HtmlLikeConverter implements TemplateConverter {

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
    protected HtmlLikeConverter( List<CodeMarker> markers ){
        _markers = markers;
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

    protected abstract boolean wants( Template t );
    protected abstract String getNewName( Template t );

    protected abstract void gotCode( Generator g , CodeMarker cm , String code );
    protected abstract void gotStartTag( Generator g , String tag , String restOfTag );
    protected abstract void gotEndTag( Generator g , String tag );
    protected abstract void gotText( Generator g , String text );

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
                    gotCode( g , cm , state.data.substring( state.pos , end - 1 ) );
                    
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
                
                gotText( g , text.toString() );
                text.setLength( 0 );
                
                final String rest = _readRestOfTag( state );
                
                if ( tag.startsWith( "/" ) ){
                    gotEndTag( g , tag.substring(1) );
                }
                else {
                    gotStartTag( g , tag , rest );
                }
                
                continue;
            }
            
            text.append( c );
        }
        
        end( g );

        System.out.println( "----" );
        System.out.println( g._buf );
        System.out.println( "----" );

        return new Result( new Template( getNewName( t ) , g._buf.toString() ) );
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

    final String _readRestOfTag( State state ){
        StringBuilder buf = new StringBuilder();
        
        boolean inquote = false;
        
        while ( state.hasNext() ){
            char c = state.next();

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
        
        void append( String code ){
            _buf.append( code );
        }

        final State _state;
        final StringBuilder _buf;
    }

    final List<CodeMarker> _markers;
}
