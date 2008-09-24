// DNSResolver.java

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

package ed.net;

import java.net.*;
import java.util.*;
import java.io.*;

import org.xbill.DNS.*;

import ed.net.httpserver.*;
import ed.log.*;
import ed.util.*;

public class DNSResolver {

    private static boolean DEBUG = Boolean.getBoolean("DEBUG.DNS");
    public static boolean TRACE_ALL = false;

    private static final Logger _logger = Logger.getLogger( "ed.net.DNSResolver" );
    static {
	_logger.setLevel( Level.INFO );
    }

    public static final boolean trace(){
        if ( ! DEBUG )
            return false;
        
        if ( TRACE_ALL )
            return true;
	
	return Math.random() > .9;
    }
    static ThreadLocal<String> _host = new ThreadLocal();

    static final int VERSION_ID = 6;
    static final long NEGATIVE_TTL = TimeConstants.S_MINUTE * 10;
    static final long MINIMUM_TTL = TimeConstants.S_MINUTE;

    static final int MAX_ENTRIES = 20001;

    static final int TIMEOUT = 4;
    
    public static InetAddress[] getAllByName( String host )
        throws UnknownHostException {
        
        _host.set( host );

        List<InetAddress> l = lookupAll( host );
	if ( l == null || l.size() == 0 )
	    throw new UnknownHostException(host);
        InetAddress all[] = new InetAddress[l.size()];
        for ( int i=0; i<l.size(); i++ )
            all[i] = l.get(i);
        return all;
    }

    public static InetAddress getByName( String host )
        throws UnknownHostException {
        _host.set( host );
        return lookupAll( host ).get(0);
    }

    public static List<InetAddress> lookupAll( String host )
        throws UnknownHostException {
        _host.set( host );
        synchronized ( DNSUtil.getDomain( host ).intern() ){
            return lookupAll( _stringToName( host ) );
        }
    }

    static List<InetAddress> lookupAll( Name host )
        throws UnknownHostException {
        
        if ( isIpAddress( host ) ){
            List<InetAddress> l = new ArrayList<InetAddress>();
            l.add( _nameToIp( host ) );
            return l;
        }

        List<Record> records = lookupRecords( host );
        if ( records == null || records.size() == 0 )
            throw new UnknownHostException( host.toString() );
        
        List<InetAddress> addrs = new ArrayList<InetAddress>();
        for ( Record r : records ){
            if ( r.getType() == Type.A ){
                addrs.add( ((ARecord)r).getAddress() );
            }
        }
        if ( addrs.size() == 0 )
            throw new UnknownHostException( host.toString() );
        return addrs;
    }

    static InetAddress _nameToIp( Name n )
        throws UnknownHostException {
        if ( ! isIpAddress( n ) )
            throw new UnknownHostException("can't do that:" + n );
        String s = n.toString();
        return InetAddress.getByName( s.substring( 0 , s.length() - 1 ) );
    }

    static boolean isIpAddress( Name host ){
        if ( host.labels() != 5 )
            return false;

        for ( int i=0; i<4; i++ ){
            if ( ! StringUtil.isDigits( host.getLabelString( i ) ) )
                return false;
        }

        return true;
    }

    static List<Record> lookupRecords( String host )
	throws UnknownHostException{
        return lookupRecords( _stringToName( host ) );
    }

    static List<Record> lookupRecords( Name host ){
        return lookupRecords( host , null );
    }

    public static List<Record> singleCacheLookup( String host )
        throws UnknownHostException {
        return _cache.lookup( _stringToName( host ) );
    }

    public static List<Record> traceLookup( String host , boolean adjustTTLs )
        throws UnknownHostException {
        return traceLookup( _stringToName( host ) , adjustTTLs );
    }

    public static List<Record> traceLookup( Name host , boolean adjustTTLs )
        throws UnknownHostException {
        List<Record> lst = _traceLookup( host , adjustTTLs );
        Collections.reverse( lst );
        return lst;
    }
    
    private static List<Record> _traceLookup( Name host , boolean adjustTTLs )
        throws UnknownHostException {
        
        List<Record> check = lookupRecords( host );
        if ( check == null || check.size() == 0 )
            return new LinkedList<Record>();

        // try the Cs        
        List<Record> cur = _cache.get( host , Type.CNAME );
        if ( cur != null ){
            for ( Record r : cur ){
                if ( r instanceof CNAMERecord ){
                    CNAMERecord c = (CNAMERecord)r;
                    Name next = c.getAlias();
                    List<Record> lst = _traceLookup( next , adjustTTLs );
                    if ( lst != null ){
                        if ( adjustTTLs )
                            c = new CNAMERecord( c.getName() , c.getDClass() , _cache.getTTL( host , Type.CNAME ) , next );
                        
                        lst.add( c );
                        return lst;
                    }
                }
            }
        }
        

        // look for an A
        cur = _cache.get( host , Type.A );
        for ( Record r : cur ){
            if ( r instanceof ARecord ){
                List<Record> lst = new LinkedList<Record>();
                if ( adjustTTLs )
                    r = new ARecord( r.getName() , r.getDClass() , _cache.getTTL( host , Type.A ) , ((ARecord)r).getAddress() );
                lst.add( r );
                return lst;
            }
        }
        
        return new LinkedList<Record>();        
    }
    
    public static boolean isRelated( String hostString , String testHostString )
        throws UnknownHostException {
        Name host = _stringToName( hostString );
        Name testHost = _stringToName( testHostString );
        
        lookupAll( host );

        for ( int type : _typesToCheck ){
            List<Record> lst = _cache.get( host , type );
            if ( lst == null )
                continue;
            
            for ( Record r : lst ){
                Name other = null;

                if ( r instanceof NSRecord )
                    other = ((NSRecord)r).getTarget();
                else if ( r instanceof CNAMERecord )
                    other = ((CNAMERecord)r).getAlias();
                
                if ( other.subdomain( testHost ) )
                    return true;
            }
        }

        return false;
    }
    
    static int _typesToCheck[] = new int[]{ Type.NS , Type.CNAME };
    
    static List<Record> lookupRecords( Name host , State state ){
        if ( state == null )
            state = new State();
            
        synchronized ( host.toString().intern() ){
            List<Record> lst = _cache.lookup( host );
            if ( lst != null ){
                return _handleLookupResults( lst , state , false );
            }
            
            if ( state._depth > 100 ){
                throw new RuntimeException("depth should not be this high...");
            }

            if ( state._stack.contains( host ) ){
                if ( trace() ) System.out.println("found loop");
                return null;
            }

            state._stack.add( host );

            try {
                while ( _doIteration( host , state ) ){
                    lst = _cache.lookup( host );
                    if ( lst != null ){
                        return _handleLookupResults( lst , state , true );
                    }
                }


                lst = _cache.lookup( host );
                if ( lst != null )
                    return _handleLookupResults( lst , state , true );

            }
            finally {
                state._stack.remove( state._stack.size() - 1 );
            }
            
            if ( state._depth == 0 )
                _cache.addNegative( host , Type.A );
        }
        return null;
    }

    static List<Record> _handleLookupResults( List<Record> lst , State state , boolean newlyFetched ){
        if ( lst.size() == 0 )
            return lst;
        if ( lst.get(0).getType() == Type.CNAME ){
            
            for ( Record r : lst ){
                if ( state != null ){
                    if ( state._seen.contains( r ) )
                        continue;
                    state._seen.add( r );
                }
                return lookupRecords( ((CNAMERecord)r).getAlias() , state );
            }
            _logger.error( "circularCname: " + lst.get(0).getName().toString() );
            return null;
        }
        return lst;
    }

    static boolean _doIteration( Name host , State state ){
        if ( trace() ) System.out.println( "_doIteration:" + host );
        List<Record> ns = _getLowestNameServersInCache( host , state );
        if ( ns == null )
            return false;
        if ( trace() ) System.out.println( "\t nameServers for:" + host + " = " + ns );

        List<NSRecord> actuallySentTo = new ArrayList<NSRecord>();
        
        Message response = _sendLookup( ns , host , state , actuallySentTo );
        if ( trace() ) System.out.println( "res:" + response );
        
        if ( response == null && actuallySentTo.size() == 0 && state._depth == 0 ){ // we couldn't resolve any of the nameservers
            if ( trace() ){
                System.out.println("name servers for:" + ns.get(0).getName() + " are crap");
                System.out.println( "\t" + ns.get(0).getName().labels() );
            }
            if ( ns.get(0).getName().labels() > 2 ){
                Name removed = _removeLowestNameServersInCache( host );

                if ( removed == null )
                    return false;
                
                if ( state._lowestNSRemoved.contains( removed ) )
                    return false;
                
                state._lowestNSRemoved.add( removed );
            }
            state._checked.clear();
            return true;
        }

        if ( response == null ){
            if ( state._depth == 1 && ! state._cleared ){
                if ( trace() ) System.out.println("going to try and go back up one for:" + ns.get(0).getName() );
                state._cleared = true;
                _removeLowestNameServersInCache( host );
                state._checked.clear();
                return true;
            }
            return false;
        }
        
        for ( Record r : response.getSectionArray( Section.AUTHORITY ) ){
            if ( r.getType() == Type.SOA ){
                SOARecord sr = (SOARecord)r;
                if ( host.subdomain( sr.getName() ) )
                    return false;
            }
        }

        return true;
    }

    static Message _sendLookup( List<Record> nameServers , Name host , State state , List<NSRecord> actuallySentTo ){
        return _sendLookup( nameServers , host , state , actuallySentTo , Type.A );
    }

    static Message _sendLookup( List<Record> nameServers , Name host , State state , List<NSRecord> actuallySentTo , int t ){
        state._sendLookups++;
        Record question = Record.newRecord( host , t , DClass.IN );
        Message query = Message.newQuery( question );

        Message altQuery = null;
        if ( t == Type.A ){
            altQuery = Message.newQuery( Record.newRecord( host , Type.CNAME , DClass.IN ) );
        }
        for ( Record r : nameServers ){
            
            if ( ! ( r instanceof NSRecord ) ){
                if ( trace() ) System.out.println("got non ns record");
                continue;
            }
            
            NSRecord ns = (NSRecord)r;

            Pair<Name,Name> p = new Pair<Name,Name>( host , ns.getTarget() );
            
            if ( state._checked.contains( p ) ){
                actuallySentTo.add( ns );
                if ( trace() ) System.out.println(" already sent request to:" + ns.getTarget() + " for:" + host );
                continue;
            }
            
            state._checked.add( p );

            String addr = null;
            
            if ( ns.getName().labels() > 1 ){
                //List<Record> addresses = _cache.lookup( ns.getTarget() );
                state._depth++;
                List<Record> addresses = lookupRecords( ns.getTarget() , state );
                state._depth--;
                if ( addresses == null || addresses.size() == 0 ){
                    if ( trace() ) System.out.println("no addresses for:" + ns.getTarget() );
                    continue;
                }
                
                if ( ! ( addresses.get(0) instanceof ARecord ) ){
                    if ( trace() ) System.out.println("got non A record");
                    continue;
                }            
                
                addr = ((ARecord)addresses.get(0)).getAddress().getHostAddress();
            }
            else {
                addr = ns.getTarget().toString();
                if ( trace() ) System.out.println(" USING SYSTEM!!!!:" + ns.getTarget() + " : " + addr );
            }

            try {
                actuallySentTo.add( ns );
                if ( trace() ) System.out.println("Sending query to:" + ns.getTarget() + ":" + addr + " for:" + host );
                
                for ( int i=0; i<1; i++ ){ // this is if we want to retry to a single server. 
                    try {
                        Message response = _sendMessage( addr , query );
                        if ( size( response ) == 0 && altQuery != null )
                            response = _sendMessage( addr , altQuery );
                        _cache.add( response , ns.getName() );
                        if ( size( response ) > 0 )
                            return response;
                        if ( trace() )
                            System.out.println("empty response from:" + ns.getTarget() + " for:" + host + "\n" + response );
                        break;
                    }
                    catch ( IOException ioe ){
                        if ( trace() ){
                            System.out.println( "timeout sending to:" + ns.getTarget() + ":" + addr + " for:" + host );
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            catch ( Exception e ){
		_logger.error( "_sendLookups: shouldn't be here" , e );
            }
            
            if ( trace() ) System.out.println( "failed to:" + ns.getTarget() + " will try next name server. for:"  + host );
            
        }
        return null;
    }

    static int size( Message m ){
        return 
            m.getSectionArray( Section.ANSWER ).length + 
            m.getSectionArray( Section.AUTHORITY ).length + 
            m.getSectionArray( Section.ADDITIONAL ).length;
    }

    public static Message _sendMessage( String addr , Message query )
        throws IOException {
        
        DatagramSocket sock = new DatagramSocket();
        sock.setSoTimeout( TIMEOUT * 1000 );
        
        byte[] queryBytes = query.toWire();
        DatagramPacket queryPacket = new DatagramPacket( queryBytes , queryBytes.length , InetAddress.getByName( addr )  , 53 );
        sock.send( queryPacket );

        byte[] responseBytes = new byte[512];
        DatagramPacket recievePacket = new DatagramPacket( responseBytes , responseBytes.length );
        sock.receive( recievePacket );
        
        sock.close();

        return new Message( responseBytes );

    }

    static List<Record> _getLowestNameServersInCache( Name host , State state ){
        while ( true ){
            if ( trace() ) System.out.println( host );
            
            if ( host.equals( Name.root ) )
                return _rootNameServers;
            
            List<Record> nameServers = _cache.get( host , Type.NS );
            if ( nameServers != null && nameServers.size() > 0 ){
                return nameServers;
            }
            host = new Name( host , 1 );
        }
    }

    /**
       @return the lowest ns host name if any
     */
    static Name _removeLowestNameServersInCache( Name host ){
        while ( host.labels() > 1 ){
            if ( trace() ) System.out.println( host );
            
            if ( host.equals( Name.root ) )
                return null;
            
            List<Record> nameServers = _cache.get( host , Type.NS );
            if ( nameServers != null ){
                //System.out.println( "\t REMOVING ns for:" + host );
                _cache.remove( host , Type.NS );
                return host;
            }
            host = new Name( host , 1 );
        }
        return null;
    }

    static Name _stringToName( String host )
	throws UnknownHostException {
        try {
            if ( ! host.endsWith(".") )
                host = host + ".";
            return new Name( host );
        }
        catch ( TextParseException tpe ){
            throw new UnknownHostException( host );
        }
    }


    static final class DNSCache implements Serializable , Updatable {

        DNSCache(){
            Map<Entry,CachedStuff> m = null;
            
            try {
                FileInputStream fin = new FileInputStream("log/dns-cache");
                ObjectInputStream in = new ObjectInputStream( fin ); 
                int oldVersion = in.readInt();
                if ( oldVersion == VERSION_ID ){
                    m = (Map<Entry,CachedStuff>)in.readObject();
                }
                in.close();
                fin.close();
            }
            catch ( FileNotFoundException fnf ){
                m = null;
            }
            catch ( Throwable e ){
                e.printStackTrace();
                m = null;
            }
	    
            if ( m == null ){
                m = new LinkedHashMap<Entry,CachedStuff>( MAX_ENTRIES , .75F , true){
                    public boolean removeEldestEntry(Map.Entry eldest) {
                        return size() > MAX_ENTRIES;
                    }
                };
            }
            
            _map = m;
	    UpdateManager.add( this );
        }
	
        protected void finalize(){
            write();
        }

	public void update(){
	    write();
	}

	public long timeBeforeUpdates(){
	    return TimeConstants.MS_MINUTE * 5;
	}
	
        void write(){
            try {
		_logger.debug( "writing to cache" );

                FileOutputStream fout = new FileOutputStream( "logs/dns-cache.temp" );
                ObjectOutputStream out = new ObjectOutputStream( fout );
                out.writeInt( VERSION_ID );
		synchronized ( _map ){
		    out.writeObject( _map );
		}
                out.close();
                fout.close();
		
                File old = new File( "log/dns-cache.temp" );
                old.renameTo( new File("log/dns-cache" ) );
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }

        void add( Message m , Name from ){
            add( m , Section.ANSWER , from );
            add( m , Section.AUTHORITY , from );
            add( m , Section.ADDITIONAL , from );
        }
        
        void add( Message m , int section , Name from ){
            for ( Record r : m.getSectionArray( section ) ){
                add( r , from );
            }
        }
        
        void add( Record r , Name from ){
            if ( ! r.getName().subdomain( from ) ){
                //System.out.println( "not adding because could be poisining" );
                return;
            }
            add( r );
        }

        static int sections[] = new int[]{ Section.ANSWER , Section.AUTHORITY , Section.ADDITIONAL };        
        static Record findRecord( Message m , Name n , int type ){
            for ( int section : sections ){
                for ( Record r : m.getSectionArray( section ) ){
                    if ( r.getType() == type && r.getName().equals( n ) )
                        return r;
                }
            }
            return null;
        }

        NSRecord addNameServer( Name domain , Name ns , InetAddress ia)
            throws UnknownHostException{
            NSRecord r = new NSRecord( domain , DClass.IN , TimeConstants.S_MONTH , ns );
            add( r , -1 );
            if ( ia != null )
                add( new ARecord( ns , DClass.IN , TimeConstants.S_MONTH ,  ia ) , -1 );
            return r;
        }
        
        void add( Record r ){
            add( r.getName() , r.getType() , r.getTTL() , r );
        }

        void add( Record r , long ttl ){
            add( r.getName() , r.getType() , ttl , r );
        }

        void addNegative( Name n , int type ){
            if ( trace() ) System.out.println("Cache: adding negative: " + n );
            add( n , type , NEGATIVE_TTL , null );
        }

        void add( Name n , int type , long ttl , Record r ){
            if ( ttl >= 0 && ttl < MINIMUM_TTL )
                ttl = MINIMUM_TTL;
            
            if ( r == null )
                return;

            if ( r.getType() == Type.CNAME ){
                CNAMERecord c = (CNAMERecord)r;
                if ( c.getName().equals( c.getAlias() ) ){
                    return;
                }
            }

            synchronized ( _map ){
                Entry e = _makeEntry( _lookupEntry , n , type );
                CachedStuff cs = _map.get( e );
            
                if ( cs == null ){
                    e = _makeEntry( n , type );
                    cs = new CachedStuff();
                    _map.put( e , cs );
                }

                if ( ttl < 0 )
                    cs._expires = -1;
                else
                    cs._expires = System.currentTimeMillis() + ( 1000 * ttl );
                
                if ( r != null && ! cs._lst.contains( r ) ){
                    cs._lst.add( r );
                    Collections.shuffle( cs._lst );
                }
                
            }
        }

        List<Record> lookup( Name n ){
            return lookup( n , 0 );
        }
        
        List<Record> lookup( Name n , int iter ){
            return lookup( n , iter , new HashSet<Name>() );
        }
        
        List<Record> lookup( Name n , int iter , HashSet<Name> been ){

            if ( been.contains( n ) ){
                _logger.error("lookup : think i found a circular loop for: " + n );
                return null;
            }
            been.add( n );

            if ( iter > 50 ){
                _logger.error("lookup : going insane for: " + n );
                return null;
            }
            
            if ( trace() ) System.out.println("Cache: Looking up:" + n );
            List<Record> records = get( n , Type.A );
            if ( records != null ){
                if ( trace() ) System.out.println("\t found A:" + records );
                return records;
            }
            
            records = get( n , Type.CNAME );
            if ( records == null )
                return null;
            if ( records.size() == 0 ){
                if ( trace() ) System.out.println("\t empty C:" + records );
                return records;
            }
            
            for ( Record r : records ){
                CNAMERecord c = (CNAMERecord)r;
                if ( c.getAlias().equals( n ) )
                    continue;
                if ( trace() ) System.out.println("\t down C:" + records );
                List<Record> next = lookup( c.getAlias() , iter + 1  , been );
                if ( next != null && next.size() > 0 )
                    return next;
            }
            
            return records;
        }

        List<Record> get( Name n , int type ){
            synchronized ( _map ){
                Entry e = _makeEntry( _lookupEntry , n , type );
                CachedStuff cs = _map.get( e );
                
                if ( cs == null )
                    return null;

                if ( cs._expires > 0 && cs._expires < System.currentTimeMillis() ){
                    if ( trace() ) System.out.println( "\t\t " + n + " expired." );
                    _map.remove( e );
                    return null;
                }
                if ( trace() ) System.out.println( "\t\t " + n + " expires in " + ( ( cs._expires - System.currentTimeMillis()  ) / 1000 ) + " s. " + cs._expires );
                return cs._lst;
            }
        }

        /**
         * @return ttl in seconds or -1 for a failure
         */
        int getTTL( Name n , int type ){
            Entry e = _makeEntry( _lookupEntry , n , type );
            CachedStuff cs = _map.get( e );
            
            if ( cs == null )
                return -1;
            
            long diff = cs._expires - System.currentTimeMillis();
            if ( diff <= 0 )
                return -1;
            
            return (int)(diff/1000);
        }

        List<Record> remove( Name n , int type ){
            synchronized ( _map ){
                Entry e = _makeEntry( _lookupEntry , n , type );
                CachedStuff cs = _map.remove( e );
                if ( _map.get( e ) != null ){
                    _logger.error("cacheBroken: " + n + " was removed but still there.." );
                }
                if ( cs == null )
                    return null;
                return cs._lst;
            }
        }

        
        Entry _makeEntry( Name n , int type ){
            return _makeEntry( new Entry() , n , type );
        }
        
        Entry _makeEntry( Entry e , Name n , int type ){
            e._name = n;
            e._type = type;
            return e;
        }

        public void dump( PrintStream out ){
            synchronized ( _map ){
                for ( Map.Entry<Entry,CachedStuff> e : _map.entrySet() ){
                    System.out.println("----");
                    System.out.println( e.getKey() );
                    for ( Record r : e.getValue()._lst ){
                        System.out.println( "\t " + r );
                    }
                }
            }
        }

        private final Entry _lookupEntry = new Entry();
        private final Map<Entry,CachedStuff> _map;
        
        class Entry implements Serializable {

            // private static final long serialVersionUID = 7367964651970623133L;

            public int hashCode(){
                return _name.hashCode() + _type;
            }
            
            public boolean equals( Object o ){
                Entry e = (Entry)o;
                return 
                    _type == e._type &&
                    _name.equals( e._name );
            }
            
            public String toString(){
                return _name.toString();
            }

            Name _name;
            int _type;
        }
        
        class CachedStuff implements Serializable  {
            // private static final long serialVersionUID = 6902936470711515237L;
            List<Record> _lst = new ArrayList<Record>();
            long _expires = -1;
        }
        
        
    }

    static class State {
        Set<Pair<Name,Name>> _checked = new HashSet<Pair<Name,Name>>();
        boolean _cleared = false;
        int _depth = 0;
        int _sendLookups = 0;
        List<Name> _stack = new ArrayList<Name>();
        Set<Name> _lowestNSRemoved = new HashSet<Name>();
        Set<Record> _seen = new HashSet<Record>();
    }
    
    static final DNSCache _cache = new DNSCache();
    static final List<Record> _rootNameServers = new ArrayList<Record>();

    static class Monitor extends HttpMonitor {

	Monitor(){
	    super( "dns" , false );
	}
	
	public void handle( JxpWriter out , HttpRequest request , HttpResponse response ){
	    
            String h = request.getParameter("h","").trim();
            if ( h.length() == 0 ){
                out.print( "use the h param" );
		return;
	    }
            
            out.print( "<h3>" + h + "</h3>\n" );
            
            try {
                out.print( "<pre>" );
                List<Record> lst = traceLookup( h , true );
                for ( Record r : lst ){
                    out.print( r.toString() );
		    out.print( "\n" );
                }
                out.print( "</pre>" );
            }
            catch ( Exception e ){
                out.print( "<b>" + e.toString() + "</b>" );
            }

        }

    }

    static {
        
        HttpServer.addGlobalHandler( new Monitor() );

        for ( char c = 'A'; c <= 'M'; c++ ){
            try {
                String foo = c + ".ROOT-SERVERS.NET";
                _rootNameServers.add( _cache.addNameServer( Name.root , new Name( foo +".") , InetAddress.getByName( foo ) ) );
                
            }
            catch ( Exception e ){
                e.printStackTrace();
            }
        }
    }

    static void _test( String host , boolean valid ){
        String res = null;
        try {
            res = lookupAll( host ).toString();
        }
        catch ( UnknownHostException e ){
            res = null;
        }

        System.out.println( host + ":" + res );
        if ( valid && res == null ){
            throw new RuntimeException("fail!!");
        }
    }
    
    static void _test()
        throws Exception {
        
        _test( "4.78.166.131" , true );
        _test( "www.shopwiki.com" , true );
        _test( "shopwiki.com" , true );
        _test( "www.shopwiki.com." , true );
        _test( "www.shopwiki.com" , true );
        _test( "www.shopwiki.com" , true );
        _test( "ny1.shopwiki.com" , true );
        _test( "static.shopwiki.com" , true );
        _test( "static-dev.shopwiki.com" , true );
        _test( "sadasdas.shopwiki.com" , false );
        
        _test( "slashdot.net" , true);
        _test( "store.apple.com" , true );        
        _test( "www.buy.com" , true );        
        _test( "www.flexistentialist.com" , false );        

        _cache.addNameServer( new Name( "google.com." ) , new Name( "ns1.google.com." ) , null );
        _cache.addNameServer( new Name( "google.com." ) , new Name( "ns2.google.com." ) , null );
        _cache.addNameServer( new Name( "google.com." ) , new Name( "ns3.google.com." ) , null );
        _cache.addNameServer( new Name( "google.com." ) , new Name( "ns4.google.com." ) , null );
        
        _test( "www.google.com" , true );
        _test( "www.macys.com" , true );
        _test( "www.asdhasd1287ascxhsa789dh9ashd9.com" , false );
        _test( "plasma-tv-display.com" , false );
        _test( "www.h-net.org" , true );
        _test( "www.systransoft.com" , true );

        _test( "www.gatehouseinternational.co.uk" , false );
    }
    
    public static void main( String args[] )
        throws Exception {
        
        DEBUG = true;
        TRACE_ALL = true;

        _cache.remove( new Name( "browse.barnesandnoble.com" ) ,  Type.A );
        
        if ( args.length > 0 && args[0].trim().length() > 0 ){
            if ( args[0].equals("-dump") ){
                _cache.dump( System.out );
            }
            else {
                System.out.println( lookupAll( new Name( args[0] + "." ) ) );
            }
        }
        else {
            _test();

            _cache.add( new NSRecord( new Name( "com." ) , DClass.IN , TimeConstants.S_MONTH , new Name("foo.foo.com." ) ) , 
                        new Name( "foo.com." ) );
        }
        
    }
    
}
