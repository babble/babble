// InMemoryAppender.java

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

package ed.log;

import ed.js.*;
import ed.util.*;
import ed.net.httpserver.*;

public class InMemoryAppender implements Appender {

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
    
    private final CircularList<Event> _list = new CircularList<Event>( 1000 , true );
}
