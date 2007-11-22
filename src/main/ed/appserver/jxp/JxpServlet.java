// JxpServlet.java

package ed.appserver.jxp;

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
        
        scope.put( "print" , new MyWriter( writer , null ) , true );
        
        try {
            _theFunction.call( scope );
        }
        catch ( RuntimeException re ){
                
            if ( _source._jsCodeToLines != null ){
                StackTraceElement stack[] = re.getStackTrace();
                
                boolean changed = false;
                for ( int i=0; i<stack.length; i++ ){
                    
                    StackTraceElement element = stack[i];
                    if ( element == null )
                        continue;
                    
                    String es = element.toString();
                    if ( ! es.contains( _source._lastFileName ) )
                        continue;
                    
                    int line = StringParseUtil.parseInt( es.substring( es.lastIndexOf( ":" ) + 1 ) , -1 );
                    List<Block> blocks = _source._jsCodeToLines.get( line );

                    System.out.println( line + " : " + blocks );

                    if ( blocks == null )
                        continue;
                    
                    stack[i] = new StackTraceElement( _source.getName() , stack[i].getMethodName() , _source.getName() , blocks.get( 0 )._lineno );
                    changed = true;
                    System.out.println( stack[i] );
                    
                }
                
                if ( changed ){
                    re.setStackTrace( stack );
                }
            }
            throw re;
        }
    }
    
    public static class MyWriter extends JSFunctionCalls1 {

        public MyWriter( JxpWriter writer , String cdnPrefix ){
            _writer = writer;
            _cdnPrefix = cdnPrefix;
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
            
            if ( src.length() > 0 ){
                if ( ! src.startsWith( "/" ) ){
                    _writer.print( src );
                }
                else {
                    _writer.print( _cdnPrefix );
                    _writer.print( src );
                }
            }
            _writer.print( s.substring( m.end(1) ) );
            
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
        
        static final Pattern _tagPattern = Pattern.compile( "<(img|script) " , Pattern.CASE_INSENSITIVE );
        final Matcher _matcher = _tagPattern.matcher("");
        final JxpWriter _writer;
        final String _cdnPrefix;

        final StringBuilder _extra = new StringBuilder();
    }
    
    final JxpSource _source;
    final JSFunction _theFunction;
}
