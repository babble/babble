// ThreadPool.java

package ed.util;

import java.util.*;
import java.util.concurrent.*;

public abstract class ThreadPool<T> {
    
    public ThreadPool( String name , int numThreads ){
        this( name , numThreads , Integer.MAX_VALUE );
    }
    
    public ThreadPool( String name , int numThreads , int maxQueueSize ){
        _name = name;
        _maxThreads = numThreads;
        _threads.add( new MyThread() );
        _queue = new LinkedBlockingQueue<T>( maxQueueSize );
    }
    
    public abstract void handle( T t )
        throws Exception ;
    public abstract void handleError( T t , Exception e );

    public int queueSize(){
        return _queue.size();
    }

    public boolean offer( T t ){
        if ( _queue.size() > 0 && _threads.size() < _maxThreads )
            _threads.add( new MyThread() );
        return _queue.offer( t );
    }

    class MyThread extends Thread {
        MyThread(){
            super( "ThreadPool.MyThread:" + _name + ":" + _threads.size() );
            setDaemon( true );
            start();
        }

        public void run(){
            while ( true ){
                T t = null;

                try {
                    t = _queue.take();
                }
                catch ( InterruptedException ie ){
                }
                
                if ( t == null )
                    continue;

                try {
                    handle( t );
                }
                catch ( Exception e ){
                    handleError( t , e );
                }
            }
        }
    }

    final String _name;
    final int _maxThreads;
    final List<MyThread> _threads = new Vector<MyThread>();
    private final BlockingQueue<T> _queue;
    
}
