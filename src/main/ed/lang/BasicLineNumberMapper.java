// BasicLineNumberMapper.java

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

package ed.lang;

import java.util.*;

public class BasicLineNumberMapper implements StackTraceFixer {

    public BasicLineNumberMapper( String source , String generated , Map<Integer,Integer> mapping ){
        _source = source;
        _generated = generated;
        _mapping = mapping;
    }

    public StackTraceElement fixSTElement( StackTraceElement element ){
        if ( element == null || _mapping == null )
            return element;
        
        if ( ! ( element.getClassName().equals( _generated ) ||
                 element.getFileName().equals( _generated ) ) )
            return element;

        
        Integer line = _mapping.get( element.getLineNumber() );
        if ( line == null )
            return element;

        return new StackTraceElement( _source , "___" , _source , line );
    }

    public boolean removeSTElement( StackTraceElement element ){
        return false;
    }

    public String toString(){
        String s = "{" + _source + " -> " + _generated + " ";
        if ( _mapping instanceof TreeMap && _mapping.size() > 0 ){
            TreeMap tm = (TreeMap)_mapping;
            s += tm.firstKey() + " -> " + tm.lastKey();
        }
        s += " }";
        return s;
    }

    final String _source;
    final String _generated;
    final Map<Integer,Integer> _mapping;
}
