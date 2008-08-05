// URLFixer.java

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

package ed.appserver;

import java.io.*;

import ed.util.*;
import ed.net.httpserver.*;

public class URLFixer {

    public static final boolean NOCDN = Config.get().getBoolean( "NO-CDN" );

    public URLFixer( HttpRequest request , AppRequest ar ){
        this( getStaticPrefix( request , ar ) , getStaticSuffix( request , ar ) , ar.getContext() );
        _ar = ar;
    }
    
    public URLFixer( String cdnPrefix , String cdnSuffix , AppContext context ){
        _cdnPrefix = cdnPrefix;
        _cdnSuffix = cdnSuffix;
        _context = context;
    }
    
    public String fix( String url ){
        StringBuilder buf = new StringBuilder();
        fix( url , buf );
        return buf.toString();
    }

    public void fix( String url , Appendable a ){
        
        if ( url == null )
            return;
        
        
        if ( url.length() == 0 )
            return;

        
        // parse out options
        
        boolean nocdn = false;
        boolean forcecdn = false;
	
        if ( url.startsWith( "NOCDN" ) ){
            nocdn = true;
            url = url.substring( 5 );
        }
        else if ( url.startsWith( "CDN/" ) ){
            forcecdn = true;
            url = url.substring( 3 );
        }
	
        boolean doVersioning = true;
        
        // weird special cases
        if ( ! url.startsWith( "/" ) ){
            if ( _ar == null || url.startsWith( "http://" ) || url.startsWith( "https://" ) ){
                nocdn = true;
                doVersioning = false;
            }
            else {
                url = _ar.getDirectory() + url;
            }
        }
        
        if ( url.startsWith( "//" ) ){ // this is the special //www.slashdot.org/foo.jpg syntax
            nocdn = true;
            doVersioning = false;
        }
        
        // setup 
        
        String uri = url;
        int questionIndex = url.indexOf( "?" );
        if ( questionIndex >= 0 )
            uri = uri.substring( 0 , questionIndex );
        
        String cdnTags = null;
        if ( uri.equals( "/~f" ) || uri.equals( "/~~/f" ) ){
            cdnTags = ""; // TODO: should i put a version or timestamp here?
        }
        else {
            cdnTags = _cdnSuffix;
            if ( cdnTags == null )
                cdnTags = "";
            else if ( cdnTags.length() > 0 )
                cdnTags += "&";
            
            if ( doVersioning && _context != null ){
                File f = _context.getFileSafe( uri );
		if ( f == null )
		    cdnTags += "lm=cantfind";
		else if ( ! f.exists() )
		    cdnTags += "lm=doesntexist";
		else
                    cdnTags += "lm=" + f.lastModified();
            }
        }
	
        // print
        
        try {
            if ( forcecdn || ( ! nocdn && cdnTags != null ) )
                a.append( cdnPrefix() );
            
            a.append( url );
            
            if ( cdnTags != null && cdnTags.length() > 0 ){
                if ( questionIndex < 0 )
                    a.append( "?" );
                else
                    a.append( "&" );
                a.append( cdnTags );
            }
        }
        catch ( IOException ioe ){
            throw new RuntimeException( "couldn't append" , ioe );
        }
        
    }

    public String getCDNPrefix(){
        return cdnPrefix();
    }
    public String getCDNSuffix(){
        return _cdnSuffix;
    }

    public String setCDNPrefix( String s ){
        _cdnPrefix = s;
        return cdnPrefix();
    }
    public String setCDNSuffix( String s ){
        _cdnSuffix = s;
        return _cdnSuffix;
    }
   
    String cdnPrefix(){
        if ( _ar != null && _ar.isScopeInited() ){
            Object foo = _ar.getScope().get( "CDN" );
            if ( foo != null )
                return foo.toString();
        }
        
        return _cdnPrefix;
    }

    static String getStaticPrefix( HttpRequest request , AppRequest ar ){
        
        if ( NOCDN )
            return "";
        
        String host = ar.getHost();
        
        if ( host == null )
            return "";

        if ( host.indexOf( "." ) < 0 )
            return "";
        
        if ( request.getPort() > 0 )
            return "";

        if ( request.getHeader( "X-SSL" ) != null )
            return "";

        String prefix= "http://static";

        if ( host.indexOf( "local." ) >= 0 )
            prefix += "-local";
        
        prefix += ".10gen.com/" + host;
        return prefix;
    }

    static String getStaticSuffix( HttpRequest request , AppRequest ar ){
        final AppContext ctxt = ar.getContext();
        return "ctxt=" + ctxt.getEnvironmentName() + "" + ctxt.getGitBranch() ;
    }

    private final AppContext _context;
    private AppRequest _ar;

    private String _cdnPrefix;
    private String _cdnSuffix;

            
}
