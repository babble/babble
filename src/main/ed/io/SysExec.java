// SysExec.java

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

package ed.io;

import java.io.*;
import java.util.*;

import ed.js.*;
import ed.js.engine.*;
import ed.security.*;

public class SysExec extends ed.js.func.JSFunctionCalls4 {


    /**
     * adds quotes as needed
     */
    static String[] fix( String s ){
        String base[] = s.split( "\\s+" );
            
        List<String> fixed = new ArrayList();
        boolean changed = false;
            
        for ( int i=0; i<base.length; i++ ){

            if ( ! base[i].startsWith( "\"" ) ){
                fixed.add( base[i] );
                continue;
            }
            
            int end = i;
            while( end < base.length && ! base[end].endsWith( "\"" ) )
                end++;
            
            String foo = base[i++].substring( 1 );
            for ( ; i<=end && i < base.length; i++ )
                foo += " " + base[i];

            i--;

            if ( foo.endsWith( "\"" ) )
                foo = foo.substring( 0 , foo.length() - 1 );
            
            fixed.add( foo );
            changed = true;
        }

        if ( changed ){
            System.out.println( fixed );
            base = new String[fixed.size()];
            for ( int i=0; i<fixed.size(); i++ )
                base[i] = fixed.get(i);
        }

        return base;
    }

    public static Result exec( String cmdString ){
        return exec( cmdString , null , null , null );
    }

    public static Result exec( String cmdString , String env[] , File procDir , String toSend ){
        
        String cmd[] = fix( cmdString );

        try {
            final Process p = Runtime.getRuntime().exec( cmd , env , procDir);
            
            if ( toSend != null ){
                OutputStream out = p.getOutputStream();
                out.write( toSend.getBytes() );
                out.close();
            }
                
            final Result res = new Result();
            final IOException threadException[] = new IOException[1];
            Thread a = new Thread(){
                    public void run(){
                        try {
                            synchronized ( res ){
                                res.setErr( StreamUtil.readFully( p.getErrorStream() ) );
                            }
                        }
                        catch ( IOException e ){
                            threadException[0] = e;
                        }
                    }
                };
            a.start();
                
            synchronized( res ){
                res.setOut( StreamUtil.readFully( p.getInputStream() ) );
            }
                
            a.join();
            
            if ( threadException[0] != null )
                throw threadException[0];

            res.exitValue( p.waitFor() );

            return res;
        }
        catch ( Exception e ){
            throw new JSException( e.toString() , e );
        }
    }

    public Object call( Scope scope , Object o , Object toSendObj , Object envObj , Object pathObj , Object extra[] ){
        if ( o == null )
            return null;
            
        if ( ! Security.isCoreJS() )
            throw new JSException( "can't do sysexec from [" + Security.getTopJS() + "]" );

        File root = scope.getRoot();
        if ( root == null )
            throw new JSException( "no root" );
            
        String env[] = new String[]{};
	    
        String toSend = null;
        if ( toSendObj != null )
            toSend = toSendObj.toString();

        if ( envObj instanceof JSObject ){
            JSObject foo = (JSObject)envObj;
            env = new String[ foo.keySet().size() ];
            int pos = 0;
            for ( String name : foo.keySet() ){
                Object val = foo.get( name );
                if ( val == null )
                    val = "";
                env[pos++] = name + "=" + val.toString();
            }
        }

        File procDir = root;
	    
        if ( pathObj instanceof JSString ){

            procDir  = new File( root , pathObj.toString() );

            try {
                if (!procDir.getCanonicalPath().contains(root.getCanonicalPath())) {
                    throw new JSException("directory offset moves execution outside of root");
                }
            } catch (IOException e) {
                throw new JSException("directory offset problem", e);	            
            }	        
        }
        
        return exec( o.toString() , env , procDir , toSend );
    }        

    public static class Result extends JSObjectBase {
        void setOut( String s ){
            set( "out" , s );
        }

        void setErr( String s ){
            set( "err" , s );
        }
        
        void exitValue( int ev ){
            set( "exitValue" , ev );
        }

        public String getOut(){
            return get( "out" ).toString();
        }

        public String getErr(){
            return get( "err" ).toString();
        }

        public int exitValue(){
            return ((Number)get( "exitValue" )).intValue();
        }

        public String toString(){
            return 
                "{\n" + 
                "\t exitValue : " + exitValue() + " , \n " + 
                "\t out : " + getOut() + " , \n " + 
                "\t err : " + getErr() + " , \n " + 
                "}\n";
        }

    }
}
