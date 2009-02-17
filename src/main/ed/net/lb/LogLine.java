// LogLine.java

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

package ed.net.lb;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import ed.io.*;

public class LogLine {
    
    public LogLine( Date when , String ip , int retry , String server , int responseCode ,
                    String method , String url , int genTime , String userAgent ,
                    String referer , String cookie
                    ){
        
        _when = when;
        _ip = ip;
        _retry = retry;
        _server = server;
        _responseCode = responseCode;

        _method = method;
        _url = url;
        _genTime = genTime;
        _userAgent = _fixString( userAgent );
        _referer = _fixString( referer );
        _cookie = _fixString( cookie );
    }

    public static Iterable<LogLine> parse( InputStream in )
        throws IOException {
        
        final LineReader lr = new LineReader( in );
        
        return new Lines( lr.iterator() );
    }

    static class Lines implements Iterator<LogLine> , Iterable<LogLine> {

        Lines( Iterator<String> lines ){
            _lines = lines;
        }

        public LogLine next(){
            return parse( _lines.next() );
        }

        public boolean hasNext(){
            return _lines.hasNext();
        }

        public void remove(){
            throw new RuntimeException( "not allowed" );
        }

        public Iterator<LogLine> iterator(){
            return this;
        }

        final Iterator<String> _lines;
    }
    
    public static LogLine parse( String line ){
        final Matcher m = _linePattern.matcher( line );
        if ( ! m.find() )
            throw new RuntimeException( "invalid line [" + line + "]" );
        
        try {
            LogLine ll =  new LogLine( 
                                      LB._dateFormat.parse( m.group(1) ) ,
                                      m.group(2) ,
                                      Integer.parseInt( m.group(3) ) ,
                                      m.group(4) ,
                                      Integer.parseInt( m.group(5) ) ,
                                      m.group(6) ,
                                      m.group(7) ,
                                      Integer.parseInt( m.group(8) ) ,
                                      m.group(9) ,
                                      m.group(10) ,
                                      m.group(11)
                                       );

            if ( ! ll.toString().trim().equals( line ) )
                throw new RuntimeException( "different\n" + line + "\n" + ll.toString() );
            
            return ll;
        }
        catch ( java.text.ParseException pe ){
            throw new RuntimeException( "can't parse line [" + line + "]" , pe );
        }
    }
    
    static final Pattern _linePattern = 
        Pattern.compile( "\\[(.*?)\\] " +
                         "([\\d\\.]+) " + 
                         "retry:(\\d+) " + 
                         "went:([\\w\\.\\-:/]+) " +
                         "(\\d+) " + // responseCode
                         "(\\w+) " + // method
                         "\"(.*?)\" " +  // url
                         "(\\d+) " + 
                         "\"(.*?)\" " + 
                         "\"(.*?)\" " + 
                         "\"(.*?)\"" + 
                         ""
                         );

    static String _fixString( String s ){
        if ( s == null )
            return null;
        
        s = s.trim();
        if ( s.length() == 0 )
            return null;
        
        if ( s.equals( "null" ) )
            return null;
        
        return s;
    }

    public String toString(){
        return 
            "[" + LB._dateFormat.format( _when ) + "] " + 
            _ip + " "  +
            "retry:" + _retry + " " +
            "went:" + _server + " " +
            _responseCode + " " + 
            _method + " " +
            "\"" + _url + "\" " + 
            _genTime + " " + 
            "\"" + _userAgent + "\" " +
            "\"" + _referer + "\" " +
            "\"" + _cookie + "\"";
    }
    
    final Date _when;
    final String _ip;
    final int _retry;
    final String _server;
    final int _responseCode;

    final String _method;
    final String _url;
    final int _genTime;
    final String _userAgent;
    final String _referer;
    final String _cookie;
    
    public static void main( String args[] )
        throws IOException {
        
        for ( LogLine ll : parse( new FileInputStream( args[0] ) ))
            System.out.println( ll );

    }

}
