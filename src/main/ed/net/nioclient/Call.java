// Call.java

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
