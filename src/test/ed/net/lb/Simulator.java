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
import java.util.Calendar;
import java.util.Iterator;

import ed.cloud.Cloud;
import ed.js.JSArray;
import ed.js.JSObject;
import ed.js.JSObjectBase;
import ed.net.DNSUtil;
import ed.net.httpserver.HttpServerTest;
import ed.net.lb.LB;
import ed.db.DBBase;
import ed.db.DBCollection;

public class Simulator extends LB {

    public static class RequestSpawner extends Thread {
        public RequestSpawner( String s1, String s2, String s3, String s4, int num ) {
            super();

            _s = makeRequestString( s1, s2, s3, s4 );
            _num = num;
        }

        public void run() {
            try {
                sendRequests( _num, _s );
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }

        private void sendRequests( int num, String requests ) 
            throws IOException {

            _cal = Calendar.getInstance();
            long startTime = _cal.getTimeInMillis();
            for( int i=0; i<num; i++) {
                sendString( requests );
            }
            _cal = Calendar.getInstance();
            System.out.println( "Sent " + num + " request(s): " + requests + 
                              "\nTotal time: "+ ( _cal.getTimeInMillis()-startTime ));
        }

        private void sendString( String buf ) 
            throws IOException {

            Socket s = new Socket( DNSUtil.getMyAddresses().get(0) , _port );
            s.setSoTimeout( 1000 );
            s.getOutputStream().write( buf.getBytes() );
            InputStream in = s.getInputStream();
            HttpServerTest.Response r = HttpServerTest.read( in );
            if( _verbose == 1 ) {
                System.out.println( r );
            }
        }
        private String makeRequestString( String method, String url, String params, String headers ) {
            StringBuilder buf = new StringBuilder();
            buf.append(method).append(" ").append(url).append("?").append(params).append(" HTTP/1.1\r\n");
            buf.append("Host: ").append( url ).append("\r\n");
            buf.append(headers);
            buf.append("\r\n");
            return buf.toString();
        }

        String _s;
        int _num = 1;
    }

    public Simulator() 
        throws IOException {
        super( _port, new GridMapping.Factory(), _verbose );
    }

    public void doStuff() 
        throws IOException,
               InterruptedException {

        start();

        // get db connection
        DBBase db = (DBBase)Cloud.getInstance().getScope().get("db");

        // default strings
        String method = "GET";
        String url = null;
        String params = "";
        String headers = "";
        String name = "";
        String machine = "";
        int num = 1;

        final String intro = "\nr - send requests\n" + 
            "p - add/edit/remove pools\n" + 
            "s - add/edit/remove sites\n" +
            "v - view current configuration\n" +
            //            "k - kill a pool\n" +
            "\nWhat would you like to do? ";
        System.out.print( intro );

        InputStreamReader isr = new InputStreamReader( System.in );
        char r;
        while( ( r = getChar( isr ) ) != -1 ) {
            switch ( r ) {
            //request
            case 'r' :
                System.out.print( "\t[p]ost/[g]et: " );
                method = ( getChar( isr ) == 'p'  ? "POST" : "GET" );

                System.out.print( "\turl: " );
                url = readOpt( isr, url );

                System.out.print( "\tparams: " );
                params = readOpt( isr, params );

                System.out.print( "\theaders: " );
                headers = readOpt( isr, headers );

                System.out.print( "\tnumber of times: " );
                num = Integer.parseInt( readOpt( isr, num + "" ) );

                RequestSpawner spawn = new RequestSpawner( method, url, params, headers, num );
                spawn.start();
                spawn.join();
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

                // find the array of machines
                if( action == 'p' ) 
                    System.out.print( "\tmachine: " );
                else {
                    System.out.println( "\tenvironment" );
                    System.out.print( "\t\t(field 1 of 2) name: " );
                }

                JSArray machines = new JSArray();
                while( !(machine = readOpt( isr, "" )).equals( "" ) ) {
                    if( action == 'p' ) {
                        // add machine name
                        machines.add( machine );
                        System.out.print( "\tmachine: " );
                    }
                    else {
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

                System.out.println( "\nNote: Router will automatically refresh mapping "+
                                    "within 30 seconds or on restart" );

                break;

            case 'v' :
                printCollection( db );
                break;

                /*
            case 'k' :
                killPool( isr, db.getCollection( "pools" ) );
                break;
                */
            default :
                break;
            }
            
            System.out.print( intro );
        }

        isr.close();
        join();
        System.exit(0);
    }

    // todo: print aliases
    private static void printCollection( DBBase db ) {
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

        for( cursor = db.getCollection( "pools" ).find(); 
             cursor != null && cursor.hasNext(); ) {
            JSObject pool = cursor.next();
            System.out.println( "pool " + pool.get( "name" ) );
            for( Object obj : (JSArray)pool.get( "machines" ) ) {
                System.out.println( "\t" + obj );
            }
            System.out.println();
        }
    }

    /*
    private static void killPool( InputStreamReader isr, DBCollection pools ) 
        throws IOException {
        System.out.print( "which pool would you like to kill? " );
        String pool = readOpt( isr, "" );

        Iterator cursor;
        for( cursor = pools.find(); 
             cursor != null && cursor.hasNext(); ) {
            JSObject obj = (JSObject)cursor.next();
            JSArray foo = (JSArray)obj.get( "machines" );
            if( foo == null )
                continue;

            if( foo.remove( pool ) ) {
                foo.add( pool + "123456789" );
                pools.save( obj ); 
            }
        }
    }
    */

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

        // make load balancer
        Simulator sim = new Simulator();
        sim.doStuff();
    }

    private static int _port = 8080;
    private static int _verbose = 0;
    private static boolean _timed = false;
    private static boolean _memTracked = false;
    private static Calendar _cal;
}
