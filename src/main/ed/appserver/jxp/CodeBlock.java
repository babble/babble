// CodeBlock.java

package ed.appserver.jxp;

class CodeBlock extends Block {

    CodeBlock( Type t , String raw , int lineno ){
        super( t , raw , lineno );
        
        if ( t != Type.CODE &&
             t != Type.OUTPUT )
            throw new RuntimeException( "wtf" );
    }
    

}
