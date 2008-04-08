// Zeus.java

package ed.cloud;

import java.io.*;

public class Zeus {
    
    public static String generateResolveTS(){
        Cloud c = Cloud.getInstance();
        return c.evalFunc( "Cloud.Zeus.resolveTS" ).toString();
    }

    public static void main( String args[] )
        throws IOException {

        String ts = generateResolveTS();
        System.out.println( ts );
        
        FileOutputStream fout = new FileOutputStream( "zeus.out" );
        fout.write( ts.getBytes() );
        
    }

}
