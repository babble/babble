// JSFunction.java

package ed.js;

public abstract class JSFunction {

    public abstract void call();
    /*
    public abstract void call( Object p1 );
    public abstract void call( Object p1 , Object p2 );
    public abstract void call( Object p1 , Object p2 , Object p3 );
    public abstract void call( Object p1 , Object p2 , Object p3 , Object p4 );
    public abstract void call( Object p1 , Object p2 , Object p3 , Object p4 , Object p5 );
    */
    protected void SYSOUT( Number n ){
        System.out.println( n );
    }

    protected void SYSOUT( Object ... os ){
        for ( Object o : os )
            System.out.print( o + " " );
        System.out.println();
    }
}
