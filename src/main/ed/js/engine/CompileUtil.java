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
import java.util.jar.*;

import org.eclipse.jdt.internal.compiler.batch.*;

import ed.*;
import ed.io.*;
import ed.util.*;

public class CompileUtil {
    
    static boolean CD = false;
    
    static class MyClassLoader extends ClassLoader {
        MyClassLoader(){
        }

        public Class findClass(String name)
            throws ClassNotFoundException {
            byte[] b = loadClassData( name );
            return defineClass(name, b, 0, b.length);
        }

        File _getJarFile( String name ){
            
            String file = getCompileSrcDir( name );
            file = file.substring( 0 , file.length() - 1 );
            file = file.replaceAll( "\\$[\\d\\$]+$" , "" );
            return new File( file + ".jar" );
        }
        
        private byte[] loadClassData( String name )
            throws ClassNotFoundException {
            
            File f = _getJarFile( name );
            if ( ! f.exists() ){
                throw new ClassNotFoundException( "Jar file doesn't exist [" + f + "]" );
            }
            
            try {
                JarFile jar = new JarFile( f );
                JarEntry entry = jar.getJarEntry( name + ".class" );

                if ( entry == null )
                    throw new ClassNotFoundException( "can't find [" + name + "] in [" + f + "]" );                

                return StreamUtil.readBytesFully( jar.getInputStream( entry ) );
            }
            catch ( IOException ioe ){
                throw new ClassNotFoundException( "error loading [" + name + "] from [" + f + "]" + ioe );
            }
         }
    }

    private static final ClassLoader _loader = new MyClassLoader();
    
    public static String getCompileSrcDir( final String pack ){
        return WorkingFiles.getTmpDir() + pack.replace( '.' , '/' ) + "/";
    }
    
    public static Class<?> compile( final String pack , final String c , final String source , final Convert convert )
        throws IOException , ClassNotFoundException {
        
        final boolean D = convert.D;

        if ( CD ) System.err.println( "compile called on : " + pack + " " + c );
        if ( D ) System.out.println( source );
        
        File dir = new File( getCompileSrcDir( pack ) );
        synchronized ( _globalLock ){
            dir.mkdirs();
        }
        
        File f = new File( dir , c + ".java" );
        File jarFile = new File( dir , c + ".jar" );
        
        synchronized( jarFile.toString().intern() ){
            long depend = getDependencyLastTime();
            
            String old = null;
            if ( f.exists() )
                old = StreamUtil.readFully( new FileInputStream( f ) );
            
            final boolean oldSourceSame = source.equals( old );
            final boolean oldExists = jarFile.exists();
            final boolean oldDepends = jarFile.lastModified() > depend;
            
            boolean oldOK = oldExists && oldSourceSame && oldDepends;
            
            if ( ! oldOK ){
                _rollingLog.write( f.toString() );
                if ( CD ) System.out.println( " compiling  oldSourceSame: " + oldSourceSame + " oldExists:" + oldExists + " oldDepends:" + oldDepends + "\t" + f );
                
                _cleanOld( dir , c , false );

                FileOutputStream fout = new FileOutputStream( f );
                fout.write( source.getBytes() );
                fout.close();
                
                if ( CD ) System.err.println( "going to start compiling " );
                
                String cp = "build";
                if ( EDFinder.whereIsEd != null ){
                    cp += File.pathSeparatorChar + EDFinder.whereIsEd;
                    if ( ! EDFinder.whereIsEd.endsWith( ".jar" ) )
                        cp += "/build";

                }

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
                
                _createJar( jarFile , pack , dir , c );
                _cleanOld( dir , c , true );
            }
            
            Class result = _loader.loadClass( pack + "." + c );
            return result;
        }
    }

    static void _cleanOld( final File dir , final String prefix , final boolean onlyClassFiles  ){
        if ( ! dir.exists() )
            return;
        
        File[] toDelete = dir.listFiles( new MyFilter( prefix , onlyClassFiles ) );
        
        for ( File f : toDelete ){
            f.delete();
        }
    }

    static void _createJar( File jar , String pack , File dir , String prefix )
        throws IOException {
        File[] classFiles = dir.listFiles( new MyFilter( prefix , true ) );

        FileOutputStream fout = new FileOutputStream( jar );
        JarOutputStream jout = new JarOutputStream( fout );
        
        for ( File f : classFiles ){
            FileUtil.add( jout , pack + "." + f.getName() , f );
        }
        jout.closeEntry();
        jout.close();
        fout.close();
    }
    
    static class MyFilter implements FilenameFilter {
        MyFilter( final String prefix , final boolean onlyClassFiles ){
            _prefix = prefix;
            _onlyClassFiles = onlyClassFiles;
        }
        
        public boolean accept( final File mydir, final String name ){

            if ( ! name.startsWith( _prefix ) )
                return false;

            String remaining = name.substring( _prefix.length() );
            if ( remaining.matches( "[\\$\\d]*\\.class" ) )
                return true;
            
            if ( _onlyClassFiles )
                return false;
            
            if ( name.equals( _prefix ) )
                return true;
            
            if ( name.lastIndexOf( "." ) == _prefix.length() )
                return true;

            return false;
        }
        
        final String _prefix;
        final boolean _onlyClassFiles;
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
        "./" ,
        "../ed" ,
        "../../ed" ,
    };
    private static final String _dependsDirs[] = new String[]{ 
        "src/main/ed/js" , 
        "src/main/ed/js/engine" , 
        "src/main/ed/js/func" };

    private static final RollingNamedPipe _rollingLog = new RollingNamedPipe( "compile" );
    static {
        _rollingLog.setMessageDivider( "\n" );
    }

    static {
        
        String root = null;
        
        for ( int i=0; i<_possibleRoots.length; i++ ){
            File temp = new File( _possibleRoots[i] , _dependsDirs[0] );
            if ( temp.exists() && temp.isDirectory() ){
                root = _possibleRoots[i];
                break;
            }
        }
        
        if ( root == null && EDFinder.whereIsEd != null ){
            File temp = new File( EDFinder.whereIsEd , _dependsDirs[0] );
            if ( temp.exists() && temp.isDirectory() ){
                root = EDFinder.whereIsEd;
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
                    if ( f.getName().endsWith( ".java" ) )
                        _dependFiles.add( f );
                }
            }
        }
    }
    
    
}
