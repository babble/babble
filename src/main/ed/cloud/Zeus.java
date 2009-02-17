// Zeus.java

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

package ed.cloud;

import java.io.*;
import java.net.*;

import com.zeus.soap.zxtm._1_0.*;

import ed.io.*;
import ed.util.*;

public class Zeus {

    public Zeus( String host , String username , String password )
        throws IOException {
        _host = host;
        _username = username;
        _password = password;
        
        _url = new URL( "https://" + username + ":" + password + "@" + host + ":9090/soap" );
    }
    
    public String getRule( String name )
        throws Exception {

        boolean found = false;
        
        String all[] = _getCatalogRulePort().getRuleNames();
        for ( int i=0; i<all.length; i++ )
            if ( all[i].equals( name ) )
                found = true;

        if ( ! found )
            return null;

        CatalogRuleRuleInfo infos[] = _getCatalogRulePort().getRuleDetails( new String[]{ name } );
        if ( infos == null || infos.length == 0 )
            return null;
        
        return infos[0].getRule_text();
    }

    public void setRule( final String name , String text )
        throws Exception {

        if ( name == null || text == null )
            throw new NullPointerException( "name or text is null" );

        CatalogRuleSyntaxCheck check[] = _getCatalogRulePort().checkSyntax( new String[]{ text } );
        if ( ! check[0].isValid() )
            throw new RuntimeException( "errors : " + check[0].getErrors() );

        String old = getRule( name );
        
        if ( old != null ){
            _getCatalogRulePort().setRuleText( new String[]{ name } , new String[]{ text } );
        }
        else
            _getCatalogRulePort().addRule( new String[]{ name } , new String[]{ text } );

        _getCatalogRulePort().setRuleNotes( new String[]{ name } , new String[]{ "Auto Generated.  Last Modified : " + ( new java.util.Date() ) } ); 
    }

    String getResolverName( int back ){
        if ( back < 0 )
            throw new RuntimeException( "stupid back : " + back );
        String n = "resolver";
        if ( back > 0 )
            n += "-" + back;
        return n;
    }

    public void updateResolveRule()
        throws Exception {
        
        String old = getRule( "resolver" );
        final String n = generateResolveTS();        
        
	if ( old.equals( n ) )
	    return;

        if ( old == null ){
            throw new RuntimeException( "no old resolver rule - i'm dying" );
        }

	String diff = DiffUtil.computeDiff( old , n );
	
	if ( Math.abs( n.length() - old.length() ) > ( n.length() / 30 ) ){
	    System.out.println( diff );
            throw new RuntimeException( "too much has changed (length)" );
        }

	if ( diff.length() > ( n.length() / 30 ) ){
	    System.out.println( diff );
	    throw new RuntimeException( "too much has changed (diff)" );
	}

	System.out.println( "sanity check done.  old length:" + old.length() + " new length:" + n.length() );

        for ( int i=7; i>0; i-- ){
            old = getRule( getResolverName( i - 1 ) );
            if ( old != null )
                setRule( getResolverName( i ) , old );
        }

        setRule( "resolver" , n );
    }
    
    public static String generateResolveTS(){
        Cloud c = Cloud.getInstance();
        return c.evalFunc( "Cloud.Zeus.resolveTS" ).toString();
    }

    // --- crazy soap stuff ---

    private CatalogRulePort _getCatalogRulePort()
        throws Exception {
        if ( _catalogRulePort != null )
            return _catalogRulePort;
        CatalogRuleLocator l = new CatalogRuleLocator();
        _catalogRulePort = l.getCatalogRulePort( _url );
        return _catalogRulePort;
    }

    private PoolPort _getPoolPort()
        throws Exception {
        if ( _poolPort != null )
            return _poolPort;
        PoolLocator l = new PoolLocator();
        _poolPort = l.getPoolPort( _url );
        return _poolPort;
    }

    // --- fields ----

    private CatalogRulePort _catalogRulePort;
    private PoolPort _poolPort;

    private final String _host;
    private final String _username;
    private final String _password;

    private final URL _url;

    // --- main ----
    
    public static void main( String args[] )
        throws Exception {

        File f = new File( ".zeuspw" );
        if ( f.exists() ){
            System.err.println( "going for real" );
            String pass = StreamUtil.readFully( new FileInputStream( ".zeuspw" ) ).trim();
            Zeus z = new Zeus( "iad-sb-n3.10gen.com" , "admin" , pass );
            z.updateResolveRule();
        }
        else {
            System.out.println( "DEBUG MODE" );
            System.out.println( generateResolveTS() );
        }

    }

}
