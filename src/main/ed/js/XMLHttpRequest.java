// XMLHttpRequest.java

package ed.js;

import ed.js.func.*;
import ed.js.engine.*;

public class XMLHttpRequest extends JSObjectBase {
    
    public final static JSFunction _cons = new JSFunctionCalls3(){

            public JSObject newOne(){
                return new XMLHttpRequest();
            }

            public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                return open( s , methodObj , urlObj , asyncObj , args );
            }
            
            public Object open( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                
                XMLHttpRequest r = (XMLHttpRequest)s.getThis();
                if ( r == null )
                    r = new XMLHttpRequest();
                
                if ( urlObj != null )
                    r.open( methodObj , urlObj , asyncObj );

                return r;
            }    

            protected void init(){
                
                _prototype.set( "open" , new JSFunctionCalls3() {
                        public Object call( Scope s , Object methodObj , Object urlObj , Object asyncObj , Object[] args ){
                            return open( s , methodObj , urlObj , asyncObj , args );
                        }
                    } );
                
            }
        };

    public XMLHttpRequest(){
        super( _cons );
    }

    public XMLHttpRequest( String method , String url , boolean async ){
        super( _cons );
        init( method , url , async );
    }

    void open( Object method , Object url , Object async ){
        init( method.toString() , url.toString() , JSInternalFunctions.JS_evalToBool( async ) ); 
    }

    void init( String method , String url , boolean async ){
        
        _method = method;
        _url = url;
        _async = async;
        
        set( "method" , _method );
        set( "url" , _url );
        set( "async" , _async );
    }

    public String toString(){
        return _method + " " + _url;
    }

    String _method;
    String _url;
    boolean _async;
}
