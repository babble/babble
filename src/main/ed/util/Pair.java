// Pair.java

package ed.util;

public class Pair<A,B>{
    public Pair( A a , B b ){
        first = a;
        second = b;
    }

    public int hashCode(){
        return 
            ( first == null ? 0 : first.hashCode() ) + 
            ( second == null ? 0 : second.hashCode() );
    }

    public boolean equals( Object o ){
        if ( ! ( o instanceof Pair ) )
            return false;

        Pair other = (Pair)o;
        return _equals( first , other.first ) && _equals( second , other.second );
    }

    private final boolean _equals( Object o1 , Object o2 ){
        if ( o1 == null )
            return o2 == null;
        if ( o2 == null )
            return false;
        
        return o1.equals( o2 );
    }

    public A first;
    public B second;
}
