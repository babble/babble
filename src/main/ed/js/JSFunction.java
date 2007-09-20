// JSFunction.java

package ed.js;

public abstract class JSFunction extends JSInternalFunctions {

    public JSFunction(){
    }

    public JSFunction( String name ){
        _name = name;
    }

    public abstract Object call();
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
    
    public void setName( String name ){
        _name = name;
    }

    public String toString(){
        return "JSFunction : " + _name;
    }

    String _name = "NO NAME SET";
}
