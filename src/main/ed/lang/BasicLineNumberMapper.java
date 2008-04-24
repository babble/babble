// BasicLineNumberMapper.java

package ed.lang;

import java.util.*;

public class BasicLineNumberMapper implements StackTraceFixer {

    public BasicLineNumberMapper( String source , String generated , Map<Integer,Integer> mapping ){
        _source = source;
        _generated = generated;
        _mapping = mapping;
    }

    public StackTraceElement fixSTElement( StackTraceElement element ){
        if ( ! ( element.getClassName().equals( _generated ) ||
                 element.getFileName().equals( _generated ) ) )
            return element;

        return new StackTraceElement( _source , "___" , _source , _mapping.get( element.getLineNumber() ) );
    }

    public boolean removeSTElement( StackTraceElement element ){
        return false;
    }

    final String _source;
    final String _generated;
    final Map<Integer,Integer> _mapping;
}

