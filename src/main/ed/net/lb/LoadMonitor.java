// LoadMonitor.java

package ed.net.lb;

import ed.net.httpserver.*;
import ed.util.*;

public class LoadMonitor {

    final static int trackingInverval = 5000;
    final static int secondsBack = 3600;
    final static int intervals = secondsBack / (int)( trackingInverval / 1000 );

    LoadMonitor( Router r ){
        _router = r;
    }
    
    public void hit( HttpRequest request , long in , long out , boolean ok ){
        _all.hit( in , out , ok );
    }

    class Tracker {

        Tracker( String name ){
            _name = name;
        }
        
        void hit( long in , long out , boolean ok ){
            _reqPerSecTracker.hit();
            _dataIn.hit( in );
            _dataOut.hit( out );
            _errors.hit( ok ? 0 : 1 );
        }

        final String _name;
        final ThingsPerTimeTracker _reqPerSecTracker = new ThingsPerTimeTracker( trackingInverval  , intervals );
        final ThingsPerTimeTracker _dataIn = new ThingsPerTimeTracker( trackingInverval , intervals );
        final ThingsPerTimeTracker _dataOut = new ThingsPerTimeTracker( trackingInverval , intervals );
        final ThingsPerTimeTracker _errors = new ThingsPerTimeTracker( trackingInverval , intervals );
    }

    final Router _router;
    final Tracker _all = new Tracker( "all" );
}
