// PipedHttpResponseHandler.java

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

package ed.net.httpclient;

import java.io.*;

import ed.io.*;
import ed.util.*;

/**
*/
public class PipedHttpResponseHandler extends HttpResponseHandlerBase {
    public PipedHttpResponseHandler( OutputStream out ){
	this( out , -1 );
    }
    
    public PipedHttpResponseHandler( OutputStream out , int maxSize ){
	_out = out;
	_maxSize = maxSize;
    }

    public int read( InputStream in )
	throws IOException {
	return StreamUtil.pipe( in , _out , _maxSize );
    }

    OutputStream _out;
    int _maxSize = -1;
}
