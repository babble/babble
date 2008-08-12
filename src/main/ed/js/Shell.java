// Shell.java

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

package ed.js;

import java.io.*;
import java.util.*;

import jline.*;

import ed.db.*;
import ed.io.*;
import ed.lang.*;
import ed.lang.python.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.appserver.*;
import ed.appserver.templates.*;
import ed.appserver.templates.djang10.Djang10Source;

/** The shell is a handy tool for testing and debugging. To run, go to
 * ed and type ./runAnt.bash ed.js.Shell.
 * Keywords: <dl>
 * <dt>core</dt><dd>Refers to the core library</dd>
 * <dt>external</dt><dd>Refers to the external libraries</dd>
 * <dt>local</dt><dd>Refers to ed</dd>
 * <dt>connect</dt><dd>Can be used to connect to a database: <tt>db = connect("mydb")</tt></dd>
 * <dt>openFile</dt><dd>Open a local file</dd>
 * <dt>exit</dt><dd>Exit the shell</dd>
 * <dt>scopeWithRoot</dt><dd></dd>
 * </dl>
 * @expose
 */
public class Shell {

    /** @unexpose */
    static final PrintStream _originalPrintStream = System.out;

    /** @unexpose */
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

    /** @unexpose */
    public final static PrintStream _myPrintStream = new PrintStream( _myOutputStream );

    /** Connect to a database. */
    public static class ConnectDB extends JSFunctionCalls2 {
        public Object call( Scope s , Object name , Object ip , Object crap[] ){
            return DBProvider.get( name.toString() , ip == null ? null : ip.toString() );
        }

    }

    /** Adds methods and libraries described in the class summary above.
     * @param s Scope to use for these commands.
     */
    public static void addNiceShellStuff( Scope s ){

        s.put( "core" , CoreJS.get().getLibrary( null , null , s , false ) , true );
        s.put( "external" , Module.getModule( "external" ).getLibrary( null , null , s , false ) , true );
        s.put( "local" , new JSFileLibrary( new File( "." ) ,  "local" , s ) , true );

        s.put( "connect" , new ConnectDB() , true );

        s.put( "openFile" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return new JSLocalFile( fileName.toString() );
                }
            } , true );

        s.put( "exit" , new JSFunctionCalls1(){
                public Object call( Scope s , Object code , Object crap[] ){
                    System.exit( code instanceof Number ? ((Number)code).intValue() : 0 );
                    return null;
                }
            } , true );

        s.put( "log" , ed.log.Logger.getLogger( "shell" ) ,true );
        s.put( "scopeWithRoot" , new JSFunctionCalls1(){
                public Object call( Scope s , Object fileName , Object crap[] ){
                    return s.child(new File(fileName.toString()));
                }
            } , true);

        Djang10Source.install(s);
    }

    /** @unexpose */
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
                data = data.substring( data.indexOf( "\n" ) );

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

            if ( a.endsWith( ".js" ) ){
                File f = new File( a );
                JSFileLibrary fl = new JSFileLibrary( f.getParentFile() == null ? new File( "." ) : f.getParentFile()  , "blah" , s );
                try {
                    ((JSFunction)(fl.get( f.getName().replaceAll( ".js$" , "" ) ))).call( s );
                }
                catch ( Exception e ){
                    StackTraceHolder.getInstance().fix( e );
                    e.printStackTrace();
                    return;
                }
            }
            else if ( a.endsWith( ".py" ) ){
                PythonJxpSource py = new PythonJxpSource( new File( a ) , ((JSFileLibrary)(s.get( "local" ) ) ) );
                try {
                    py.getFunction().call( s );
                }
                catch ( Exception e ){
                    StackTraceHolder.getInstance().fix( e );
                    e.printStackTrace();
                    return;
                }
            }
            else {
                Template t = new Template( a , StreamUtil.readFully( new FileInputStream( a ) ) , Language.find( a ) );
                while ( ! t.getExtension().equals( "js" ) ){
                    TemplateConverter.Result r = TemplateEngine.oneConvert( t , null );
                    if ( r == null )
                        throw new RuntimeException( "can't convert : " + t.getName() );
                    t = r.getNewTemplate();
                }
                try {
                    s.eval( t.getContent() , a );
                }
                catch ( Exception e ){
                    StackTraceHolder.getInstance().fix( e );
                    e.printStackTrace();
                    return;
                }
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
                if ( hasReturn[0] ){
                    if ( res instanceof DBCursor )
                        ed.db.Shell.displayCursor( System.out , (DBCursor)res );
                    else
                        System.out.println( JSON.serialize( res ) );
                }
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
