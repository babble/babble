// URLFixer.java

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

package ed.appserver;

import java.io.*;

import ed.util.*;
import ed.net.httpserver.*;

public class URLFixer {

    public static final boolean NOCDN = Config.get().getBoolean( "NO-CDN" );
    public static final String LM404 = "doesntexist";

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
        // don't rewrite w/in js files
        if (_ar != null && _ar.getResponse() != null && _ar.getResponse().getContentType() != null) {
            String content_type = _ar.getResponse().getContentType();
            if (content_type.indexOf("javascript") != -1 || content_type.indexOf("ecmascript") != -1) {
                try {
                    a.append(url);
                }
                catch (IOException ioe) {
                    throw new RuntimeException("couldn't append", ioe);
                }

                return;
            }
        }

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

            if ( url.contains( "ctxt=" ) || cdnTags == null )
                cdnTags = "";

            if ( doVersioning && _context != null && ! url.contains( "lm=" ) ){
                File f = _context.getFileSafe( uri );
                if ( f == null )
                    cdnTags = _urlAppendNameValue( cdnTags , "lm=cantfind" );
                else if ( ! f.exists() )
                    cdnTags = _urlAppendNameValue( cdnTags , "lm=" + LM404 );
                else
                    cdnTags = _urlAppendNameValue( cdnTags , "lm=" + f.lastModified() );
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

        if ( request.getPort() > 0 )
            return "";

        if ( request.getHeader( "X-SSL" ) != null )
            return "";

        String prefix= "http://static";

        if ( host.indexOf( "local." ) >= 0 )
            prefix += "-local";

        prefix += "." + Config.getExternalDomain() + "/" + host;
        return prefix;
    }

    static String _urlAppendNameValue( String base , String extra ){
        if ( base == null || base.length() == 0 )
            return extra;

        if ( base.endsWith( "&" ) )
            return base + extra;

        return base + "&" + extra;
    }

    static String getStaticSuffix( HttpRequest request , AppRequest ar ){
        final AppContext ctxt = ar.getContext();
        String suffix = "ctxt=" + ctxt.getEnvironmentName() + "" + ctxt.getGitBranch();
        if ( ctxt.getGitHash() != null )
            suffix += "-" + ctxt.getGitHash();
        return suffix;
    }

    private final AppContext _context;
    private AppRequest _ar;

    private String _cdnPrefix;
    private String _cdnSuffix;


}
