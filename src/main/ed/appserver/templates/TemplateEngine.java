// TemplateEngine.java

package ed.appserver.templates;

import java.io.*;
import java.util.*;

import ed.util.*;

public class TemplateEngine {
    
    static final File tmp = new File( "/tmp/jxp/templates/" );
    static final boolean OUTPUT = true;

    public static TemplateConverter.Result oneConvert( Template t , DependencyTracker tracker ){
        TemplateConverter tc = _builtIns.get( t.getExtension() );
        if ( tc == null )
            return null;
        
        try {
            final TemplateConverter.Result r = tc.convert( t , tracker );
            if ( OUTPUT ){
                try {
                    File f = new File( tmp , r.getNewTemplate().getName() );
                    f.getParentFile().mkdirs();
                    FileOutputStream fout = new FileOutputStream( f );
                    fout.write( r.getNewTemplate().getContent().getBytes() );
                    fout.close();
                }
                catch ( IOException ioe ){
                    ioe.printStackTrace();
                }
            }
                
            return r;
        }
        catch ( RuntimeException re ){
            if ( re.toString().contains( t.getName() ) )
                throw re;
            throw new RuntimeException( t.getClass() + " failed to convert [" + t.getName() + "] b/c of " + re , re );
        }
    }
    
    private static final Map<String,TemplateConverter> _builtIns = new TreeMap<String,TemplateConverter>();
    static {
        _builtIns.put( "jxp" , new JxpConverter() );
        _builtIns.put( "html" , new JxpConverter( true ) );

        _builtIns.put( "djang10" , new Djang10Converter() );

        _builtIns.put( "rb" , new ed.lang.ruby.RubyConvert.TemplateImpl() );
        _builtIns.put( "erb" , new RubyTemplateConverter( "erb" ) );
        _builtIns.put( "rhtml" , new RubyTemplateConverter( "rhtml" ) );
        _builtIns.put( "txt" , new RubyTemplateConverter("txt"));
    } 
}
