// LogReplayer.java

package ed.db;

import java.io.*;
import java.net.*;

import org.apache.commons.cli.*;

import ed.io.*;

public class LogReplayer {

    static boolean running = true;

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
        
        String host = cl.getOptionValue( "h" , DBAddress.defaultHost() );
        int port = Integer.parseInt( cl.getOptionValue( "p" , String.valueOf( DBPort.PORT ) ) );

        System.out.println( "host:" + host );
        System.out.println( "port:" + port );

        
        final Socket sock = new Socket( host , port );
        
        // utilty thread to read responses from db.  We don't care what they say.
        
        Thread t = new Thread(){
                public void run(){
                    byte buf[] = new byte[1024*128];
                    try {
                        InputStream in = sock.getInputStream();
                        while ( running  ){
                            in.read( buf );
                            System.out.print( "r" );
                        }
                        System.out.println("running false = ending");
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

        	File f = new File(obj.toString());
        	long size = f.length();
        	
        	System.out.println("==========  Replaying file : " + obj.toString() + " size = " + size + " ===========");        	
            LogReplayer.noisyPipe( new FileInputStream( obj.toString() ) , out, -1, size);
        	System.out.println("==========  Done with file : " + obj.toString() + " ===========");
        }

        System.out.println("Done with sending all data.  Telling reader thread to stop...");
        
        running = false;
        t.join();
        
        System.out.println("Done w/ reader thread - finishing read to keep db happy");

        byte buf[] = new byte[128];
        InputStream in = sock.getInputStream();
        while ( in.read(buf) != -1  ){
            in.read( buf );
            System.out.print( "R" );
        }
    }
    
    public static int noisyPipe( InputStream is , OutputStream out , int maxSize, long totalSize)
        throws IOException {
        byte buf[] = new byte [4096];
        int len = -1;
        int total = 0;
        int count = 0;
        while ((len = is.read(buf)) != -1){
            out.write(buf, 0, len); 
            System.out.print("w");
            total += len;
            if ( maxSize > 0 && total > maxSize ) {
                throw new IOException("too big");
            }
            
            if (count++ % 200 == 0) {
            	System.out.println("replayed " + total + " bytes out of " + totalSize + " : " +  (int) (1.0 * total / totalSize *100) + "%");
            }
        }
        return total;
    }

}
