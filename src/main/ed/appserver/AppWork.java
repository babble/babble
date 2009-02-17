// AppWork.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.appserver;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

import ed.util.*;
import ed.js.*;
import ed.js.engine.*;
import ed.db.*;

public abstract class AppWork implements Comparable<AppWork> {
    
    protected AppWork( AppContext context , String name ){
        _context = context;
        _name = name;
        _id = new ObjectId();
    }
    
    public abstract Object run();
    
    public int compareTo( AppWork other ){
        return (int)(this._created - other._created);
    }

    public boolean isDone(){
        return _done;
    }

    public void join(){
        getResult( true );
    }
    
    public Object returnData(){
        return getResult( true );
    }
    
    public Object getResult(){
        return getResult( true );
    }

    public Object getResult( boolean block ){
        
        boolean added = false;
        while ( ! _done && block ){
            if ( ! added ){
                _waiters.add( Thread.currentThread() );
                added = true;
            }
            
            try {
                Thread.sleep( 100 );
            }
            catch ( InterruptedException ie ){
            }
        }
        
        if ( ! _done )
            throw new RuntimeException( "not done yet" );

        if ( _error != null )
            throw _error;

        return _result;
    }

    public void start(){
    }
    
    void done( Object result ){
        _result = result;
        _done();
    }

    void error( RuntimeException re ){
        _error = re;
        _context.getLogger( "queuework" ).error( _name , re );
        _done();
    }
    
    private void _done(){
        _done = true;
        for ( Thread t : _waiters )
            t.interrupt();
        _waiters.clear();
    }

    final protected AppContext _context;
    final protected String _name;
    final protected ObjectId _id;
    final protected long _created = System.currentTimeMillis();

    final private List<Thread> _waiters = new LinkedList<Thread>();
    private boolean _done = false;
    private Object _result = null;
    private RuntimeException _error;

    public static class FunctionAppWork extends AppWork {

        public FunctionAppWork( AppContext context , String name , JSFunction func , Object ... args  ){
            super( context , name );
            _func = func;
            _args = args;
        }
        
        public Object run(){
            final Scope s = _context.getScope().child( "work scope" );
            s.makeThreadLocal();
            try {
                return _func.call( _context.getScope() , _args );
            }
            finally {
                Scope.clearThreadLocal();
            }
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
            try {
                work.done( work.run() );
            }
            catch ( RuntimeException re ){
                work.error( re );                
            }
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
