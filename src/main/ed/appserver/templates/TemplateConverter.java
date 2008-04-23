// TemplateConverter.java


package ed.appserver.templates;

public interface TemplateConverter {

    public static class Result {
        
        Result( Template newTemplate ){
            _newTemplate = newTemplate;
        }
        
        public Template getNewTemplate(){
            return _newTemplate;
        }
        
        final Template _newTemplate;
    }

    /**
     * @return null if i can't convert this.  
     */
    public Result convert( Template t );
}
