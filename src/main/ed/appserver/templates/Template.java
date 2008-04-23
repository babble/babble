// Template.java

package ed.appserver.templates;

public class Template {

    public Template( String name , String content ){
        _name = name;
        _content = content;
    }
    
    public String getName(){
        return _name;
    };
    
    public String getContent(){
        return _content;
    }

    final String _name;
    final String _content;
}
