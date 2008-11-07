// Call.java

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

package ed.net.nioclient;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import ed.io.*;
import ed.log.*;
import ed.util.*;
import ed.net.*;
import ed.net.httpserver.*;
import static ed.net.HttpExceptions.*;
import static ed.net.nioclient.NIOClient.*;

public abstract class Call {
    
    protected abstract InetSocketAddress where(); 
    protected abstract void error( ServerErrorType type , Exception e );
    
    /**
     * you should put the request in buf
     * if the request doesn't fit, you must return a ByteStream with the rest of the data
     */
    protected abstract ByteStream fillInRequest( ByteBuffer buf );
    protected abstract WhatToDo handleRead( ByteBuffer buf , Connection conn );
    
    protected void cancel(){
        _cancelled = true;
    }

    public boolean isCancelled(){
        return _cancelled;
    }

    protected void pause(){
        _paused = true;
    }

    public boolean isPaused(){
        return _paused;
    }
    
    protected void wakeup(){
        _paused = false;
    }

    public void done(){
        _done = true;
        _doneTime = System.currentTimeMillis();
    }

    public boolean isDone(){
        return _done;
    }

    public long getStartedTime(){
        return _started;
    }

    public long getTotalTime(){
        if ( _done )
            return _doneTime - _started;
        return -1;
    }

    private boolean _cancelled = false;
    private boolean _paused = false;
    private boolean _done = false;
        
    protected final long _started = System.currentTimeMillis();
    private long _doneTime = -1;
        
}
