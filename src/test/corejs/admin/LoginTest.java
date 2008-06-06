// JSONTest.java

package corejs.admin;

import ed.*;

import org.testng.annotations.Test;

import ed.js.*;
import ed.js.engine.*;

import com.meterware.httpunit.*;
import java.net.*;
import java.util.regex.*;

public class LoginTest extends ed.TestCase {

    @Test(groups = {"basic"})
    public void testLogin() throws Exception{
        // Change this URI if the port or hostname of the admin site changes
        URI uri = new URI("http://localhost:1338/admin/");
        WebConversation wc = new DigestConversation();
        WebResponse resp;
        try {
            resp = wc.getResponse(uri.toString());
            assert(false);
        }
        catch(AuthorizationRequiredException e){
            // OK; admin not allowed without login credentials
        }

        wc.setAuthorization("test@10gen.com", "test");
        resp = wc.getResponse(uri.toString());
        
        String text = resp.getText();
        Pattern p = Pattern.compile(".*Welcome.*");
        Matcher m = p.matcher(text);
        assert(m.find());
        
        wc.setAuthorization("notadmin@10gen.com", "notadmin");
        try {
            resp = wc.getResponse(uri.toString());
            m = p.matcher(resp.getText());
            assert(! m.find());
        }
        catch(AuthorizationRequiredException e){
            // OK; not admin, so not allowed in
        }

    }

    public static void main( String args[] ){
        (new LoginTest()).runConsole();
    }
}
