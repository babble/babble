// Github.java

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

package ed.git;

import java.io.*;
import java.util.regex.*;

import ed.js.*;

public class Github implements GitHost {
    
    public GitIdentity createAccount( String username , String email , String password )
        throws GitException {

        XMLHttpRequest req = _go( null , true , "users" , false ,
                                  "user[login]" , username ,
                                  "user[email]" , email , 
                                  "user[password]" , password , 
                                  "user[password_confirmation]" , password  
                                  );

        String txt = req.getResponseText();

        if ( txt.contains( "Login has already been taken" ) )
            throw new GitException.UsernameTaken( username );

        if ( txt.contains( "Email is already taken" ) )
            throw new GitException.UsernameTaken( email );
        
        return getIdent( username , password );
    }
    
    public GithubIdent getIdent( String username , String password )
        throws GitException {
        
        XMLHttpRequest req = _go( null , true , "session" , false ,
                                  "login" , username , 
                                  "password" ,  password
                                  );

        
        StringBuilder cookieHeader = new StringBuilder();
        JSObject cookies = (JSObject)req.get( "cookies" );
        if ( cookies != null ){
            for ( String name : cookies.keySet() )
                cookieHeader.append( name ).append( "=" ).append( cookies.get( name ).toString() ).append( "; " );
        }
        
        req = new XMLHttpRequest( "GET" , "https://github.com/account" );
        req.setRequestHeader( "Cookie" , cookieHeader.toString() );
        _dl( req , null );
        
        Matcher m = Pattern.compile( "github.token\\s+(\\w+)\\b" ).matcher( req.getResponseText() );
        if ( ! m.find() )
            throw new GitException( "can't find token" );
        
        return new GithubIdent( username , m.group(1) , cookieHeader.toString() );
    }
    
    public void forkRepository( GitIdentity who , String whatToFork )
        throws GitException {
        XMLHttpRequest x = _go( who , false , whatToFork + "/fork" , true );
        if ( x.getResponseCode() != 200 )
            throw new GitException( "got wrong response code [" + x.getResponseCode() + "]" );

        try {
            // github takes some time to stabilize, so we don't want to rush it
            Thread.sleep( 10000 );
        }
        catch ( InterruptedException ie ){}

    }

    public void renameRepository( GitIdentity who , String from , String to )
        throws GitException {
        _go( who , true , who._usernane + "/" + from + "/edit/rename" , true , "name" , to );
    }
    
    
    public static class GithubIdent extends GitIdentity {
        
        public GithubIdent( String username , String token ){
            this( username , token , null );
        }
        
        GithubIdent( String username , String token , String cookieHeader ){
            super( username );
            _token = token;
            _cookieHeader = cookieHeader;
        }
        public String toString(){
            return "username:" + _usernane + " token:" + _token;
        }
        
        final String _token;
        String _cookieHeader;
    }

    XMLHttpRequest _go( GitIdentity ident , boolean secure , String path , boolean followRedirects , String ... params )
        throws GitException {

        GithubIdent gi = (GithubIdent)ident;
        
        String postData = null;
        if ( gi != null || params.length > 0 ){
            if ( gi == null )
                postData = _createPostData( params );
            else
                postData = _createPostData( _combine( params , "token" , gi._token , "login" , gi._usernane ) );
        }
        
        XMLHttpRequest x = _createRequest( postData != null , secure , path , followRedirects );
        
        if ( gi != null && gi._cookieHeader != null )
            x.setRequestHeader( "Cookie" , gi._cookieHeader );

        return _dl( x , postData );
    }
    
    XMLHttpRequest _dl( XMLHttpRequest x , String postData )
        throws GitException {
        
        if ( postData != null )
            x.setRequestHeader( "Content-Type" , "application/x-www-form-urlencoded"  );

        try {
            if ( x.send( postData ) == null )
                throw new GitException( "error talking to github" , x.getError() );
        }
        catch ( IOException ioe ){
            throw new GitException( "error talking to github" , ioe );
        }
        
        return x;        
    }

    String[] _combine( String[] old , String ... toAdd ){
        String[] arr = new String[old.length + toAdd.length];
        int i=0;
        for ( ; i<old.length; i++ )
            arr[i] = old[i];
        for ( int j=0; j<toAdd.length; j++ )
            arr[i++] = toAdd[j];
        return arr;
    }
    
    String _createPostData( String ... params ){

        if ( params.length % 2 != 0 )
            throw new IllegalArgumentException( "params have to be even" );

        StringBuilder formData = new StringBuilder();
        for ( int i=0; i<params.length; i+=2 )
            formData.append( params[i] ).append( "=" ).append( params[i+1] ).append( "&" );
        return formData.toString();
    }

    XMLHttpRequest _createRequest( boolean post , boolean secure , String path , boolean followRedirects ){
        XMLHttpRequest x = new XMLHttpRequest( post ? "POST" : "GET" , "http" + ( secure ? "s" : "" ) + "://github.com/" + path );

        if ( ! followRedirects )
            x.set( "nofollow" , true );
        
        return x;
    }
}
