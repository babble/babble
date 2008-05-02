// TemplateEngine.java

package ed.appserver.templates;

import java.util.*;

public class TemplateEngine {
    
    public static TemplateConverter.Result oneConvert( Template t ){
        TemplateConverter tc = _builtIns.get( t.getExtension() );
        if ( tc == null )
            return null;

        return tc.convert( t );
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
