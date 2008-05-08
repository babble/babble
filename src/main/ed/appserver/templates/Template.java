// Template.java

package ed.appserver.templates;

import ed.lang.*;
import ed.appserver.*;

public class Template {

    public Template( String name , String content , Language sourceLanguage ){
        _name = name;
        _content = content;
        _extension = MimeTypes.getExtension( _name );
        _sourceLanguage = sourceLanguage;
    }
    
    public String getName(){
        return _name;
    }

    public String getExtension(){
        return _extension;
    }
    
    public String getContent(){
        return _content;
    }

    public Language getSourceLanguage(){
        return _sourceLanguage;
    }

    final String _name;
    final String _content;
    final String _extension;
    final Language _sourceLanguage;
}
