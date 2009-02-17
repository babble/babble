// TemplateConverter.java

/**
*      Copyright (C) 2008 10gen Inc.
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

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
