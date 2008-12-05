// SMTPAppender.java

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

package ed.log;

import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.net.DNSUtil;
import ed.util.FastQueue;
import ed.util.MailUtil;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.smtp.SMTPSSLTransport;

/**
 * turns log messages into emails and sends them
 * the subject should be the logger name
 * all the other info should go in the body
 */
public class SMTPAppender extends Thread implements Appender {

    /**
     * @param fromEmail the email the message should seems like it comes from
     * @param toEmail who to send alerts to
     */
    SMTPAppender( String toEmail , String fromEmail, long interval ) {
        this( toEmail, fromEmail, interval, DNSUtil.getLocalHostString() );
    }

    SMTPAppender( String toEmail , String fromEmail, long interval, String logger ) {
        _toEmail = toEmail;
        _fromEmail = fromEmail;

        _formatter = new EventFormatter.DefaultEventFormatter();
        _interval = interval;
        _lastRun = System.currentTimeMillis();

        _loggerName = logger;
    }
    
    public void append( Event e ){
        String s = _formatter.format( e );
        _q.add( s );

        if( _firstMessage ) {
            long now = System.currentTimeMillis();
            try {
                createMessage();
                setSubject( now );
                sendMessage();
            }
            catch( MessagingException ex ) {}
            _lastRun = now;
            _firstMessage = false;
        }
    }

    public void start() {
        _session = MailUtil.createSession( _props , _username, _password );
        _message = new SMTPMessage( _session );

        try {
            _message.setRecipient( Message.RecipientType.TO, new InternetAddress( _toEmail ) );

            // cover all the bases on "from:" field
            _message.setEnvelopeFrom( _fromEmail );
            _message.setSubmitter( _fromEmail );
            _message.setFrom( new InternetAddress( _fromEmail ) );
            _message.setSender( new InternetAddress( _fromEmail ) );
        }
        catch( MessagingException e ) {
            e.printStackTrace();
            return;
        }

        super.start();
    }

    public void run() {
        while( true ) {
            long now = System.currentTimeMillis();
            try {
                if( now - _lastRun >= _interval && _q.size() > 0 ) {
                    createMessage();
                    setSubject( now );
                    sendMessage();
                    _lastRun = now;
                }
                sleep( (_lastRun + _interval) - now );
            }
            catch( MessagingException e ) {}
            catch( InterruptedException e ) {}
        }
    }

    /** Empties the queue and turns any messages in it into the body of an email
     */
    private void createMessage() 
        throws MessagingException {

        StringBuilder m = new StringBuilder();
        while( _q.size() > 0 ) {
            m.append( _q.poll() );
        }
        _message.setText( m.toString() );
    }

    private void setSubject( long now) 
        throws MessagingException {
        _message.setSubject( _loggerName + ": " + 
                             new Date( _lastRun ) + " to " + new Date( now ) );
    }

    private void sendMessage()
        throws MessagingException {

        if( _ssl ) 
            SMTPSSLTransport.send( _message );
        else
            SMTPTransport.send( _message );
    }

    public void setMailInfo( String server, int port, boolean ssl ) {
        _props = new Properties();
        _props.setProperty( "mail.smtp.host" , server );
        _props.setProperty( "mail.smtp.auth" , "true" );
        _props.setProperty( "mail.smtp.port" , port+"" );
        _props.setProperty( "mail.smtp.socketFactory.port" , port+"" );
        _props.setProperty( "mail.smtp.socketFactory.fallback" , "false" );
        
        _ssl = ssl;
        if( _ssl )
            _props.setProperty( "mail.smtp.socketFactory.class" , "javax.net.ssl.SSLSocketFactory" );
    }

    public void setAccountInfo( String username, String password ) {
        _username = username;
        _password = password;
    }

    private boolean _firstMessage = true;
    private final long _interval;
    private long _lastRun;

    private final String _fromEmail;
    private final String _toEmail;

    private String _username = "";
    private String _password = "";

    private final EventFormatter _formatter;
    private Session _session;
    private SMTPMessage _message;
    private Properties _props;
    private boolean _ssl = false;

    private final FastQueue<String> _q = new FastQueue<String>();

    private String _loggerName;
}
