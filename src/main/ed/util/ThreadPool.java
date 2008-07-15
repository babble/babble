// ThreadPool.java

package ed.util;

import java.util.*;
import java.util.concurrent.*;

/** @expose */
public abstract class ThreadPool<T> {

    /** Initializes a new thread pool with a given name and number of threads.
     * @param name identifying name
     * @param numThreads the number of threads allowed in the pool
     */
    public ThreadPool( String name , int numThreads ){
        this( name , numThreads , Integer.MAX_VALUE );
    }

    /** Initializes a new thread pool with a given name, number of threads, and queue size.
     * @param name identifying name
     * @param numThreads the number of threads allowed in the pool
     * @param maxQueueSize the size of the pool entry queue
     */
    public ThreadPool( String name , int numThreads , int maxQueueSize ){
        _name = name;
        _maxThreads = numThreads;
        _queue = new LinkedBlockingQueue<T>( maxQueueSize );
        _threads.add( new MyThread() );
    }

    /** Handles a given object.
     * @param t the object to handle
     * @throws Exception
     */
    public abstract void handle( T t )
        throws Exception ;

    /** Handles a given object and exception.
     * @param t the object to handle
     * @param e  the exception to handle
     */
    public abstract void handleError( T t , Exception e );

    /** Returns the size of the pool's queue.
     * @return pool size
     */
    public int queueSize(){
        return _queue.size();
    }

    /** Adds a new object to the pool, if possible.
     * @param t the object to be added
     * @return if the object was successfully added
     */
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

    /** @unexpose */
    final String _name;
    /** @unexpose */
    final int _maxThreads;
    /** @unexpose */
    final List<MyThread> _threads = new Vector<MyThread>();
    private final BlockingQueue<T> _queue;

}
