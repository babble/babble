// LoadMonitor.java

package ed.net.lb;

import java.util.*;

import ed.net.*;
import ed.net.httpserver.*;
import ed.util.*;

public class LoadMonitor {

    LoadMonitor( Router r ){
        _router = r;
    }
    
    public void hit( HttpRequest request , HttpResponse response ){
        _all.hit( request , response );
	_router.getPool( request )._tracker.hit( request , response );
    }

    HttpLoadTracker getTracker( String name ){
	return _router.getPool( name )._tracker;
    }
    
    void _addMonitors( String name ){
	HttpServer.addGlobalHandler( new HttpMonitor( name + "-load" ){
                public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
                    _all.displayGraph( out );
                }
            }
            );
    }
    
    final Router _router;
    final HttpLoadTracker _all = new HttpLoadTracker( "all" , 20 , 60 );
}
