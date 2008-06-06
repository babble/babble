// TemplateConverter.java

package ed.appserver.templates;

import java.util.*;

import ed.util.*;

public interface TemplateConverter {

    public static class Result {
        
        public Result( Template newTemplate , Map<Integer,Integer> lineMapping ){
            _newTemplate = newTemplate;
            _lineMapping = lineMapping;
        }
        
        public Template getNewTemplate(){
            return _newTemplate;
        }
        
        public Map<Integer,Integer> getLineMapping(){
            return _lineMapping;
        }

        final Template _newTemplate;
        final Map<Integer,Integer> _lineMapping;
    }

    /**
     * @return null if i can't convert this.  
     */
    public Result convert( Template t , DependencyTracker tracker );
}
