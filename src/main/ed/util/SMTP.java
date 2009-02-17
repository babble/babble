// SMTP.java

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

package ed.util;

import ed.util.MailUtil;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.smtp.SMTPSSLTransport;

/**
 * A wrapper for Java's SMTP stuff
 */
public class SMTP {

    public SMTP() {}

    /**
     * Simple message sender.  Uses whatever parameters have been set.
     * Identical to send()
     */
    public void sendMessage( String to, String subject, String message ) 
        throws MessagingException {
        send( to, subject, message );
    }

    public void setFrom( String from ) { 
        _fromEmail = from; 
    }

    /**
     * Sets up paramters to use gmail: SSL on, the port to 465, and the server to smtp.gmail.com.
     */
    public void setGmail() {
        setSSL( true );
        setPort( 465 );
        setServer( "smtp.gmail.com" );
    }

    public void setPassword( String p ) { 
        _password = p; 
    }

    public void setPort( int p ) { 
        _port = p; 
    }

    public void setProperty( String s, Object o ) { 
        _props.setProperty( s, o + "" ); 
    }

    public void setServer( String s ) { 
        _server = s; 
    }

    public void setSSL( boolean ssl ) {
        _ssl = ssl;
        if( _ssl )
            setProperty( "mail.smtp.socketFactory.class" , "javax.net.ssl.SSLSocketFactory" );
        else
            _props.remove( "mail.smtp.socketFactory.class" );
    }

    public void setUsername( String u ) { 
        _username = u; 
    }

    private void _setter( String to, String subject, String text ) 
        throws MessagingException {
        if( to == null || to.equals( "" ) ) 
            throw new RuntimeException( "no to address given, unable to send" );
        if( subject == null || subject.equals( "" ) )
            System.out.println( "Warning: email has no subject line" );
        if( text == null || text.equals( "" ) )
            System.out.println( "Warning: email has no body" );

        setProperty( "mail.smtp.host" , _server );
        setProperty( "mail.smtp.port" , _port );
        setProperty( "mail.smtp.socketFactory.port" , _port );
        
        _session = MailUtil.createSession( _props , _username, _password );
        _message = new SMTPMessage( _session );

        if( _fromEmail != null && !_fromEmail.equals( "" ) ) {
            _message.setEnvelopeFrom( _fromEmail );
            _message.setSubmitter( _fromEmail );
            _message.setFrom( new InternetAddress( _fromEmail ) );
            _message.setSender( new InternetAddress( _fromEmail ) );
        }

        _message.setRecipient( Message.RecipientType.TO, new InternetAddress( to ) );

        _message.setSubject( subject );
        _message.setText( text );
    }

    /**
     * Sends a message using whatever parameters have been set.
     */
    private void send( String to, String subject, String message ) 
        throws MessagingException {

        _setter( to, subject, message );

        if( _ssl ) 
            SMTPSSLTransport.send( _message );
        else
            SMTPTransport.send( _message );
    }

    private String _fromEmail = null;

    private String _username = "";
    private String _password = "";

    private String _server = "";
    private int _port = 25;
    private boolean _ssl = false;

    private Session _session;
    private SMTPMessage _message;
    private Properties _props = new Properties( SMTP.permaProps );

    public static Properties permaProps = new Properties();
    static {
        permaProps.setProperty( "mail.smtp.auth" , "true" );
        permaProps.setProperty( "mail.smtp.socketFactory.fallback" , "false" );
    }
}
