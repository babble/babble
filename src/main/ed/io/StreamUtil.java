// StreamUtil.java

package ed.io;

import java.io.*;
import java.util.*;

public class StreamUtil {

    public static String[] execSafe( String command ){
        try {
            return exec( command );
        }
        catch ( IOException ioe ){
            throw new RuntimeException( ioe );
        }
    }

    public static String[] exec( String command )
        throws IOException {
        Process p = Runtime.getRuntime().exec( command );
        String out = StreamUtil.readFully( p.getInputStream() );
        String err = StreamUtil.readFully( p.getErrorStream() );
        p.destroy();
        return new String[]{ out , err };
    }

    public static String readFully( File f )
        throws IOException {
        return readFully( f , null );
    }
    
    public static String readFully( File f , String encoding )
        throws IOException {
        FileInputStream fin = new FileInputStream( f );
        String s = readFully( fin , encoding );
        fin.close();
        return s;
    }

    public static String readFully(InputStream is) 
        throws IOException {
        return readFully( is , null );
    }

    public static String readFully(InputStream is , String encoding ) 
        throws IOException {
        return readFully( encoding == null ? 
                          new InputStreamReader( is ) : 
                          new InputStreamReader( is , encoding ) );
    }

    public static String readFully(InputStreamReader isr) 
        throws IOException {
        return readFully(new BufferedReader(isr));
    }
    
    public static String readFully(BufferedReader br) 
        throws IOException {
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }
        return buf.toString();
    }

    public static byte[] readBytesFully( InputStream is )
        throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipe( is , baos );
        return baos.toByteArray();
    }
    
    public static int pipe( InputStream is , OutputStream out )
        throws IOException {
        return pipe( is , out , -1 );
    }
    
    public static int pipe( InputStream is , OutputStream out , int maxSize )
        throws IOException {
        byte buf[] = new byte [4096];
        int len = -1;
        int total = 0;
        while ((len = is.read(buf)) != -1){
            out.write(buf, 0, len); 
            total += len;
            if ( maxSize > 0 && total > maxSize )
                throw new IOException("too big");
        }
        return total;
    }

    public static int send( byte[] b , OutputStream out )
        throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream( b );
        return pipe( in , out );
    }
    
    public static byte readByte( InputStream in )
        throws IOException {
        int b = in.read();
        if ( b < 0 )
            throw new IOException("end of stream");
        return (byte)( b & 0x000000ff );
    }
    
    public static char readChar( InputStream in )
        throws IOException {
        return (char)readByte( in );
    }

}
