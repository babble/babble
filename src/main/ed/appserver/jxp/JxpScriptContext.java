// JxpScriptContext.java

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

import ed.js.*;
import ed.js.engine.*;
import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpScriptContext implements ScriptContext {
    
    public JxpScriptContext( HttpRequest request , HttpResponse response , AppRequest ar ){
        _request = request;
        _response = response;
        _ar = ar;
        _scope = ar.getScope();
        _servletWriter = new ServletWriter( response.getWriter() , ar.getURLFixer() );
        _writer = _servletWriter.asJavaWriter();
    }

    public Object getAttribute(String name){
        return _scope.get( name );
    }
    
    public Object getAttribute(String name, int scope){
        return _scope.get( name );
    }
    
    public int getAttributesScope(String name){
        throw new RuntimeException( "what?" );
    }
    
    public Bindings getBindings(int scope){
        throw new RuntimeException( "what?" );
    }
    
    public Writer getErrorWriter(){
        return new OutputStreamWriter( System.err );
    }
    
    public Reader getReader(){
        return null;
    }
    
    public List<Integer> getScopes(){
        return SCOPES;
    }
    
    public Writer getWriter(){
        return _writer;
    }
    
    public Object removeAttribute(String name, int scope){
        throw new RuntimeException( "not done" ); 
    }
    
    public void setAttribute(String name, Object value, int scope){
        _scope.put( name , value , false );
    }

    public void setBindings(Bindings bindings, int scope){
        throw new RuntimeException( "not done" ); 
    }
    
    public void setErrorWriter(Writer writer){
        throw new RuntimeException( "you can't change the ErrorWriter" ); 
    }
    
    public void setReader(Reader reader){
        throw new RuntimeException( "you can't change the reader" ); 
    }
    
    public void setWriter(Writer writer){
        throw new RuntimeException( "you can't change the writer" ); 
    }
    

    final HttpRequest _request;
    final HttpResponse _response;
    final AppRequest _ar;
    final Scope _scope;
    final ServletWriter _servletWriter;
    final Writer _writer;

    static final List<Integer> SCOPES;
    static {
        List<Integer> lst = new LinkedList<Integer>();
        lst.add( 0 );
        SCOPES = Collections.unmodifiableList( lst );
    }
}
