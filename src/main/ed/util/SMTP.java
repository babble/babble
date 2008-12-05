// SMTP.java

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

    public SMTP() {
        _props = new Properties();
        _props.setProperty( "mail.smtp.auth" , "true" );
        _props.setProperty( "mail.smtp.socketFactory.fallback" , "false" );
    }

    public void sendMessage( String to, String subject, String message ) 
        throws MessagingException {

        setTo( to );
        setSubject( subject );
        setMessage( message );

        send();
    }

    public void setFrom( String from ) { _fromEmail = from; }
    public void setGmail() {
        setSSL( true );
        setPort( 465 );
        setServer( "smtp.gmail.com" );
    }

    public void setMessage( String m ) { _text = m; }
    public void setPassword( String p ) { _password = p; }
    public void setPort( int p ) { _port = p; }
    public void setProperty( String s, Object o ) { _props.setProperty( s, o + "" ); }

    public void setServer( String s ) { _server = s; }
    public void setSSL( boolean ssl ) {
        _ssl = ssl;
        if( _ssl )
            setProperty( "mail.smtp.socketFactory.class" , "javax.net.ssl.SSLSocketFactory" );
        else
            _props.remove( "mail.smtp.socketFactory.class" );
    }
    public void setSubject( String subject ) { _subject = subject; }
    public void setTo( String to ) { _toEmail = to; }
    public void setUsername( String u ) { _username = u; }

    private void _setter() 
        throws MessagingException {

        setProperty( "mail.smtp.host" , _server );
        setProperty( "mail.smtp.port" , _port );
        setProperty( "mail.smtp.socketFactory.port" , _port );
        
        _session = MailUtil.createSession( _props , _username, _password );
        _message = new SMTPMessage( _session );

        _message.setEnvelopeFrom( _fromEmail );
        _message.setSubmitter( _fromEmail );
        _message.setFrom( new InternetAddress( _fromEmail ) );
        _message.setSender( new InternetAddress( _fromEmail ) );

        _message.setRecipient( Message.RecipientType.TO, new InternetAddress( _toEmail ) );

        _message.setSubject( _subject );
        _message.setText( _text );
    }

    private void send() 
        throws MessagingException {

        _setter();

        if( _ssl ) 
            SMTPSSLTransport.send( _message );
        else
            SMTPTransport.send( _message );
    }

    private String _fromEmail = null;
    private String _toEmail = null;
    private String _subject = "";
    private String _text = "";

    private String _username = "";
    private String _password = "";

    private String _server = "";
    private int _port = 25;
    private boolean _ssl = false;

    private Session _session;
    private SMTPMessage _message;
    private Properties _props;
}
