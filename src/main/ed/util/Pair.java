// Pair.java

package ed.util;

/** @expose */
public class Pair<A,B>{

    /** Initializes a pair. */
    public Pair(){
    }

    /** Initializes a pair of objects.
     * @param a First object
     * @param b Second object
     */
    public Pair( A a , B b ){
        first = a;
        second = b;
    }

    /** Find the hash code for the pair.
     * @return the hash code
     */
    public int hashCode(){
        return
            ( first == null ? 0 : first.hashCode() ) +
            ( second == null ? 0 : second.hashCode() );
    }

    /** Figures out whether a given object is equivalent to this pair.
     * @param o object to compare
     * @return if the objects are equal
     */
    public boolean equals( Object o ){
        if ( ! ( o instanceof Pair ) )
            return false;

        Pair other = (Pair)o;
        return _equals( first , other.first ) && _equals( second , other.second );
    }

    /** @unexpose  */
    private final boolean _equals( Object o1 , Object o2 ){
        if ( o1 == null )
            return o2 == null;
        if ( o2 == null )
            return false;

        return o1.equals( o2 );
    }

    /** First object of the pair. */
    public A first;
    /** First object of the pair. */
    public B second;
}
