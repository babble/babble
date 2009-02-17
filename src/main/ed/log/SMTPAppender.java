// SMTPAppender.java

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

package ed.log;

import ed.js.JSObjectBase;
import ed.js.engine.Scope;
import ed.net.DNSUtil;
import ed.util.FastQueue;
import ed.util.MailUtil;
import ed.util.SMTP;

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
     * @param smtp SMTP object
     * @param toEmail who to send alerts to
     * @param interval ms between emails
     */
    SMTPAppender( SMTP smtp, String to, long interval ) {
        this( smtp, to, null, interval, null );
    }

    /**
     * @param fromEmail the email the message should seems like it comes from
     * @param toEmail who to send alerts to
     * @param interval ms between emails
     */
    SMTPAppender( String toEmail , String fromEmail, long interval ) {
        this( new SMTP(), toEmail, fromEmail, interval, null );
    }

    SMTPAppender( String toEmail , String fromEmail, long interval, String logger ) {
        this( new SMTP(), toEmail, fromEmail, interval, logger );
    }

    SMTPAppender( SMTP smtp, String toEmail, String fromEmail, long interval, String logger ) {
        _smtp = smtp;

        if( fromEmail != null ) 
            _smtp.setFrom( fromEmail );

        _toEmail = toEmail;

        _formatter = new EventFormatter.DefaultEventFormatter();
        _interval = interval;
        _lastRun = System.currentTimeMillis();

        _loggerName = logger == null ? DNSUtil.getLocalHostString() : logger;
    }
    
    public void append( Event e ){
        if( _firstMessage ) {
            long now = System.currentTimeMillis();
            try {
                _smtp.sendMessage( _toEmail, getSubject( now ), _formatter.format( e ) );
            }
            catch( MessagingException ex ) {}
            _lastRun = now;
            _firstMessage = false;
        }
        else {
            String s = _formatter.format( e );
            _q.add( s );
        }
    }

    public void run() {
        while( true ) {
            long now = System.currentTimeMillis();
            try {
                if( now - _lastRun >= _interval && _q.size() > 0 ) {
                    _smtp.sendMessage( _toEmail, getSubject( now ), getText() );
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
    private String getText() {
        StringBuilder m = new StringBuilder();
        while( _q.size() > 0 ) {
            m.append( _q.poll() );
        }
        return m.toString();
    }

    private String getSubject( long now ) {
        return _loggerName + ": " + 
            new Date( _lastRun ) + " to " + new Date( now );
    }

    public void setMailInfo( String server, int port, boolean ssl ) {
        _smtp.setServer( server );
        _smtp.setPort( port );
        _smtp.setSSL( ssl );
    }

    public void setAccountInfo( String username, String password ) {
        _smtp.setUsername( username );
        _smtp.setPassword( password );
    }

    private boolean _firstMessage = true;
    private final long _interval;
    private long _lastRun;

    private final String _toEmail;

    private final EventFormatter _formatter;
    private SMTP _smtp;

    private final FastQueue<String> _q = new FastQueue<String>();

    private String _loggerName;
}
