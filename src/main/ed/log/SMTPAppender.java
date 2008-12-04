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

/**
 * turns log messages into emails and sends them
 * the subject should be the logger name
 * all the other info should go in the body
 */
public abstract class SMTPAppender implements Appender {

    /**
     * @param fromEmail the email the message should seems like it comes from
     * @param toEmail who to send alerts to
     */
    SMTPAppender( String server , int port , boolean ssl , String fromEmail , String toEmail ){
        
    }
    
    public void append( Event e ){
        throw new RuntimeException( "not implemented yet" );
    }
}
