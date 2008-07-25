// PYJSFunctionWrapper.java

package ed.lang.python;

import org.python.core.*;

import ed.js.*;
import static ed.lang.python.Python.*;


public class PYJSFunctionWrapper extends PYJSObjectWrapper {

    public PYJSFunctionWrapper( JSFunction func ){
        super( func );
        _func = func;
    }
    
    public PyObject __call__(PyObject args[], String keywords[]) {

        if ( keywords != null && keywords.length > 0 )
            throw new RuntimeException( "what are keywords here..." );
        
        Object extra[] = new Object[ args == null ? 0 : args.length ];
        for ( int i=0; i<extra.length; i++ )
            extra[i] = toJS( args[i] );
        
        // TODO: not sure what to do about scope yet, but its not this probably
        return toPython( _func.call( _func.getScope() , extra ) );
    }

    final JSFunction _func;
}
