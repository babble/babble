// LicenseHeaderCheck.java

package ed.util;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ed.io.*;

public class LicenseHeaderCheck {
    public LicenseHeaderCheck( File headerFile , boolean recursive )
        throws IOException {
        _headerFile = headerFile;
        _headerRaw = StreamUtil.readFully( headerFile );
        _headerLines = _headerRaw.split( "\r?\n" );
        _recursive = recursive;
    }

    public void go( File f )
        throws IOException {

        if ( ! f.exists() )
            return;


        if ( f.isDirectory() ){

            for ( File child : f.listFiles() ){


                if ( ! _recursive && child.isDirectory() )
                    continue;
                
                if ( skip( f ) )
                    continue;
                
                go( child );
            }

            return;
        }
        
        if ( skip( f ) )
            return;

        doLicense( f );
    }

    boolean skip( File f ){
        
        if ( f.toString().contains( "/.git" ) )
            return true;

        final String name = f.getName();

        if ( name.endsWith( "~" ) )
            return true;
        
        if ( ! Character.isLetterOrDigit( name.charAt(0) ) )
            return true;

        if ( name.toLowerCase().startsWith( "makefile" ) 
             || name.toLowerCase().startsWith( "oplog" )
             )
            return true;

        if ( name.contains( "." ) ){
            final String ext = ed.appserver.MimeTypes.getExtension( f );
            if ( _skipExtensions.contains( ext ) )
                return true;
        }

        return false;
    }

    void doLicense( File f )
        throws IOException {
        
        final CodeType type = getCodeType( f );
        if ( type == null )
            throw new RuntimeException( "can't handle files lke : " + f );
        
        final String raw = StreamUtil.readFully( ( new FileInputStream( f ) ) );
        final String lines[] = raw.split( "\r?\n" );
        if ( ( raw.length() / lines.length ) > 150 )
            throw new RuntimeException( "Something is wrong on : " + f );
        
        int start = 0;
        for ( ; start < lines.length; start++ ){
            if ( ! lines[start].startsWith( "//" ) )
                break;
        }
        
        for ( ; start < lines.length; start++ ){
            if ( lines[start].trim().length() == 0 )
                continue;
            
            break;
        }
        
        if ( lines[start].startsWith( type._start ) && 
             ( lines[start].contains( "Copyright"  ) || 
               lines[start+1].contains( "Copyright" ) || 
               lines[start+2].contains( "Copyright" ) )
             ){
            // TODO : all this means is that their is a Copyright.
            //        need to make sure its correct
            return;
        }

        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<start; i++ )
            buf.append( lines[i] ).append( "\n" );
        
        buf.append( type._start ).append( "\n" );
        for ( int j=0; j<_headerLines.length; j++ )
            buf.append( type._eachLine ).append( _headerLines[j] ).append( "\n" );
        buf.append( type._end ).append( "\n\n" );

        for ( int i=start; i<lines.length; i++ )
            buf.append( lines[i] ).append( "\n" );

        System.out.println( f + " : " );

        FileOutputStream out = new FileOutputStream( f );
        out.write( buf.toString().getBytes() );
        out.close();
    }

    static CodeType getCodeType( File f ){
        return getCodeType( ed.appserver.MimeTypes.getExtension( f ) );
    }
    
    static CodeType getCodeType( String extension ){
        return _extensions.get( extension.toLowerCase() );
    }
    
    final File _headerFile;
    final String _headerRaw;
    final String _headerLines[];
    final boolean _recursive;

    static class CodeType {
        CodeType( String start , String end , String eachLine ){
            _start = start;
            _end = end;
            _eachLine = eachLine;
        }

        final String _start;
        final String _end;
        final String _eachLine;
    }
    
    private static final Map<String,CodeType> _extensions = new HashMap<String,CodeType>();
    static {
        CodeType cStyle = new CodeType( "/**" , "*/" , "*" );
        _extensions.put( "java" , cStyle );
        _extensions.put( "cpp" , cStyle );
        _extensions.put( "js" , cStyle );
        _extensions.put( "h" , cStyle );
        _extensions.put( "css" , cStyle );

        CodeType jxpStyle = new CodeType( "<% /**" , "*/ %>" , "*" );
        _extensions.put( "jxp" , jxpStyle );
        _extensions.put( "html" , jxpStyle );
    }
    
    private static final Set<String> _skipExtensions = new HashSet<String>();
    static {

        // visual studio crap
        _skipExtensions.add( "rc" );
        _skipExtensions.add( "sln" );
        _skipExtensions.add( "vcproj" );
        _skipExtensions.add( "user" );
        _skipExtensions.add( "gch" );

        // intermediate files
        _skipExtensions.add( "o" );
        _skipExtensions.add( "class" );
        _skipExtensions.add( "a" );

        // text files
        _skipExtensions.add( "log" );
        _skipExtensions.add( "txt" );

        // zip
        _skipExtensions.add( "gz" );
        _skipExtensions.add( "tar" );
        _skipExtensions.add( "zip" );

        // meadia
        _skipExtensions.add( "jpg" );
        _skipExtensions.add( "gif" );
        _skipExtensions.add( "png" );
        _skipExtensions.add( "ico" );


    }

    public static void main( String args[] )
        throws Exception {
        
        Options o = new Options();
        o.addOption( "r" , false , "recursive" );
        
        CommandLine cl = ( new BasicParser() ).parse( o , args );
        
        if ( cl.getArgList().size() < 2 ){
            System.err.println( "usage: LicenseHeaderCheck [-r] <header file> <dir or files>" );
            return;
        }
        
        LicenseHeaderCheck checker = new LicenseHeaderCheck( new File( cl.getArgList().get(0).toString() ) , cl.hasOption( "r" ) );
        for ( int i=1; i<cl.getArgList().size(); i++){
            checker.go( new File( cl.getArgList().get(i).toString() ) );
        }      
    }
}
