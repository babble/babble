// JxpServlet.java

package ed.appserver.jxp;

import ed.js.*;
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

        final Scope scope = ar.getContext().scope().child();

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
        
        _theFunction.call( scope );
    }

    final JxpSource _source;
    final JSFunction _theFunction;
}
