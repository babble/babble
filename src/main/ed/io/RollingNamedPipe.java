// RollingNamedPipe.java

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
        
        if ( fake )
            System.out.println( "fake named pipe for : " + path );

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
