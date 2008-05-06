package sites;

import java.util.*;
import java.net.*;
import com.meterware.httpunit.*;

import com.twmacinta.util.*;

public class DigestConversation extends WebConversation {
    public WebResponse getResponse(String uri) 
        throws java.io.IOException, MalformedURLException, org.xml.sax.SAXException {
        WebResponse resp = null;
        try {
            resp = super.getResponse(uri);
        }
        catch (AuthorizationRequiredException e) {
            try {
                _authDigest(e, uri);
                resp = super.getResponse(uri);
            }
            catch(URISyntaxException e2){
                System.out.println("I hate your uri " + uri);
            }
        }
        return resp;
    }

    public void setAuthorization(String u, String p) {
        _username = u;
        _password = p;
        //super.setAuthorization(u, p);
    }
    
    public void _authDigest(AuthorizationRequiredException e, String uristring)
        throws MalformedURLException, URISyntaxException {
        HashMap<String, String> things = _parseDigestException(e);
        String username = _username;
        String password = _password;
        String mynonce = "1";    // super secure client nonce
        String noncecount = "1"; // super accurate nonce count
        URI u = new URI(uristring);
        String ha1 = md5(username+":"+things.get("realm")+":"+password);
        String ha2 = md5("GET:"+u.getPath());
        String response = md5(ha1+":"+things.get("nonce")+":"+noncecount+":"+
                              mynonce+":"+things.get("qop")+":"+ha2);

        String respstring = "Digest username=\""+username+"\", nonce=\""+
            things.get("nonce") + "\", qop=\""+things.get("qop")+"\", cnonce=\""+mynonce+
            "\", nc=\""+noncecount+"\", response=\""+response+"\"";
        System.out.println("Setting auth to " + respstring);
        this.setHeaderField("Authorization", respstring);
    }

    public String md5(String s){
        MD5 myMd5 = new MD5();
        myMd5.Init();
        myMd5.Update(s);
        return myMd5.asHex();
    }

    public HashMap<String, String> _parseDigestException(AuthorizationRequiredException e){
        // Yes, we actually have to do this ourselves. Whee!
        String auth = e.getMessage().replaceAll("^.+?:", "").trim();
        HashMap <String, String> things = new HashMap<String, String>();
        int idx = auth.indexOf('=');
        while(idx > 0){
            String name = auth.substring(0, idx).trim();
            String val;
            auth = auth.substring(idx+1).trim();
            if(auth.startsWith("\"")){
                auth = auth.substring(1);
                idx = auth.indexOf('"');
                val = auth.substring(0, idx);
                auth = auth.substring(idx+1).trim();
            }
            else {
                int spaceidx = auth.indexOf(' ');
                int commaidx = auth.indexOf(',');
                if(spaceidx < 0 && commaidx < 0)
                    idx = auth.length();
                else if(spaceidx < 0)
                    idx = commaidx;
                else if(commaidx < 0)
                    idx = spaceidx;
                else
                    idx = Math.min(commaidx, spaceidx);

                val = auth.substring(0, idx);
                if(val.endsWith(","))
                    val = val.substring(0, val.length() - 1);
                auth = auth.substring(idx+1).trim();
            }
            things.put(name, val);
            if(auth.startsWith(","))
                auth = auth.substring(1).trim();
            idx = auth.indexOf('=');
        }
        return things;
    }

    private String _username;
    private String _password;
}
