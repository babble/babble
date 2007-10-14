// Block.java

package ed.appserver.jxp;

abstract class Block {

    static enum Type { HTML , CODE , OUTPUT };

    static Block create( Type t , String raw ){
        if ( t == Type.HTML )
            return new HtmlBlock( raw );
        if ( t == Type.CODE )
            return new CodeBlock( t , raw );
        if ( t == Type.OUTPUT )
            return new CodeBlock( t , raw );
        throw new RuntimeException( "wtf" );
    }
    
    protected Block( Type t , String raw ){
        _type = t;
        _raw = raw;
    }

    String getRaw(){
        return _raw;
    }

    Type getType(){
        return _type;
    }

    public String toString(){
        return _type + " : " + _raw.toString().replaceAll( "[\\r\\n]" , " " );
    }

    final Type _type;
    final String _raw;
}
