// SimpleStack.java

package ed.util;

import java.util.*;

public class SimpleStack<T>{

    public void push( T t ){

        if ( _size == 0 ){
            _last = t;
            _size++;
            return;
        }
        
        if ( _extra == null )
            _extra = new LinkedList<T>();
        _extra.addLast( _last );
        _last = t;
        _size++;
    }

    public T peek(){
        return _last;
    }

    public T pop(){
        T ret = _last;
        _size--;
        if ( _size != 0 )
            _last = _extra.removeLast();
        else 
            if ( _extra != null && _extra.size() > 0 )
                throw new RuntimeException( "something is wrong" );
        return ret;
    }

    public int size(){
        return _size;
    }

    public void clear(){
        _size = 0;
        _last = null;
        if ( _extra != null )
            _extra.clear();
    }

    public T get( int i ){
        if ( i < 0 || i >= _size )
            throw new IllegalArgumentException( "out of range : " + i );
        if ( i == _size - 1 )
            return _last;
        return _extra.get(i);
    }

    private int _size = 0;
    private T _last;
    private LinkedList<T> _extra;
}
