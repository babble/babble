// InMemoryAppender.java

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

package ed.log;

import ed.js.*;
import ed.util.*;
import ed.net.httpserver.*;

public class InMemoryAppender implements Appender , Sizable {

    static final InMemoryAppender INSTANCE = new InMemoryAppender();
    public static InMemoryAppender getInstance(){
        return INSTANCE;
    }

    private InMemoryAppender(){
    }

    public void append( Event e ){
        _list.add( e );
    }

    public CircularList<Event> getRecent(){
        return _list;
    }

    public long approxSize( SeenPath seen ){
        long size = 0;
        for ( int i=0; i<_list.size(); i++ )
            size += _list.get( i ).approxSize( seen );
        return size;
    }
    
    private final CircularList<Event> _list = new CircularList<Event>( 1000 , true );
}
