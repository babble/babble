// Zeus.java

package ed.cloud;

import java.io.*;
import java.net.*;

import com.zeus.soap.zxtm._1_0.*;

import ed.io.*;

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

        if ( old != null )
            _getCatalogRulePort().setRuleText( new String[]{ name } , new String[]{ text } );
        else
            _getCatalogRulePort().addRule( new String[]{ name } , new String[]{ text } );

        _getCatalogRulePort().setRuleNotes( new String[]{ name } , new String[]{ "Auto Generated.  Last Modified : " + ( new java.util.Date() ) } ); 
    }

    public void updateResolveRule()
        throws Exception {
        setRule( "resolver" , generateResolveTS() );
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

        String pass = StreamUtil.readFully( new FileInputStream( ".zeuspw" ) ).trim();
        Zeus z = new Zeus( "iad-sb-n3.10gen.com" , "admin" , pass );
        z.updateResolveRule();

    }

}
