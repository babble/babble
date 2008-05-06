// JSONTest.java

package sites.admin;

import sites.*;

import org.testng.annotations.Test;

import ed.js.*;
import ed.js.engine.*;

import com.meterware.httpunit.*;

public class LoginTest extends ed.TestCase {

    @Test(groups = {"basic"})
    public void testLogin() throws Exception{
        WebConversation wc = new DigestConversation();
        wc.setAuthorization("test@10gen.com", "test");
        WebResponse resp = wc.getResponse("http://localhost:1338/admin/");
        
        System.out.println(resp.getText());
    }

    public static void main( String args[] ){
        (new LoginTest()).runConsole();
    }
}
