// RollingNamedPipe.java

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

package ed.io;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import ed.log.*;

/**
 */
public class RollingNamedPipe {

    private final static String _namedDir = "logs/pipes/";
    static {
        File f = new File( _namedDir );
        f.mkdirs();
    }

    public RollingNamedPipe( String name ){
        this( _namedDir + name , 5 , false );
    }
    
    public RollingNamedPipe( String path , int queueSize , boolean require  ){
        this( new File( path ) , queueSize , require );
    }

    public RollingNamedPipe( File path , int queueSize , boolean require ){
        _path = path;
        _queueSize = queueSize;
        _require = require;

        boolean fake = true;
        
        if ( _path.exists() ){
            fake = false;
        }
        else {
            
            SysExec.Result res = null;
            Exception error = null;
            
            try {
                res = SysExec.exec( "mkfifo " + path.toString() );
                fake = res.exitValue() != 0;
            }
            catch ( Exception ioe ){
                fake = true;
                error = ioe;
            }
            
            if ( fake && require )
                throw new RuntimeException( "can't make named pipe : " + _path , error);
        }
        
        _fake = fake;
        
        _buffer = new ArrayBlockingQueue<byte[]>( queueSize );
        _writer = _fake ? null : new MyWriter();
    }

    public boolean canWrite(){
        return ! _fake && _buffer.size() < _queueSize;
    }
    
    public boolean write( String s ){
        if ( _fake )
            return false;
        
        if ( _buffer.size() >= _queueSize )
            return false;

        return _buffer.offer( s.getBytes() );
    }

    public void setMessageDivider( String s ){
        _messageDivider = s.getBytes();
    }
    
    class MyWriter extends Thread  {
        MyWriter(){
            super( "NamedPipe-Writer : " + _path );
            setDaemon( true );
            start();
        }

        public void run(){

            while ( true ){
                try {
                    byte b[] = _buffer.take();
                    if ( b == null )
                        continue;
                    
                    long start = System.currentTimeMillis();

                    FileOutputStream out = new FileOutputStream( _path );
                    out.write( b );
                    out.write( _messageDivider );
                    out.close();


                    long end = System.currentTimeMillis();
                    
                    if ( ( end - start ) > 1000 * 60 ){
                        // took too long, probably no one was listening, lets kill it
                        while ( _buffer.poll() != null );
                    }
                }
                catch ( IOException ioe ){
                    // don't care, someone stopped listening
                }
                catch ( Exception e ){
                    _logger.error( "error running" , e );
                }
            }
            
        }
    }
    
    
    final File _path;
    final boolean _require;
    final int _queueSize;

    final boolean _fake;
    final MyWriter _writer;
    final BlockingQueue<byte[]> _buffer;

    private byte[] _messageDivider = "\n------\n\n".getBytes();
    private static Logger _logger = Logger.getLogger( "ed.RollingNamedPipe" );
}
