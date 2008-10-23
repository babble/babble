// JxpServletTest.java

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

package ed.appserver.jxp;

import java.io.*;

import org.testng.annotations.Test;

import ed.appserver.*;
import ed.net.httpserver.*;

public class JxpServletTest extends ed.TestCase {

    String STATIC = "SSSS";
    String SUFFIX = "";
    AppContext CONTEXT = new AppContext( "src/test/samplewww" );
    File one = new File( "src/test/samplewww/1.jpg" );
    File fooone = new File( "src/test/samplewww/foo.com/1.jpg" );

    @Test(groups = {"basic"})
    public void test0(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <img >zz"  );
        assertClose( "abc <img >zz" , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test1(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc"  );
        assertClose( "abc" , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test2(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <img src='/1.jpg' > 123"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "' > 123" , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test3(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <img " );
        p.print( " src='/1.jpg?a=b' >"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?a=b&lm=" + one.lastModified() + "' > " , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test4(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <img "  );
        p.print( " src='"  );
        p.print( "/1.jpg"  );
        p.print( "' >"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "' > " , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test4WithSuffix(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , "Z=Y" , CONTEXT   );
        p.print( "abc <img "  );
        p.print( " src='"  );
        p.print( "/1.jpg"  );
        p.print( "' >"  );
        assertClose( "abc <img src='" + STATIC + "/1.jpg?Z=Y&lm=" + one.lastModified() + "' > " , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void test5(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <img "  );
        p.print( " src='"  );
        p.print( "1.jpg"  );
        p.print( "' >"  );
        assertClose( "abc <img src='1.jpg' > " , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void testWhenISHouldnt(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        String s = "abc <img src='http://foo.com/1.jpg' > 123";
        p.print( s );
        assertClose( s , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        s = "abc <img src='//foo.com/1.jpg' > 123";
        p.print( s );
        assertClose( s , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        s = "abc <img src='/foo.com/1.jpg' > 123";
        p.print( s );
        assertClose( "abc <img src='" + STATIC + "/foo.com/1.jpg?lm=" + fooone.lastModified() + "' > 123" , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter( w , "" , SUFFIX , CONTEXT   );
        s = "abc <img src='/foo.com/1.jpg' > 123";
        p.print( s );
        assertClose( "abc <img src='/foo.com/1.jpg?lm=" + fooone.lastModified() + "' > 123" , w.getContent() );
    }


    @Test(groups = {"basic"})
    public void testLink(){

        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        String s = "a <link href='/1.jpg' >";
        p.print( s );
        assertClose( "a <link href='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "' >" , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        s = "a <link href='/1.jpg' type=\"application/rss+xml\" >";
        p.print( s );
        assertClose( s , w.getContent() );

    }

    @Test(groups = {"basic"})
    public void testInScript(){

        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        String s = "<script> s = '<script src=\"/foo\"></script>' </script>";
        p.print( s );
        assertClose( s , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void testScript(){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter( w , STATIC , SUFFIX , CONTEXT   );
        p.print( "abc <script src='/1.jpg' > 123"  );
        assertClose( "abc <script src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "' > 123" , w.getContent() );
    }

    @Test(groups = {"basic"})
    public void testInQuote () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "'<script type=\"text/javascript\" src=\"/foo\"></script>'";
        p.print(s);
        //assertClose(s, w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInScript2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "<script>abc <img src='/1.jpg' > 123</script>";
        p.print(s);
        assertClose(s, w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInAndOutOfScript () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script>abc <img src='/1.jpg'> 123</script> <img src='/1.jpg'> 123";
        p.print(s);
        assertClose("abc <script>abc <img src='/1.jpg'> 123</script> <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'> 123", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInAndOutOfScript2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script src=\"/1.jpg\"/><img src='/1.jpg'> 123";
        p.print(s);
        assertClose("abc <script src=\"" + STATIC + "/1.jpg?lm=" + one.lastModified() + "\"/><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'> 123", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInAndOutOfScript3 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "<script>a = \"<script>\";</script><img src='/1.jpg'> 123";
        p.print(s);
        //assertClose("abc <script>a = \"<script>\";</script> <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'> 123", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInQuote2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "\"abc <img src='/1.jpg' > 123\"";
        p.print(s);
        //assertClose(s, w.getContent());
    }

    public static void main( String args[] ){
        (new JxpServletTest()).runConsole();
    }
}
