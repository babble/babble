// MailUtil.java

package ed.util;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {

    public static Session createSession( final Properties props , final String user , final String pass ){

        return Session.getDefaultInstance( props , new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication( user , pass );
                }
            });        
    }
}
