// AppWork.java

package ed.appserver;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

import ed.util.*;
import ed.js.*;
import ed.db.*;

public abstract class AppWork implements Comparable<AppWork> {
    
    protected AppWork( AppContext context , String name ){
        _context = context;
        _name = name;
        _id = new ObjectId();
    }
    
    public abstract void run();
    
    public int compareTo( AppWork other ){
        return (int)(this._created - other._created);
    }

    final protected AppContext _context;
    final protected String _name;
    final protected ObjectId _id;
    final protected long _created = System.currentTimeMillis();
    
    public static class FunctionAppWork extends AppWork {

        public FunctionAppWork( AppContext context , String name , JSFunction func , Object ... args  ){
            super( context , name );
            _func = func;
            _args = args;
        }
        
        public void run(){
            _func.call( _context.getScope() , _args );
        }
        
        final JSFunction _func;
        final Object[] _args;
    }

    
    public static void addQueue( Queue<AppWork> queue ){
        _queues.add( new WeakReference<Queue<AppWork>>( queue ) );
    }

    public static class Processor extends ThreadPool<AppWork> implements Runnable {
    
        Processor(){
            super( "AppWork-Processor" , 10 );
        }
        
        public void handle( AppWork work )
            throws Exception{
            work.run();
        }
        
        public void handleError( AppWork work , Exception e ){
            work._context.getLogger().error( "error doing work" , e );
        }
        
        AppWork getWorkToDo(){
            int size = _queues.size();
            for ( int i=0; i<size; i++ ){
                int pos = ( _lastSpot + i ) % size;
                
                Queue<AppWork> q = _queues.get( pos ).get();
                if ( q == null ){
                    _queues.remove( pos );
                    return getWorkToDo();
                }
                
                AppWork w = q.poll();
                if ( w == null )
                    continue;
                
                _lastSpot = pos;
                return w;
            }
            
            return null;
        }
        
        public void run(){
            while ( true ){
                
                AppWork w = getWorkToDo();
                if ( w != null ){
                    offer( w );
                    continue;
                }
                
                ThreadUtil.sleep( 10 );
            }
        }
        
        int _lastSpot = 0;
    }
    
    private static final List<WeakReference<Queue<AppWork>>> _queues = new ArrayList<WeakReference<Queue<AppWork>>>();
    private static final Processor _processor = new Processor();
    private static final Thread _processorThread = new Thread( _processor );
    static {
        _processorThread.setDaemon( true );
        _processorThread.start();
    }
}
