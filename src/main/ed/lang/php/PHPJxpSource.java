// PHPJxpSource.java

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
        _language = Language.PHP;
        _file = file;
        _quercus = new Quercus();
    }
    
    public JSFunction getFunction()
        throws IOException {
        final QuercusProgram program = getProgram();
        
        return new ed.js.func.JSFunctionCalls0(){
            public Object call( Scope s , Object extra[] ){
                try {
                    AppRequest ar = (AppRequest)(s.get( "__apprequest__" ));
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
                    env.setPwd( new com.caucho.vfs.FilePath( s.getRoot().getAbsolutePath() ) );

		    PHPConvertor convertor = new PHPConvertor( env );
                    context.setObjectConvertor( convertor );
		    env._convertor = convertor;
		    
                    env.setScriptContext(context);
                    env.start();

                    Value resultV = _program.execute(env);
                    
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
        
        _program = QuercusParser.parse( _quercus , null , ReaderStream.open( new InputStreamReader( getInputStream() ) ) );
        _lastParseTime = _file.lastModified();
        return _program;
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
    
