// SimpleStack.java

package ed.util;

import java.util.*;

public class SimpleStack<T> extends LinkedList<T>{

    public void push( T t ){
	addLast( t );
    }

    public T peek(){
	return getLast();
    }

    public T pop(){
	return removeLast();
    }

}
