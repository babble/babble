// JxpServlet.java

package ed.appserver.jxp;

import java.util.*;

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
        
        scope.put( "print" , 
                   new JSFunctionCalls1(){
                       public Object call( Scope scope , Object o , Object extra[] ){
                           if ( o == null )
                               writer.print("null");
                           else
                               writer.print( o.toString() );
                           return null;
                       }
                   } ,
                   true );
        
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
                    System.out.println( "yay" );
                }
            }
            throw re;
        }
    }

    final JxpSource _source;
    final JSFunction _theFunction;
}
