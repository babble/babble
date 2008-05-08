// JxpConvertTest.java

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.*;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class JxpConvertTest extends ConvertTestBase {

    public JxpConvertTest( String args[] ){
        super( ".jxp" , args );
    }


    @Factory 
    public Object[] getAllTests(){
        return _all.toArray();
    }

    TemplateConverter getConverter(){
        return new JxpConverter();
    }

    public static void main( String args[] ){
        JxpConvertTest t = new JxpConvertTest( args );
        t.runConsole();
    }
    
}
