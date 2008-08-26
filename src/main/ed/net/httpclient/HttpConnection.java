// HttpConnection.java

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

package ed.net.httpclient;

import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.io.*;

import org.apache.commons.httpclient.ChunkedInputStream;

import ed.log.*;
import ed.net.*;
import ed.util.*;

/** 
 */
class HttpConnection{

    public static int keepAliveMillis = 1000 * 5; // 5 seconds - this is how long we'll leave a connection idle before killing it
    public static int maxKeepAliveMillis = 1000 * 60 * 5; // 5 minutes - this is the max amount of time we'll use a single connection
    
    public static boolean DUMP_HEADERS = false;
    public static int CONNECT_TIMEOUT_SECONDS = 60;
    
    static boolean DEBUG = Boolean.getBoolean( "DEBUG.HTTP" );
    static int HTTP_CONNECTION_NUMBER = 1;
    static final Logger LOGGER = Logger.getLogger("ed.net.httpclient.HttpConnection" );
    static {
        LOGGER.setLevel( DEBUG ? Level.DEBUG : Level.INFO );
    }

    static public HttpConnection get( URL url )
	throws IOException {
	HCKey key = new HCKey( url );
	HttpConnection conn = _kac.get( key );
	if ( conn == null )
	    conn = new HttpConnection( key , url );
	else {
	    conn.reset( url, true );
            LOGGER.debug( "using old connection" );
	}
        
	return conn;
    }
    
    
    private HttpConnection( HCKey key , URL url ){
	_key = key;
	reset( url, true );
    }
    
    private void reset( URL url, boolean initial ){
        assert( initial || _currentUrl == url );
	_currentUrl = url;
        _closed = false;

        if( initial ) {
            _headers.clear();
            if ( url.getPort() > 0 && url.getPort() != 80 )
                _headers.put("Host" , url.getHost() + ":" + url.getPort() );
            else
                _headers.put("Host" , url.getHost() );
            _headers.put("User-Agent" , HttpClient.USER_AGENT );
            _requestMethod = "GET";
        }

	_responseHeaders.clear();

	_rc = -1;
	_httpVersion = -1;
	_message = null;
    }

    public void printHeaders() {
        for( Map.Entry entry : (Set<Map.Entry>)_headers.entrySet() )
            System.out.println( entry.getKey() + ": " + entry.getValue() );
    }

    public void done()
	throws IOException {
	
	_timeOutKeeper.remove( this );

        /*
        if( !_keepAlive ) {
            System.out.println("TEMP DWIGHT: warning httpconnection.done() keepalive=" + _keepAlive);
        }
        */

	if ( _userIn != null && _keepAlive ) {
	    byte buf[] = new byte[1024];
	    while ( _userIn != null && _userIn.read( buf ) >= 0 );
	}
	
	_lastAccess = System.currentTimeMillis();

	if ( _keepAlive ){
	    _kac.add( this );
	}
	else{
	    close();
	}
	
    }

    protected void finalize(){
	close();
    }
    
    protected void close(){
        _closed = true;
	
	if ( _sock != null ){
	    try {
		if  ( ! _usingSLL ){
		    if ( _sock != null ) 
			_sock.shutdownInput();
		    if ( _sock != null )
			_sock.shutdownOutput();
		}
	    }
	    catch ( IOException ioe ){}
	}

	if ( _userIn != null ){
	    try {
		_userIn.close();
	    }
	    catch ( Exception e ){}
	}

	if ( _in != null && _in != _userIn ){
	    try {
		_in.close();
	    }
	    catch ( Exception e ){}
	}
	
	if ( _sock != null ){
	    try {
		_sock.close();
	    }
	    catch ( Exception e ){}
	}

	_userIn = null;
	_in = null;
	_sock = null;
	
	_keepAlive = false;
    }

    public void setRequestMethod( String rm ){
	_requestMethod = rm;
    }

    public void setRequestProperty( String name , String value ){
	_headers.put( name, value);
    }
    
    /**
       reconstrucs status line
    */
    public String getStatus(){
	return "HTTP/" + _httpVersion + " " + _rc + " " + ( _message == null ? "" : _message );
    }
    
    public void go()
	throws IOException {

	boolean doIWantKeepAlive = true;

	_lastAccess = System.currentTimeMillis();

	if ( _sock == null ){
	    int port = _currentUrl.getPort();
	    if ( port < 0 ){
                if ( _currentUrl.getProtocol().equalsIgnoreCase("https") )
                    port = 443;
                else
                    port = 80;
            }
            
	    if ( DEBUG ) LOGGER.debug( "creating new socket to " + _key.getAddress() );
	    InetSocketAddress isa = new InetSocketAddress( _key.getAddress() , port );
	    
	    _sock = new Socket();
            
            int timeOut = CONNECT_TIMEOUT_SECONDS;

	    _sock.connect( isa , timeOut * 1000 );
            _sock.setSoTimeout( timeOut * 1000 * 5 );
            
	    if ( _currentUrl.getProtocol().equalsIgnoreCase("https") ){
		try {
		    //_sock = SSL.getSocketFactory().createSocket( _sock , _currentUrl.getHost() , port , true );
		    _usingSLL = true;
		    doIWantKeepAlive = false; // don't trust this with SSL yet
                    throw new IOException( "ssl not supported yet" );
		}
		catch ( Exception e ){
		    throw new RuntimeException(e);
		}
	    }
	    
            if ( _sock == null ){
                RuntimeException re = new RuntimeException("_sock can't be null here.  close called? " + _closed );
                re.fillInStackTrace();
                LOGGER.error("weird...",re);
                throw re;
            }

	    if ( _sock.getInputStream() == null )
		throw new RuntimeException("_sock.getInputStream() is null!!");  // should never happen, should be IOException
	    _in = new BufferedInputStream( _sock.getInputStream() );
	}
	
	StringBuilder buf = new StringBuilder();
	
	// First Line
	buf.append( _requestMethod).append(" ");
	String f = _currentUrl.getFile();
        
	if ( f == null || f.trim().length() == 0 )
	    f = "/";
	buf.append( f.replace(' ','+') );
	buf.append( " HTTP/1.1\r\n");
        
	for ( Iterator i = _headers.keySet().iterator() ; i.hasNext() ; ){
	    String name = (String)i.next();
	    String value = String.valueOf( _headers.get(name) );
	    
	    buf.append( name ).append(": ").append(value).append("\r\n");
            
	    if ( name.equalsIgnoreCase("connection") && value.equalsIgnoreCase("close") )
		doIWantKeepAlive = false;
	}
	buf.append("\r\n");
	
	String headerString = buf.toString();
	if ( DEBUG ) System.out.println( headerString );
	try {
	    _sock.getOutputStream().write( headerString.getBytes() );
            
            if ( _postData != null )
                _sock.getOutputStream().write( _postData );

            int timeoutSeconds = 60;

	    _timeOutKeeper.add( this , timeoutSeconds );

	    _in.mark(10);
	    if ( _in.read() < 0 )
		throw new IOException("stream closed on be ya bastard");
	    _in.reset();
	    if ( DEBUG ) System.out.println("sent header and seems to be ok");
	}
	catch ( IOException ioe ){
	    if ( _keepAlive ){
		if ( DEBUG ) LOGGER.debug( "trying again");
		// if we previously had a keep alive connection, maybe it died, so rety
		_keepAlive = false;
                _key.reset();
		close();
		reset( _currentUrl, false );
		go();
		return;
	    }
	    throw ioe;
	}
	
	// need to look for end of headers

	byte currentLine[] = new byte[2048];
	int idx = 0;
	boolean gotStatus = false;
	boolean chunked = false;
	int lineNumber = 0;
	boolean previousSlashR = false;
	while ( true ){
	    if ( idx >= currentLine.length ){
		byte temp[] = new byte[currentLine.length * 2];
		for ( int i=0; i<currentLine.length; i++)
		    temp[i] = currentLine[i];
		currentLine = temp;
	    }
            int t = -1;
            try {
                t = _in.read();
            } catch ( NullPointerException e ) {
                throw new IOException( "input stream was closed while parsing headers" );
            }

	    if ( t < 0 )
		throw new IOException("input stream got closed while parsing headers" );

	    currentLine[idx] = (byte)t;
	    if ( currentLine[idx] == '\r' ){
		currentLine[idx] = ' ';
	    }
            else if ( currentLine[idx] == '\n' ){
		String line = new String(currentLine,0,idx).trim();
		if ( DEBUG ) System.out.println( line );
		if ( line.length() == 0 ){
                    if ( DEBUG ) System.out.println( "rc:" + _rc );
                    if ( _rc == 100 ){
                        if ( DEBUG ) System.out.println("got Continue");
                        gotStatus = false;
                        lineNumber = 0;
                        idx = 0;
                        continue;
                    }
		    break;
                }

		if ( ! gotStatus ){
		    gotStatus = true;
	
		    Matcher m = STATUS_PATTERN.matcher( line );
		    if ( ! m.find() )
			throw new IOException("invalid status line:" + line );
		    
		    _httpVersion = Double.parseDouble( m.group(1) );
		    _rc = Integer.parseInt( m.group(2) );
		    _message = m.group(3);

		    _responseHeaderFields[0] = line;
		}
		else {
		    int colon = line.indexOf(":");
		    if ( colon < 0 ) {
			//throw new IOException("invalid header[" + line + "]");
		        LOGGER.error("weird error : {" + line + "} does not have a colon, using the whole line as the value and SWBadHeader as the key");
		        line = "SWBadHeader:" + line;
		        colon = line.indexOf(":");
		    }
		    String name = line.substring(0,colon).trim();
		    String value = line.substring(colon+1).trim();
	
		    _responseHeaders.put( name , value );
		    
		    if ( name.equalsIgnoreCase("Transfer-Encoding") && 
			 value.equalsIgnoreCase("chunked") )
			chunked = true;
		    
		    if ( lineNumber >= ( _responseHeaderFields.length - 2 ) ){
			// need to enlarge header...
			
			String keys[] = new String[_responseHeaderFieldKeys.length*2];
			String values[] = new String[_responseHeaderFields.length*2];
			
			for ( int i=0; i<lineNumber; i++ ){
			    keys[i] = _responseHeaderFieldKeys[i];
			    values[i] = _responseHeaderFields[i];
			}
			
			_responseHeaderFieldKeys = keys;
			_responseHeaderFields = values;
		    }
		    
		    _responseHeaderFieldKeys[lineNumber] = name;
		    _responseHeaderFields[lineNumber] = value;

		}
		if ( DEBUG ) System.out.println( "\t" + _responseHeaderFieldKeys[lineNumber] + ":" + _responseHeaderFields[lineNumber] );
		lineNumber++;
		idx = -1;
	    }
	    idx++;
	}
	
	_responseHeaderFieldKeys[lineNumber] = null;
	_responseHeaderFields[lineNumber] = null;
	
	// TODO: obey max? etc...?

	_keepAlive = false;
	if ( doIWantKeepAlive && (chunked || getContentLength() >= 0 || _rc == 304) ) {
            String hf = null;

            if ( hf == null )
                hf = "Connection";
            
	    if ( _httpVersion > 1 ){
		_keepAlive =  
		    getHeaderField( hf ) == null || 
		    getHeaderField( hf ).toLowerCase().indexOf("close") < 0;
	    }
	    else {
		_keepAlive = 
		    getHeaderField( hf ) != null &&
		    getHeaderField( hf ).toLowerCase().indexOf("keep-alive") >= 0 ;
	    }
	}

	if ( DEBUG ) System.out.println( "_keepAlive=" + _keepAlive );

        /* DM: TODO --------------------------------
           fix keepalive it's not set if no content length
        */

        if( !_requestMethod.equals("HEAD") ) {
            if ( chunked ) {
                _userIn = new ChunkedInputStream( _in );
            } else if ( _keepAlive ){
                _userIn = new MaxReadInputStream( _in , getContentLength() );
            }
            else {
                _userIn = _in; // just pass throgh
            }
        }
	
	_lastAccess = System.currentTimeMillis();

    }

    public String getHeaderFieldKey( int i ){
	return _responseHeaderFieldKeys[i];
    }
    
    public String getHeaderField( int i ){
	return _responseHeaderFields[i];
    }

    public String getHeaderField( String name ){
	return (String)_responseHeaders.get(name);
    }

    public int getResponseCode(){
	return _rc;
    }

    public InputStream getInputStream(){
	return _userIn;
    }
    
    public int getContentLength(){
	return StringParseUtil.parseInt( getHeaderField("Content-Length") , -1 );
    }
    
    public String getContentEncoding(){
	String s = getHeaderField("Content-Type");
	if ( s == null )
	    return null;
	String pcs[] = s.toLowerCase().split("charset=");
	if ( pcs.length < 2 )
	    return null;
	return pcs[1];
    }

    
    boolean isOk(){

        long now = System.currentTimeMillis();

	return 
	    _keepAlive && 
	    _sock != null &&
	    _lastAccess + _keepAliveMillis > now && 
	    _creation + _maxKeepAliveMillis > now && 
	    ! _sock.isClosed() &&
	    _sock.isConnected() &&
	    ! _sock.isInputShutdown() &&
	    ! _sock.isOutputShutdown() ;
    }

    public int hashCode(){
	return _id;
    }

    public boolean equals( Object o ){
	return _id == o.hashCode();
    }

    public void setPostData( byte b[] ){
        _postData = b;
        _headers.put( "Content-Length" , b.length );
        _headers.put( "Content-Type" , "application/x-www-form-urlencoded" );
    }

    private static int ID = 1;
    private int _id = ID++;
    
    private URL _currentUrl;
    private HCKey _key;

    private String _requestMethod;

    private byte _postData[];

    private Map _headers = new StringMap();
    private Map _responseHeaders = new StringMap();
    
    private String _responseHeaderFieldKeys[] = new String[50];
    private String _responseHeaderFields[] = new String[50];
    
    private Socket _sock;
    private BufferedInputStream _in;
    private boolean _usingSLL = false;

    private InputStream _userIn;
    
    private int _rc ;
    private double _httpVersion;
    private String _message;

    private boolean _keepAlive = false;
    
    private long _lastAccess = System.currentTimeMillis();
    private final long _creation = System.currentTimeMillis();

    private long _keepAliveMillis = keepAliveMillis;
    private long _maxKeepAliveMillis = maxKeepAliveMillis;

    private boolean _closed = false;

    private static Pattern STATUS_PATTERN = Pattern.compile("HTTP/([\\d\\.]+)\\s+(\\d+)\\s*(.*)" , Pattern.CASE_INSENSITIVE );

    static class HCKey {
        HCKey( URL url )
	    throws IOException {

            _host = url.getHost();
            _port = url.getPort();
            
            _string = _host + ":" + _port;
            _hash = _string.hashCode();
            
            reset();
	}

        void reset()
            throws IOException {
	    _address = DNSUtil.getByName( _host );
        }
	
        final InetAddress getAddress(){
	    return _address;
	}
	
        public final int hashCode(){
	    return _hash;
	}
	
        public String toString(){
	    return _string;
	}
	
	public boolean equals( Object o ){
	    HCKey other = (HCKey)o;
	    return 
		this._string.equals( other._string );
	}

        final String _host;
        final int _port;

	final int _hash;
	final String _string;
        
	private InetAddress _address;
    }

    private static KeepAliveCache _kac = new KeepAliveCache();


    static class KeepAliveCache {
        KeepAliveCache(){
	    _cleaner = new KeepAliveCacheCleaner();
	    _cleaner.start();

	    _closer = new KeepAliveCacheCloser();
	    _closer.start();
	}
	
        synchronized HttpConnection get( HCKey key ){
	    List<HttpConnection> l = _hostKeyToList.get( key );

	    if ( l == null ){
		if ( DEBUG ) System.out.println("no kept alive list for:" + key );
		return null;
	    }

	    while ( l.size() > 0 ){
		HttpConnection conn = l.remove(0);
		if ( conn.isOk() ){
		    if ( DEBUG ) System.out.println("found kept alive for:" + key );
		    return conn;
		}
		conn.close();
		if ( DEBUG ) System.out.println("closing kept alive for:" + key );
		// otherwise it'll just get garbage collected
	    }
	    /*
	      note: i'm not removing the empty list on purpose/
	      because i think it will get created and deleted very often
	      technically, there is a chance i'm wrong ;)
	    */
	    return null;
	}
	
        synchronized void add( HttpConnection conn ){
	    if ( DEBUG ) System.out.println( "adding connection for:" + conn._key );
	    List<HttpConnection> l = _hostKeyToList.get( conn._key );
	    if ( l == null ){
		l = new ArrayList<HttpConnection>();
		_hostKeyToList.put( conn._key , l );
	    }
	    l.add( conn );
	}

	private synchronized void clean(){
	    for ( Iterator<HCKey> i = _hostKeyToList.keySet().iterator() ; i.hasNext() ; ){
		HCKey key = i.next();
		List<HttpConnection> l = _hostKeyToList.get( key );
		for ( Iterator<HttpConnection> j = l.iterator(); j.hasNext() ; ){
		    HttpConnection hc = j.next();
		    if ( ! hc.isOk() ){
			//hc.close();
			_closer._toClose.offer( hc );
			j.remove();
			Logger.getLogger( this.getClass() ).debug( "removing a " + key );
		    }
		    else {
			Logger.getLogger( this.getClass() ).debug( "keeping a " + key );
		    }
		}
	    }
	}
	
	private Map<HCKey,List<HttpConnection>> _hostKeyToList = new HashMap<HCKey,List<HttpConnection>>();

	private Thread _cleaner;
	private KeepAliveCacheCloser _closer;
	
	class KeepAliveCacheCloser extends Thread {
            KeepAliveCacheCloser(){
		super("KeepAliveCacheCloser");
		setDaemon( true );
	    }

	    public void run(){
		while ( true ){
		    try {
			HttpConnection hc = _toClose.take();
			if ( hc != null ){
			    try {
				hc.close();
			    }
			    catch ( Exception e ){
				Logger.getLogger("ed.net.httpclient.HttpConnection.KeepAliveCacheCloser").log( Level.DEBUG , "error closing" , e );
			    }
			}
		    }
		    catch ( Exception e ){
			Logger.getLogger("ed.net.httpclient.HttpConnection.KeepAliveCacheCloser").error("error running" , e );
		    }
		}
	    }
	    
	    BlockingQueue<HttpConnection> _toClose = new ArrayBlockingQueue<HttpConnection>(10000);
	}

	class KeepAliveCacheCleaner extends Thread {
            KeepAliveCacheCleaner(){
		super("KeepAliveCacheCleaner");
		setDaemon( true );
	    }
	    
	    public void run(){
		while ( true ){
		    try {
			Thread.sleep( 1000 * 30 );
			clean();
		    }
		    catch ( Exception e ){
			Logger.getLogger( this.getClass() ).error("error cleaning" , e );
		    }
		}
	    }
	}
    }
    
    static TimeOutKeeper _timeOutKeeper = new TimeOutKeeper();

    static class TimeOutKeeper extends Thread {
        TimeOutKeeper(){
	    super("TimeOutKeeper");
	    setDaemon(true);
	    start();
	}
	
        void add( HttpConnection conn , long seconds ){
	    synchronized ( _lock ){
		_connectionToKillTime.put( conn , new Long( System.currentTimeMillis() + ( seconds * 1000 ) ) );
	    }
	}

        void remove( HttpConnection conn ){
	    synchronized ( _lock ){
		_connectionToKillTime.remove( conn );
	    }
	}
	
	public void run(){
	    while ( true ){
		try {
		    Thread.sleep( 1000 * 5 );
		}
		catch ( InterruptedException ie ){
		}
		
		List toRemove = new ArrayList();
		synchronized ( _lock ){
		    for ( Iterator i = _connectionToKillTime.keySet().iterator() ; i.hasNext() ; ){
			HttpConnection conn = (HttpConnection)i.next();
			Long time = (Long)_connectionToKillTime.get( conn );
			
			if ( time.longValue() < System.currentTimeMillis() ){
			    LOGGER.debug( "timing out a connection" );
			    i.remove();
			    toRemove.add( conn );
			}
		    }
		}
		for ( Iterator i = toRemove.iterator() ; i.hasNext() ; ){
		    HttpConnection conn = (HttpConnection)i.next();
		    conn.close();
		}
		
	    }
	}
	
	private String _lock = "HttpConnection-LOCK";
	private Map _connectionToKillTime = new HashMap();
	
    }
    
    static class MaxReadInputStream extends InputStream {
        MaxReadInputStream( InputStream in , int max ){
            _in = in;
            _toGo = max;
        }

        public int available()
            throws IOException {
            return _in.available();
        }
    
        public int read()
            throws IOException {
	
            /** [dm] this was returning zero, which is bad as you then get a file downloaded 
                on a timeout that ends with a bunch of zeroes and no error reported!  it might
                be better to throw an ioexception rather than return -1.  -1 worked for my 
                purposes.  feel free to change that if you like that better.
            */
            if ( _in == null || _closed )
                return -1;

            if ( _toGo <= 0 )
                return -1;
	
            int val = _in.read();
            _toGo--;
            return val;
        }
    
        public void close(){
            _closed = true;
        }

        private InputStream _in;
        private int _toGo;
        private boolean _closed = false;
    }
 
}
