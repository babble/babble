// CompileUtil.java

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

package ed.js.engine;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jdt.internal.compiler.batch.*;

import ed.io.*;
import ed.db.*;
import ed.util.*;

public class CompileUtil {
    
    static boolean CD = false;
    
    static final String TMP_DIR = WorkingFiles.TMP_DIR;
    private static final URLClassLoader _loader;
    static {
        
        URLClassLoader cl = null;
        try {
            File dir = new File( TMP_DIR );
            dir.mkdirs();
            cl = new URLClassLoader( new URL[]{ dir.toURL() } );
        }
        catch ( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
        _loader = cl;
    }

    public static String getCompileSrcDir( final String pack ){
        return TMP_DIR + pack.replace( '.' , '/' ) + "/";
    }
    
    public static Class<?> compile( final String pack , final String c , final String source , final Convert convert )
        throws IOException , ClassNotFoundException {
        
        final boolean D = convert.D;

        if ( CD ) System.err.println( "compile called" );
        if ( D ) System.out.println( source );
        
        File dir = new File( getCompileSrcDir( pack ) );
        synchronized ( _globalLock ){
            dir.mkdirs();
        }
        
        File f = new File( dir , c + ".java" );
        File output = new File( f.getAbsolutePath().replaceAll( "java$" , "class" ) );

        synchronized( output.toString().intern() ){
            long depend = getDependencyLastTime();
            
            String old = null;
            if ( f.exists() )
                old = StreamUtil.readFully( new FileInputStream( f ) );
            
            boolean oldOK = source.equals( old ) && output.exists() && output.lastModified() > depend;
            
            if ( ! oldOK ){
                
                FileOutputStream fout = new FileOutputStream( f );
                fout.write( source.getBytes() );
                fout.close();
                
                if ( CD ) System.err.println( "going to start compiling " );
                
                String cp = "build";
                if ( JSHook.whereIsEd != null )
                    cp += File.pathSeparatorChar + JSHook.whereIsEd + "/build";

                MyCompiler compiler = _compilerPool.get();
                boolean res = false;
                try {
                    res = compiler.compile( "-g -1.5 -classpath " + cp + " " + f.toString() );
                }
                finally {
                    _compilerPool.done( compiler );
                }
                
                if ( D ) System.out.println( f + " : " + res );
                if ( CD ) System.err.println( "done compiling " );
                
                if ( ! res ){
                    System.err.println( "**" + compiler.getLastMessage() );
                    throw new RuntimeException( compiler.getLastMessage() );
                }
                
            }
            
            return _loader.loadClass( pack + "." + c );
        }
    }

    static long getDependencyLastTime(){
        if ( _dependFiles.size() == 0 )
            return Long.MAX_VALUE;
        
        long max = 0;
        for ( File f : _dependFiles ){
            max = Math.max( max , f.lastModified() );
        }
        return max;
    }

    static class MyCompiler {
        
        MyCompiler(){
            _output = new StringWriter();
            PrintWriter pw = new PrintWriter( _output );
            _main = new Main( pw , pw , false );
        }

        boolean compile( String commandLine ){
            _output.getBuffer().setLength( 0 );
            _lastRes = _main.compile( _main.tokenize( commandLine ) );
            _lastMessage = _output.toString();
            return _lastRes;
        }
        
        boolean ok(){
            return _lastRes;
        }
        
        String getLastMessage(){
            return _lastMessage;
        }

        final StringWriter _output;
        final Main _main;

        boolean _lastRes = true;
        String _lastMessage = null;
    }
    
    private static final SimplePool<MyCompiler> _compilerPool = new SimplePool<MyCompiler>( "MyCompiler" , 4 , 4 ){
        
        public MyCompiler createNew(){
            return new MyCompiler();
        }

        public boolean ok( MyCompiler c ){
            return c.ok();
        }
    };
    private static final String _globalLock = "CompileUtil-GlobalLock-123";
    private static final Set<File> _dependFiles = new HashSet<File>();
    private static final String _possibleRoots[] = new String[]{ 
        "" ,
        "../ed" ,
        "../../ed" ,
    };
    private static final String _dependsDirs[] = new String[]{ 
        "src/main/ed/js" , 
        "src/main/ed/js/engine" , 
        "src/main/ed/js/func" };
    
    static {
        
        String root = null;
        
        for ( int i=0; i<_possibleRoots.length; i++ ){
            File temp = new File( _possibleRoots[i] , _dependsDirs[0] );
            if ( temp.exists() && temp.isDirectory() ){
                root = _possibleRoots[i];
                break;
            }
        }
        
        if ( root == null && ed.db.JSHook.whereIsEd != null ){
            File temp = new File( ed.db.JSHook.whereIsEd , _dependsDirs[0] );
            if ( temp.exists() && temp.isDirectory() ){
                root = ed.db.JSHook.whereIsEd;
            }
        }
        
        if ( root == null ){
            System.out.println( "Warning : can't find core appserver js sources : no harm, but js will be recompiled on appserver startup" );
        }
        else {
            for ( String dirName : _dependsDirs ){
                File dir = new File( root , dirName );
                
                if ( ! ( dir.exists() && dir.isDirectory() ) ){
                    System.out.println( "bad dir : " + dir );
                    _dependFiles.clear();
                    break;
                }
                
                for ( File f : dir.listFiles() ){
                    if ( ! f.getName().endsWith( ".java" ) )
                        _dependFiles.add( f );
                }
            }
        }
    }
    
    
}
