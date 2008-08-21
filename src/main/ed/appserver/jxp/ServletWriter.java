// ServletWriter.java

/**
 *    Copyright (C) 2008 10gen Inc.
 *  
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *  
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;
import ed.js.engine.*;
import ed.js.func.*;
import ed.lang.*;

import ed.appserver.*;
import ed.net.httpserver.*;

public class ServletWriter extends JSFunctionCalls1 {

    public static final int MAX_WRITTEN_LENGTH = 1024 * 1024 * 15;

    public ServletWriter( JxpWriter writer , String cdnPrefix , String cdnSuffix , AppContext context ){
        this( writer , new URLFixer( cdnPrefix , cdnSuffix , context ) );
    }

    public ServletWriter( JxpWriter writer , URLFixer fixer ){
        _writer = writer;
        _fixer = fixer;
            
        if ( _writer == null )
            throw new NullPointerException( "writer can't be null" );

        set( "setFormObject" , new JSFunctionCalls1(){ 
                public Object call( Scope scope , Object o , Object extra[] ){
                    if ( o == null ){
                        _formInput = null;
                        return null;
                    }
                        
                    if ( ! ( o instanceof JSObject ) )
                        throw new RuntimeException( "must be a JSObject" );
                        
                    _formInput = (JSObject)o;
                    _formInputPrefix = null;
                        
                    if ( extra != null && extra.length > 0 )
                        _formInputPrefix = extra[0].toString();
                        
                    return o;
                }
            } );
    }

    public Writer asJavaWriter(){
        return new Writer(){
            public void close(){
                return;
            }
            
            public void flush(){
                return;
            }
            
            public void write(char[] cbuf, int off, int len){
                ServletWriter.this.print( new String( cbuf , off , len ) );
            }
        };
    }
    
    public Object get( Object n ){
        if ( "cdnPrefix".equals( n ) )
            return _fixer.getCDNPrefix();
        if ( "cdnSuffix".equals( n ) )
            return _fixer.getCDNSuffix();
        return super.get( n );
    }

    public Object set( Object n , Object v ){
        if ( "cdnPrefix".equals( n ) )
            return _fixer.setCDNPrefix( v.toString() );
        if ( "cdnSuffix".equals( n ) )
            return _fixer.setCDNSuffix( v.toString() );
        return super.set( n  , v );
    }
        
    public Object call( Scope scope , Object o , Object extra[] ){
        if ( o == null )
            print( "null" );
        else
            print( JSInternalFunctions.JS_toString( o ) );
            
        return null;
    }
        
    public void print( String s ){
            
        if ( ( _writtenLength += s.length() ) > MAX_WRITTEN_LENGTH )
            throw new RuntimeException( "trying to write a dynamic page more than " + MAX_WRITTEN_LENGTH + " chars long" );

        if ( _writer.closed() )
            throw new RuntimeException( "output closed.  are you using an old print function" );
            
        while ( s.length() > 0 ){
                
            if ( _extra.length() > 0 ){
                _extra.append( s );
                s = _extra.toString();
                _extra.setLength( 0 );
            }
                
            _matcher.reset( s );
            if ( ! _matcher.find() ){
                _writer.print( s );
                return;
            }
                
            _writer.print( s.substring( 0 , _matcher.start() ) );
                
            s = s.substring( _matcher.start() );
            int end = endOfTag( s );
            if ( end == -1 ){
                _extra.append( s );
                return;
            }
                
            String wholeTag = s.substring( 0 , end + 1 );
                
            if ( ! printTag( _matcher.group(1) , wholeTag ) )
                _writer.print( wholeTag );
                
            s = s.substring( end + 1 );
        }
            
    }

    /**
     * @return true if i printed tag so you should not
     */
    boolean printTag( String tag , String s ){

        if ( tag == null )
            throw new NullPointerException( "tag can't be null" );
        if ( s == null )
            throw new NullPointerException( "show tag can't be null" );

        if ( tag.equalsIgnoreCase( "/head" ) && ! _writer.hasSpot() ){
            _writer.saveSpot();
            return false;
        }

        { // CDN stuff
            String srcName = null;
            if ( tag.equalsIgnoreCase( "img" ) ||
                 tag.equalsIgnoreCase( "script" ) )
                srcName = "src";
            else if ( tag.equalsIgnoreCase( "link" ) ){
                srcName = "href";
                Matcher m = _attributeMatcher( "type" , s );
                if ( m.find() ){
                    String type = m.group(1);
                    if ( type.contains( "rss" ) || type.contains( "atom" ) )
                        srcName = null;
                }
            }
                
            if ( srcName != null ){
                    
                s = s.substring( 2 + tag.length() );
                    
                // TODO: cache pattern or something
                Matcher m = _attributeMatcher( srcName , s );
                if ( ! m.find() )
                    return false;
                    
                _writer.print( "<" );
                _writer.print( tag );
                _writer.print( " " );
                    
                _writer.print( s.substring( 0 , m.start(1) ) );
                String src = m.group(1);
                    
                printSRC( src );
                    
                _writer.print( s.substring( m.end(1) ) );

                return true;
            }
                
        }
            
        if ( _formInput != null && tag.equalsIgnoreCase( "input" ) ){
            Matcher m = Pattern.compile( "\\bname *= *['\"](.+?)[\"']" ).matcher( s );

            if ( ! m.find() )
                return false;
                
            String name = m.group(1);
            if ( name.length() == 0 )
                return false;
                
            if ( _formInputPrefix != null )
                name = name.substring( _formInputPrefix.length() );
                
            Object val = _formInput.get( name );
            if ( val == null )
                return false;
                
            if ( s.toString().matches( "value *=" ) )
                return false;

            _writer.print( s.substring( 0 , s.length() - 1 ) );
            _writer.print( " value=\"" );
            _writer.print( HtmlEscape.escape( val.toString() ) );
            _writer.print( "\" >" );
                
            return true;
        }

        return false;
    }

    /**
     * takes the actual src of the asset and fixes and prints
     * i.e. /foo -> static.com/foo
     */
    void printSRC( String src ){
            
        if ( src == null || src.length() == 0 )
            return;
	    
        _fixer.fix( src , _writer );
    }

    int endOfTag( String s ){
        for ( int i=0; i<s.length(); i++ ){
            char c = s.charAt( i );
            if ( c == '>' )
                return i;
                
            if ( c == '"' || c == '\'' ){
                for ( ; i<s.length(); i++)
                    if ( c == s.charAt( i ) )
                        break;
            }
        }
        return -1;
    }
     
    static Matcher _attributeMatcher( String name , String tag ){
        Pattern p = _attPatternCache.get( name );
        if ( p == null ){
            p = Pattern.compile( name + " *= *['\"](.+?)['\"]" , Pattern.CASE_INSENSITIVE );
            _attPatternCache.put( name , p );
        }
        return p.matcher( tag );
    }
   
    static final Pattern _tagPattern = Pattern.compile( "<(/?\\w+)[ >]" , Pattern.CASE_INSENSITIVE );
    static final Map<String,Pattern> _attPatternCache = Collections.synchronizedMap( new HashMap<String,Pattern>() );
    final Matcher _matcher = _tagPattern.matcher("");
    final StringBuilder _extra = new StringBuilder();

    final JxpWriter _writer;
    final URLFixer _fixer;

    JSObject _formInput = null;
    String _formInputPrefix = null;
        
    int _writtenLength = 0;
}
