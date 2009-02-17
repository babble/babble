// ScriptJxpSource.java

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

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import javax.script.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.templates.*;


public class ScriptJxpSource extends JxpSource {

    ScriptJxpSource( File file ){
        this( Language.find( file.getName() , true ) , file );
    }

    ScriptJxpSource( Language lang , File file ){
        _language = lang;
        if ( ! _language.isScriptable() )
            throw new RuntimeException( lang + " is not scriptable!" );
        
        _engine = lang.getScriptEngine();
        _file = file;
    }
    
    public JSFunction getFunction(){
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                try {
                    AppRequest ar = (AppRequest)(s.get( "__apprequest__" ));
                    JxpScriptContext context = new JxpScriptContext( _language.getObjectConvertor() , ar.getRequest() , ar.getResponse() , ar );
                    
                    return _engine.eval( new InputStreamReader( getInputStream() ) , context );
                }
                catch ( Exception e ){
                    throw new RuntimeException( "can't eval : " + _file , e );
                }
            }
        };
    }

    public String getName(){
        return _file.toString();
    }
    
    protected String getContent()
        throws IOException {
        return StreamUtil.readFully( _file );
    }
    
    protected InputStream getInputStream()
        throws IOException {
        return new FileInputStream( _file );
    }
    
    public long lastUpdated(Set<Dependency> visitedDeps){
        return _file.lastModified();
    }
    
    public File getFile(){
        return _file;
    }
    
    final Language _language;
    final ScriptEngine _engine;
    final File _file;
}
    
