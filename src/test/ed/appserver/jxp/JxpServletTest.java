// JxpServletTest.java

package ed.appserver.jxp;

import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpServletTest extends ed.TestCase {

    String STATIC = "SSSS";
    AppContext CONTEXT = new AppContext( "src/test/samplewww" );

    public void test0(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc <img >zz"  );
        assertClose( "abc <img >zz" , w.getContent() );
    }

    public void test1(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc"  );
        assertClose( "abc" , w.getContent() );
    }

    public void test2(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc <img src='/1.jpg' > 123"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?lm=1195794916000' > 123" , w.getContent() );
    }

    public void test3(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc <img " );
        p.print( " src='/1.jpg?a=b' >"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?a=b&lm=1195794916000' > " , w.getContent() );
    }

    public void test4(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc <img "  );
        p.print( " src='"  );
        p.print( "/1.jpg"  );
        p.print( "' >"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?lm=1195794916000' > " , w.getContent() );
    }

    public void test5(){
        JxpWriter w = new JxpWriter.Basic();
        JxpServlet.MyWriter p = new JxpServlet.MyWriter( w , STATIC , CONTEXT , null  );
        p.print( "abc <img "  );
        p.print( " src='"  );
        p.print( "1.jpg"  );
        p.print( "' >"  );
        assertClose( "abc <img src='1.jpg' > " , w.getContent() );
    }

    public static void main( String args[] ){
        (new JxpServletTest()).runConsole();
    }
}
