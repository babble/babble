// JSCompileException.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package ed.js.engine;

import java.util.*;
import java.util.regex.*;

import ed.ext.org.mozilla.javascript.*;

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
