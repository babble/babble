// LogReplayer.java

package ed.db;

import java.io.*;
import java.net.*;

import org.apache.commons.cli.*;

import ed.io.*;

public class LogReplayer {

    public static void main( String args[] )
        throws Exception {

        Options o = new Options();
        o.addOption( "h" , true , "host" );
        o.addOption( "p" , true , "port" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        if ( cl.getArgList().size() == 0 ){
            System.out.println( o );
            return;
        }
        
        String host = cl.getOptionValue( "h" , DBProvider.getDefaultHost() );
        int port = Integer.parseInt( cl.getOptionValue( "p" , String.valueOf( DBPort.PORT ) ) );

        System.out.println( "host:" + host );
        System.out.println( "port:" + port );

        final Socket sock = new Socket( host , port );
        Thread t = new Thread(){
                public void run(){
                    byte buf[] = new byte[1024*128];
                    try {
                        InputStream in = sock.getInputStream();
                        while ( true ){
                            in.read( buf );
                            System.out.print( "." );
                        }
                    }
                    catch ( Exception e ){
                        e.printStackTrace();
                    }
                }
            };
        t.setDaemon( true );
        t.start();
        
        OutputStream out = sock.getOutputStream();

        for ( Object obj : cl.getArgList() ){
            StreamUtil.pipe( new FileInputStream( obj.toString() ) , out );
        }
        
    }
}
