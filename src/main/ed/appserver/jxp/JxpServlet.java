// JxpServlet.java

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ed.js.*;
import ed.util.*;
import ed.js.engine.*;
import ed.js.func.*;

import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpServlet {
    
    JxpServlet( JxpSource source , JSFunction func ){
        _source = source;
        _theFunction = func;
    }

    public void handle( HttpRequest request , HttpResponse response , AppRequest ar ){
        
        final JxpWriter writer = response.getWriter();

        final Scope scope = ar.getScope();

        scope.put( "request" , request , true );
        scope.put( "response" , response , true );
        
        scope.put( "print" , new MyWriter( writer , getStaticPrefix( request , ar ) , ar.getContext() ) , true );
        
        try {
            _theFunction.call( scope );
        }
        catch ( RuntimeException re ){
            _source.fix( re );
            throw re;
        }
    }
    
    String getStaticPrefix( HttpRequest request , AppRequest ar ){

        String host = request.getHost();
        
        if ( host == null )
            return null;

        if ( host.indexOf( "." ) < 0 )
            return null;

        if ( request.getPort() > 0 )
            return null;

        String prefix= "http://static";

        if ( host.indexOf( "local." ) >= 0 )
            prefix += "-local";
        
        prefix += ".10gen.com/" + host;
        return prefix;
    }
    
    public static class MyWriter extends JSFunctionCalls1 {

        public MyWriter( JxpWriter writer , String cdnPrefix , AppContext context ){
            _writer = writer;
            _cdnPrefix = cdnPrefix;
            _context = context;
        }
        
        public Object call( Scope scope , Object o , Object extra[] ){
            if ( o == null )
                print( "null" );
            else
                print( o.toString() );
            return null;
        }
        
        public void print( String s ){

            if ( _extra.length() > 0 ){
                _extra.append( s );
                s = _extra.toString();
                _extra.setLength( 0 );
            }

            if ( _cdnPrefix == null ){
                _writer.print( s );
                return;
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
            
            printTag( _matcher.group(1) , wholeTag );

            print( s.substring( end + 1 ) );
            
        }

        void printTag( String tag , String s ){
            _writer.print( "<" );
            _writer.print( tag );
            _writer.print( " " );
            
            String srcName = null;
            if ( tag.equalsIgnoreCase( "img" ) ||
                 tag.equalsIgnoreCase( "script" ) )
                srcName = "src";
	    else if ( tag.equalsIgnoreCase( "link" ) )
		srcName = "href";
            else 
                throw new RuntimeException( "no name for : " + tag );
            
            s = s.substring( 2 + tag.length() );
            
            Matcher m = Pattern.compile( srcName + " *= *['\"](.+?)['\"]" , Pattern.CASE_INSENSITIVE ).matcher( s );
            if ( ! m.find() ){
                _writer.print( s );
                return;
            }
            
            _writer.print( s.substring( 0 , m.start(1) ) );
            String src = m.group(1);
            
            printSRC( src );
            
            _writer.print( s.substring( m.end(1) ) );
            
        }
    
        void printSRC( String src ){
            if ( src == null || src.length() == 0 )
                return;

            if ( src.startsWith( "CDN/" ) ){
                _writer.print( _cdnPrefix );
                _writer.print( src.substring( 3 ) );
                return;
            }
            
            if ( ! src.startsWith( "/" ) ){
                _writer.print( src );
                return;
            }
            
            String uri = src;
            int questionIndex = src.indexOf( "?" );
            if ( questionIndex >= 0 )
                uri = uri.substring( 0 , questionIndex );
            
            if ( _context != null ){
                File f = _context.getFile( uri );
                
                boolean exists = f.exists();
                
                if ( exists )
                    _writer.print( _cdnPrefix );

                _writer.print( src );
                
                if ( exists ){
                    if ( questionIndex < 0 )
                        _writer.print( "?" );
                    else
                        _writer.print( "&" );
                    _writer.print( "lm=" );
                    _writer.print( f.lastModified() );
                }
                
                return;
            }

            _writer.print( src );
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
        
        static final Pattern _tagPattern = Pattern.compile( "<(img|script|link) " , Pattern.CASE_INSENSITIVE );
        final Matcher _matcher = _tagPattern.matcher("");
        final StringBuilder _extra = new StringBuilder();

        final JxpWriter _writer;
        final String _cdnPrefix;
        final AppContext _context;

    }
    
    final JxpSource _source;
    final JSFunction _theFunction;
}
