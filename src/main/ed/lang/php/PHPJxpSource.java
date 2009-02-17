// PHPJxpSource.java

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

package ed.lang.php;

import java.io.*;
import java.util.*;
import javax.script.*;

import com.caucho.quercus.*;
import com.caucho.quercus.env.*;
import com.caucho.quercus.page.*;
import com.caucho.quercus.parser.*;
import com.caucho.quercus.program.*;
import com.caucho.vfs.ReaderStream;
import com.caucho.vfs.WriteStream;
import com.caucho.vfs.ReaderWriterStream;


import ed.io.*;
import ed.js.*;
import ed.js.engine.*;
import ed.js.engine.Scope;
import ed.lang.*;
import ed.util.*;
import ed.appserver.*;
import ed.appserver.jxp.*;
import ed.appserver.templates.*;


public class PHPJxpSource extends JxpSource {

    public PHPJxpSource( File file ){
        _language = Language.PHP();
        _file = file;
        _quercus = new Quercus();
    }
    
    public JSFunction getFunction()
        throws IOException {
        final QuercusProgram program = getProgram();
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                try {
                    AppRequest ar = AppRequest.getThreadLocal();
                    JxpScriptContext context = new JxpScriptContext( null , ar.getRequest() , ar.getResponse() , ar );
                    
                    QuercusPage page = new InterpretedPage(_program);
                    
                    WriteStream out = (WriteStream)ar.getScope().get( "__phpwriter__" );
                    if ( out == null ){
                        ReaderWriterStream stream = new ReaderWriterStream( null , context.getWriter() );
                        out = new WriteStream(stream);
                        out.setNewlineString("\n");
                        out.setEncoding("utf-8");
                        ar.getScope().putExplicit( "__phpwriter__" , out );
                    }
                    
                    PHPEnv env = new PHPEnv( _quercus, page, out , ar );
                    env.setPwd( _getPath( _file.getParentFile() ) );

		    PHPConvertor convertor = new PHPConvertor( env );
                    context.setObjectConvertor( convertor );
		    env._convertor = convertor;
		    
                    env.setScriptContext(context);
                    env.start();

                    Value resultV = null;
                    try {
                        resultV = _program.execute(env);
                    }
                    catch ( QuercusExitException ee ){
                        // i think this is ok
                    }
                    
                    Object result = null;
                    if (resultV != null)
                        result = resultV.toJavaObject();

                    out.flush();

                    return result;
                }
                catch ( Exception e ){
                    throw new RuntimeException( "can't eval : " + _file , e );
                }
            }
        };
    }

    QuercusProgram getProgram()
        throws IOException {
        if ( _file.lastModified() <= _lastParseTime && _program != null )
            return _program;
        
        _program = QuercusParser.parse( _quercus , _getPath( _file ) , ReaderStream.open( new InputStreamReader( getInputStream() , "utf8" ) ) );
        _lastParseTime = _file.lastModified();
        return _program;
    }

    com.caucho.vfs.Path _getPath( File f ){
        return new com.caucho.vfs.FilePath( f.getAbsolutePath() );
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
    final File _file;
    final Quercus _quercus;

    long _lastParseTime = 0;
    QuercusProgram _program;

}
    
