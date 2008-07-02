// JSCompileException.java

package ed.js.engine;

import java.util.*;
import java.util.regex.*;

import org.mozilla.javascript.*;

import ed.lang.*;

public class JSCompileException extends RuntimeException {

    
    static JSCompileException create( EvaluatorException ee ){

        String s = ee.getMessage();
        Pattern p = Pattern.compile( "^.*\\(([\\w/\\\\.]+)#(\\d+)\\)$" );
        Matcher m = p.matcher( s );
        
        if ( ! m.find() )
            return new JSCompileException( s , "" , -1 );
        
        StackTraceElement broken = new StackTraceElement( m.group(1) , "---" , m.group(1) , Integer.parseInt( m.group(2) ) );
        StackTraceElement fixed = StackTraceHolder.getInstance().fix( broken );
        
        System.out.println( s );

        return new JSCompileException( s.substring( 0 , m.start(1) - 1 ).trim() , fixed.getFileName() , fixed.getLineNumber() );
    }
    
    JSCompileException( String msg , String file , int line ){
        super( msg + " (" + file + "#" + line + ")" );
        _msg = msg;
        _file = file;
        _line = line;
    }

    public String getError(){
        return _msg;
    }

    public String getFileName(){
        return _file;
    }

    public int getLineNumber(){
        return _line;
    }
    
    final String _msg;
    final String _file;
    final int _line;
}
