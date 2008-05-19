// Shell.java

package ed.js;

import java.io.*;
import java.util.*;

import jline.*;

import ed.db.*;
import ed.io.*;
import ed.lang.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.*;
import ed.appserver.templates.Djang10Converter;

public class Shell {
    
    static final PrintStream _originalPrintStream = System.out;

    final static OutputStream _myOutputStream = new OutputStream(){
            
            public void write( byte b[] , int off , int len ){
                RuntimeException re = new RuntimeException();
                re.fillInStackTrace();
                re.printStackTrace();
                _originalPrintStream.write( b , off , len );
            }
            
            public void write( int b ){
                _originalPrintStream.write( b );
                throw new RuntimeException("sad" );
            }
        };

    public final static PrintStream _myPrintStream = new PrintStream( _myOutputStream );

    public static void addNiceShellStuff( Scope s ){

        ed.db.migrate.Drivers.init( s );

        s.put( "core" , new CoreJS( s ) , true );
        s.put( "external" , new JSFileLibrary( new File( "/data/external" ) ,  "external" , s ) , true );
        s.put( "local" , new JSFileLibrary( new File( "." ) ,  "local" , s ) , true );

        s.put( "connect" , new JSFunctionCalls2(){
                public Object call( Scope s , Object name , Object ip , Object crap[] ){
                    return DBProvider.get( name.toString() , ip == null ? null : ip.toString() );
                }
                
                Map<String,DBJni> _dbs = new HashMap<String,DBJni>();
            } , true  );
        
        s.put( "openFile" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return new JSLocalFile( fileName.toString() );
                }
            } , true );
        
        s.put( "exit" , new JSFunctionCalls0(){
                public Object call( Scope s , Object crap[] ){
                    System.exit(0);
                    return null;
                }
            } , true );
        
        s.put( "log" , ed.log.Logger.getLogger( "shell" ) ,true );
        s.put( "scopeWithRoot" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return s.child(new File(fileName.toString()));
                }
            } , true);
        
        Djang10Converter.injectHelpers(s);
    }
    
    public static void main( String args[] )
        throws Exception {
        
        System.setProperty( "NO-SECURITY" , "true" );
        
        Scope s = Scope.newGlobal().child( new File("." ) );
        s.makeThreadLocal();

        addNiceShellStuff( s );

        File init = new File( System.getenv( "HOME" ) + "/.init.js" );
        
        if ( init.exists() )
            s.eval( init );
        
        if ( args.length > 0 && args[0].equals( "-shell" ) ){

            String data = StreamUtil.readFully( new FileInputStream( args[1] ) );

            if ( data.startsWith( "#!" ) )
                data = data.substring( data.indexOf( "\n" ) + 1);
            
            JSFunction func = Convert.makeAnon( data );
            Object jsArgs[] = new Object[ args.length - 2 ];
            for ( int i=0; i<jsArgs.length; i++ )
                jsArgs[i] = args[i+2];
            func.call( s , jsArgs );
            return;
        }

        boolean exit = false;

        for ( String a : args ){
            if ( a.equals( "-exit" ) ){
                exit = true;
                continue;
            }

            File temp = new File( a );
            try {
                s.eval( temp );
            }
            catch ( Exception e ){
                StackTraceHolder.getInstance().fix( e );
                e.printStackTrace();
                return;
            }

        }

        if ( exit )
            return;

        String line;
        ConsoleReader console = new ConsoleReader();
        console.setHistory( new History( new File( ".jsshell" ) ) );
        
        boolean hasReturn[] = new boolean[1];
        
        while ( ( line = console.readLine( "> " ) ) != null ){
            line = line.trim();
            if ( line.length() == 0 )
                continue;
            
            if ( line.equals( "exit" ) ){
                System.out.println( "bye" );
                break;
            }

            try {
                Object res = s.eval( line , "lastline" , hasReturn );
                if ( hasReturn[0] )
                    System.out.println( JSON.serialize( res ) );
            }
            catch ( Exception e ){
                if ( JS.RAW_EXCPETIONS )
                    e.printStackTrace();
                StackTraceHolder.getInstance().fix( e );
                e.printStackTrace();
                System.out.println();
            }
        }
    }
}
