// Simulator.java

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

package ed.net.lb;

import java.lang.InterruptedException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import ed.cloud.Cloud;
import ed.js.JSArray;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.net.DNSUtil;
import ed.net.httpserver.HttpRequest;
import ed.net.httpserver.HttpResponse;
import ed.net.httpserver.HttpServer;
import ed.net.nioclient.NIOClient;
import ed.db.DBBase;
import ed.db.DBCollection;

public class Simulator { 

    private static DBBase db;

    public Simulator() 
        throws IOException {

        // connect to db
        db = (DBBase)Cloud.getInstance().getScope().get("db");

        // set up local pseudo servers
        Iterator<JSObject> pools;
        for( pools = db.getCollection( "pools" ).find(); pools.hasNext(); ) {
            JSObject pool = pools.next();

            // "machines" should never be null
            if( pool.get( "machines" ) == null ) 
                continue;

            // find the port of each machine
            for( Object o : (JSArray)pool.get( "machines" ) ) {
                String s = o.toString();
                int port = Integer.parseInt( s.substring( s.indexOf( ":" ) + 1 ) );
                if( !_portsUsed.contains( port ) ) {
                    _servers.add( new HttpServer( port ) );
                    _portsUsed.add( port );
                }
                _basePort = Math.max( port, _basePort );
            }
        }

        // set up router
        _router = new Router( new GridMapping.Factory() );
    }

    public void doStuff() 
        throws IOException,
               InterruptedException {

        // get db connection

        // default strings
        String url = null;
        String name = "";
        String machine = "";
        long interval = 10;
        long totalTime = 100;
        int num = 1;

        final String intro = "\nr - send requests\n" + 
            "p - add/edit/remove pools\n" + 
            "s - add/edit/remove sites\n" +
            "v - view current configuration\n" +
            "k - kill/raise a server\n" +
            "\nWhat would you like to do? ";
        System.out.print( intro );

        InputStreamReader isr = new InputStreamReader( System.in );
        char r;
        while( ( r = getChar( isr ) ) != -1 ) {
            switch ( r ) {
            //request
            case 'r' :
                System.out.println( "defaults: \n"+
                                    "\turl: "+url+"\n" +
                                    "\tinterval: "+interval+"\n" +
                                    "\ttotal time: "+totalTime+"\n" );

                System.out.print( "\turl: " );
                url = readOpt( isr, url );

                System.out.print( "\tsend a request every: (ms) " );
                interval = Long.parseLong( readOpt( isr, interval+"" ) );

                System.out.print( "\tfor: (ms) " );
                totalTime = Long.parseLong( readOpt( isr, totalTime+"" ) );

                HttpRequest request = HttpRequest.getDummy( url, "Host: "+url );

                _cal = Calendar.getInstance();
                long current = _cal.getTimeInMillis();
                long end = current + totalTime;
                do {
                    InetSocketAddress addr = _router.chooseAddress( request, false );
                    System.out.println( "chosen address: "+addr );
                    if( addr == null ) {
                        System.out.println( "no viable server found." );
                    }
                    else if( _deadAddresses.contains( addr.getPort() ) ) {
                        _router.error( request, 
                                       (HttpResponse)null, 
                                       addr, 
                                       NIOClient.ServerErrorType.SOCK_TIMEOUT, 
                                       new RuntimeException( "faking pool outage at " + addr ) );
                    }
                    else {
                        _router.success( request, (HttpResponse)null, addr );
                    }

                    try { Thread.sleep( interval ); }
                    catch( InterruptedException e ) {}

                    _cal = Calendar.getInstance();
                    current = _cal.getTimeInMillis();
                }
                while( current < end );

                System.out.println(  );
                break;

            // add/remove pool
            case 'p' :
            // add/remove site
            case 's' :
                char action  = r;
                DBCollection collection = action == 'p' ? 
                    db.getCollection( "pools" ) : db.getCollection( "sites" );
                    
                JSObject pool = getPoolOrSite( isr );
                boolean remove = false;
                // use the { name : "foo" } obj to remove or replace an existing mapping
                JSObject temp = collection.findOne( pool );
                if( temp != null ) {
                    System.out.print("\tThis "+( action == 'p' ? "pool" : "site" ) + " exists. [r]emove/[e]dit? ");
                    r = getChar( isr );
                    
                    // remove a pool
                    if( r == 'r' ) {
                        collection.remove( pool );
                        break;
                    }
                    pool = temp;
                }

                JSArray machines = new JSArray();
                // find the array of machines
                if( action == 'p' ) { 
                    System.out.print( "\tnumber of machines: " );
                    num = Integer.parseInt( readOpt( isr, "1" ) );

                    System.out.print( "\tstarting port: (default: "+(_basePort+1)+") " );
                    int port = Integer.parseInt( readOpt( isr, (_basePort+1)+"" ) );
                    for( int i=port; i<port + num; i++ ) {
                        machines.add( "localhost:" + i );
                        if( !_portsUsed.contains( i ) ) {
                            _servers.add( new HttpServer( i ) );
                            _portsUsed.add( i );
                        }
                    }
                }
                else {
                    System.out.println( "\tenvironment" );
                    System.out.print( "\t\t(field 1 of 2) name: " );
                    
                    while( !(machine = readOpt( isr, "" )).equals( "" ) ) {
                        machines.add( getEnvironment( isr, machine ) );
                        // reprompt
                        System.out.println( "\tenvironment" );
                        System.out.print( "\t\t(field 1 of 2) name: " );
                    }
                }
                pool.set( action == 'p' ? "machines" : "environments", machines );
                
                // save the { name : "", machines : [] } to the db
                collection.save( pool );
                System.out.println( "saved " + (action == 'p' ? "pool " : "site " ) + name );

                _router = new Router( new GridMapping.Factory() );
                break;

            case 'v' :
                printCollection();
                break;

            case 'k' :
                killAddress( isr );
                break;

            default :
                break;
            }
            
            System.out.print( intro );
        }

        isr.close();
    }

    // todo: print aliases
    private static void printCollection() {
        printEnvironments();
        printPools();
    }

    private static void printEnvironments() {
        StringBuilder sb = new StringBuilder();

        Iterator<JSObject> cursor;
        for( cursor = db.getCollection( "sites" ).find(); cursor.hasNext(); ) {
            JSObject site = cursor.next();
            JSArray aliases = new JSArray();
            System.out.println( "site " + site.get( "name" ) );
            if( site.get( "environments" ) != null ) {
                for( Object obj : (JSArray)site.get( "environments" ) ) {
                    JSObject o = (JSObject)obj;
                    System.out.println( "\t" + o.get( "name" ) + " : " + o.get( "pool" ) );

                    if( o.get( "aliases" ) != null ) {
                        for( Object aliaso : (JSArray)o.get( "aliases" ) ) {
                            JSObject alias = new JSObjectBase();
                            alias.set( "alias", aliaso.toString() );
                            alias.set( "name", o.get( "name" ).toString() );
                            aliases.add( alias );
                        }
                    }
                }
            }
            if( aliases.size() > 0 ) {
                System.out.println( "site-alias: "+site.get( "name" ) );
                for( Object a : aliases ) {
                    System.out.println( "\t" + ((JSObject)a).get( "alias" ) + " : " + 
                                        ((JSObject)a).get( "name" ) );
                }
            }
            System.out.println();
        }
    }

    private static void printPools() {
        Iterator<JSObject> cursor;
        for( cursor = db.getCollection( "pools" ).find(); 
             cursor != null && cursor.hasNext(); ) {
            JSObject pool = cursor.next();
            System.out.println( "pool " + pool.get( "name" ) );
            for( Object obj : (JSArray)pool.get( "machines" ) ) {
                String addr = obj.toString();
                System.out.print( "\t" + obj );
                if( _deadAddresses.contains( Integer.parseInt( addr.substring( addr.indexOf( ":" ) + 1 ) ) ) )
                    System.out.print( " (dead)" );
                System.out.println();
            }
            System.out.println();
        }
    }

    private static void killAddress( InputStreamReader isr ) 
        throws IOException {
        printPools();

        System.out.print( "which address would you like to kill? " );
        int port = Integer.parseInt( readOpt( isr, "" ) );

        System.out.print( "for how long? (ms, leave blank for forever) " );
        long time = Long.parseLong( readOpt( isr, "0" ) );

        Strangler s = new Strangler( port, time );
        s.setDaemon( true );
        s.start();
    }

    static class Strangler extends Thread {
        public Strangler( int port, long time ) {
            super( "Strangle "+port );
            if( !_deadAddresses.contains( port ) )
                _deadAddresses.add( port );
            _p = port;
            _t = time;
        }

        public void run() {
            if( _t == 0 ) 
                return;

            try {
                Thread.sleep( _t );
            }
            catch( InterruptedException e ) {}
            _deadAddresses.remove( new Integer( _p ) );
        }

        int _p;
        long _t;
    }

    // add alias list
    private static JSArray addAliases( InputStreamReader isr, String machine ) 
        throws IOException {

        System.out.print( "\tset aliases for " + machine + "? ([y]es/[n]o) " );

        JSArray aliases = new JSArray(); 
        char r = getChar( isr );
        if( r == 'y' ) {
            String alias;
            System.out.print( "\t\talias: " );
            while( !(alias = readOpt( isr, "" )).equals( "" ) ) {
                aliases.add( alias );
                System.out.print( "\t\talias: " );
            }
        }
        return aliases;
    }

    // add environment name/pool/alias
    private static JSObject getEnvironment( InputStreamReader isr, String name ) 
        throws IOException {
        JSObject environment = new JSObjectBase();
        environment.set( "name", name );

        System.out.print( "\t\t(field 2 of 2) pool: " );
        environment.set( "pool", readOpt( isr, "" ) );
        environment.set( "aliases", addAliases( isr, name ) );
        return environment;
    }

    private static JSObject getPoolOrSite( InputStreamReader isr ) 
        throws IOException {

        JSObject pool = new JSObjectBase();
        System.out.print( "\tname: " );
        String name = readOpt( isr, "" );
        pool.set( "name", name );

        return pool;
    }

    private static char getChar( InputStreamReader isr ) 
        throws IOException {
        char r = (char) isr.read();
        while( r != '\n' && isr.read() != '\n' );
        return r;
    }

    private static String readOpt( InputStreamReader isr, String defaultStr ) 
        throws IOException { 

        char r;
        char temp[] = new char[100];
        int i=0;
        while( ( r = (char)isr.read() ) != '\n' ) {
            temp[i++] = r;
        }
        if( i == 0 )
            return defaultStr;
        return new String( temp, 0, i );

    }

    public static void main( String[] args ) 
        throws Exception {

        _timed = true;
        _memTracked = true;

        Simulator sim = new Simulator();
        sim.doStuff();
    }

    private static int _port = 8080;
    private static int _basePort = 14520;
    private static int _verbose = 0;
    private static boolean _timed = false;
    private static boolean _memTracked = false;
    private static Calendar _cal;
    private static ArrayList<Integer> _deadAddresses = new ArrayList<Integer>();
    private static ArrayList<Integer> _portsUsed = new ArrayList<Integer>();

    private Router _router;
    private ArrayList<HttpServer> _servers = new ArrayList<HttpServer>();
}

