// GitUtils.java

package ed.util;

import java.io.*;

import ed.io.*;
import ed.log.*;

public class GitUtils {

    static final Logger _log = Logger.getLogger( "git" );

    public static boolean isSourceDirectory( File dir ){
        if ( hasGit( dir ) )
            return true;

        if (!dir.isDirectory()) {
            return false;
        }
        
        for ( File t : dir.listFiles() )
            if ( t.getName().endsWith( ".js" ) )
                return true;

        return false;
    }

    public static boolean hasGit( File dir ){
        return (new File( dir , ".git" )).exists();
    }

    public static String getBranchOrTagName( File dir ){

        if ( ! dir.toString().endsWith( ".git" ) )
            dir = new File( dir , ".git" );

        if ( ! dir.exists() )
            throw new RuntimeException( dir + " does not exist" );
        
        try {
            return _getBranchOrTagName( dir );
        }
        catch ( IOException ioe ){
            // should never happen
            throw new RuntimeException( ioe );
        }
    }

    private static String _getBranchOrTagName( final File git )
        throws IOException {
        String head = StreamUtil.readFully( new File( git , "HEAD" ) ).trim();
        if ( head.startsWith( "ref: refs/heads/" ) )
            return head.substring( 16 ).trim();

        if ( head.length() == 40 ){
            String tag = _findTag( git , head );
            if ( tag != null )
                return tag;

            return head;
        }
        
        throw new RuntimeException( "dont know what to do with HEAD [" + head + "]" );
    }
    
    private static String _findTag( final File git , final String hash )
        throws IOException {
        for ( File t : (new File( git , "refs/tags/" ) ).listFiles() ){
            String tag = StreamUtil.readFully( t ).trim();
            if ( tag.equals( hash ) )
                return t.getName();

            SysExec.Result r = SysExec.exec( "git log " + t.getName() + " -n1" , null , git , null );
            
            if ( r.getOut().startsWith( "commit " ) ){
                int idx = r.getOut().indexOf( "\n" );
                final String newHash = r.getOut().substring( 7 , idx ).trim();
                if ( newHash.equals( hash ) )
                    return t.getName();
            }

        }
        return null;
    }
    
    /**
     * @param name optional
     */
    public static boolean clone( String cloneurl , File dirToCloneInto , String name ){
        _log.info( "cloning " + cloneurl + " to " + dirToCloneInto );
        SysExec.Result r = SysExec.exec( "git clone " + cloneurl + " " + ( name == null ? "" : name  ) , null , dirToCloneInto , null );
        return r.getErr().trim().length() == 0;
    }
    
    
    public static void main( String args[] )
        throws Exception {
        System.out.println( getBranchOrTagName( new File( args[0] ) ) );
    }

}
