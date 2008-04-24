// Djang10ConverterTest.java

package ed.appserver.templates;

import java.io.*;

import org.testng.annotations.*;

import ed.*;
import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import ed.io.*;

public class Djang10ConverterTest extends ConvertTestBase {

    public Djang10ConverterTest(){
        super( ".djang10" );
    }


    @Factory 
    public Object[] getAllTests(){
        return _all.toArray();
    }

    Djang10Converter getConverter(){
        return new Djang10Converter();
    }

    public static void main( String args[] ){
        Djang10ConverterTest t = new Djang10ConverterTest();
        t.runConsole();
    }
    
}
