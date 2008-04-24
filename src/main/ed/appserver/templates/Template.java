// Template.java

package ed.appserver.templates;

import ed.appserver.*;

public class Template {

    public Template( String name , String content ){
        _name = name;
        _content = content;
        _extension = MimeTypes.getExtension( _name );
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

    final String _name;
    final String _content;
    final String _extension;
}
