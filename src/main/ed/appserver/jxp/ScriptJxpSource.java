// ScriptJxpSource.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.appserver.jxp;

import java.io.*;
import java.util.*;
import javax.script.*;

import com.caucho.quercus.script.*;

import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.templates.*;


public class ScriptJxpSource extends JxpSource {

    static QuercusScriptEngineFactory _phpFactory = new QuercusScriptEngineFactory();

    static ScriptEngine getScriptEngine( File f ){
        if ( f.toString().endsWith( ".php" ) )
            return _phpFactory.getScriptEngine();
        throw new RuntimeException( "don't know how to dael with : " + f );
    }
    
    ScriptJxpSource( File file ){
        this( getScriptEngine( file ) , file );
    }

    ScriptJxpSource( ScriptEngine engine , File file ){
        _engine = engine;
        _file = file;
    }
    
    public JSFunction getFunction(){
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                try {
                    AppRequest ar = (AppRequest)(s.get( "__apprequest__" ));
                    JxpScriptContext context = new JxpScriptContext( ar.getRequest() , ar.getResponse() , ar );
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

    final ScriptEngine _engine;
    final File _file;
}
    
