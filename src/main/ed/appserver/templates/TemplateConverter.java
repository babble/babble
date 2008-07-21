// TemplateConverter.java

/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
