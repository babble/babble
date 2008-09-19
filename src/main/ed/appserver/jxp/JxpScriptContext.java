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
import ed.lang.*;

public class JxpScriptContext implements ScriptContext {
    
    public JxpScriptContext( ObjectConvertor convertor , Scope s ){
        _convertor = convertor;
        _scope = s;
        _writer = new OutputStreamWriter( System.out );
    }

    public JxpScriptContext( ObjectConvertor convertor , HttpRequest request , HttpResponse response , AppRequest ar ){
        _convertor = convertor;
        _scope = ar.getScope();
        _writer = ar.getServletWriter().asJavaWriter();
    }

    public Object getAttribute(String name){
        return getAttribute( name , 0 );
    }
    
    public Object getAttribute(String name, int scope){
        Object o = null;
        
        if ( name.equals( "_10gen" ) || name.equals( "_xgen" ) )
            o = _scope;
        else 
            o = _scope.get( name );
        if ( _convertor != null )
            o = _convertor.toOther( o );
        return o;
    }
    
    public int getAttributesScope(String name){
        throw new RuntimeException( "what?" );
    }
    
    public Bindings getBindings(int scope){
        return _scope;
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
        if ( _convertor != null )
            value = _convertor.toJS( value );
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
    
    public ObjectConvertor getObjectConvertor(){
        return _convertor;
    }

    public void setObjectConvertor( ObjectConvertor convertor ){
        _convertor = convertor;
    }


    final Scope _scope;
    final Writer _writer;
    ObjectConvertor _convertor;

    static final List<Integer> SCOPES;
    static {
        List<Integer> lst = new LinkedList<Integer>();
        lst.add( 0 );
        SCOPES = Collections.unmodifiableList( lst );
    }
}
