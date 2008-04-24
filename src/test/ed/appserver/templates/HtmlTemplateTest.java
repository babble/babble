// HtmlTemplateTest.java

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.Test;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class HtmlTemplateTest extends ConvertTestBase {

    public HtmlTemplateTest(){
        super( ".html" );
    }

    TemplateConverter getConverter(){
        return new JxpConverter( true );
    }


    Object[] getArgs(){
        JSObjectBase o = new JSObjectBase();
        o.set( "foo" , "17" );
        return new Object[]{ o };
    }

    public static void main( String args[] ){
        HtmlTemplateTest t = new HtmlTemplateTest();
        t.runConsole();
    }
    
}
