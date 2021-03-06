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

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
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
        assertClose( "abc <img src='" + STATIC + "/1.jpg?a=b&amp;lm=" + one.lastModified() + "' > " , w.getContent() );
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
        assertClose( "abc <img src='" + STATIC + "/1.jpg?Z=Y&amp;lm=" + one.lastModified() + "' > " , w.getContent() );
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
        String s = "<script>'<script type=\"text/javascript\" src=\"/foo\"></script>'";
        p.print(s);
        assertClose(s, w.getContent());
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
        String s = "abc <script>a = \"<script>\"; img = \"<img src='/1.jpg'/>\"</script><img src='/1.jpg'> 123";
        p.print(s);
        assertClose("abc <script>a = \"<script>\"; img = \"<img src='/1.jpg'/>\"</script> <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'> 123", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testInAndOutOfScript4 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script>a = \"</script>\"; img = \"<img src='/1.jpg'/>\"</script><img src='/1.jpg'> 123";
        p.print(s);
        assertClose("abc <script>a = \"</script>\"; img = \"<img src='/1.jpg'/>\"</script> <img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'> 123", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuotesInScriptTag () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("abc <script> \"'\\\"''\" </ script ><img src='/1.jpg'>");
        assertClose("abc <script> \"'\\\"''\" </ script ><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuotesInScriptTag2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script> \"'\\\"''\"' </ script ><img src='/1.jpg'>";
        p.print(s);
        assertClose(s, w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuotesInScriptTag3 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script> \"'\\\"''\"\" </ script ><img src='/1.jpg'>";
        p.print(s);
        assertClose(s, w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuotesInScriptTag4 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        String s = "abc <script> \"</script><img src='/1.jpg'>'</script><img src='/1.jpg'>\\\"''\"'<img src=\'/1.jpg\'>' </ script ><img src='/1.jpg'>";
        p.print(s);
        assertClose("abc <script> \"</script><img src='/1.jpg'>'</script><img src='/1.jpg'>\\\"''\"'<img src=\'/1.jpg\'>' </ script ><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void _testPerformance() {
        double ratio = 0;
        for (int h = 0; h < 20; h += 1) {
            JxpWriter w = new JxpWriter.Basic();
            ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);

            long before = System.currentTimeMillis();
            for (int i = 0; i < 10000; i += 1) {
                p.print("abc ");
                p.print(" blah blah <br /> <p> hello </p>  ");
                p.print(" <script src='/1.jpg'>heres some stuff</script> <script />");
                p.print(" <script>\"''\\\"\"' </script>'");
                p.print("</script>");
                p.print("123 anoe <img src='/1.jpg'> blah");
                p.print("do more stuff ");
                p.print("<script>");
                p.print("'within a quote \' </script");
                p.print("leave \" the quote ' </script> blah");
            }
            long after = System.currentTimeMillis();
            long diffOurs = after - before;

            StringBuffer b = new StringBuffer();
            before = System.currentTimeMillis();
            for (int i = 0; i < 10000; i += 1) {
                b.append("abc ");
                b.append(" blah blah <br /> <p> hello </p>  ");
                b.append(" <script src='/1.jpg'>heres some stuff</script> <script />");
                b.append(" <script>\"''\\\"\"' </script>'");
                b.append("</script>");
                b.append("123 anoe <img src='/1.jpg'> blah");
                b.append("do more stuff ");
                b.append("<script>");
                b.append("'within a quote \' </script");
                b.append("leave \" the quote ' </script> blah");
            }
            after = System.currentTimeMillis();
            long diffTheirs = after - before;

            ratio += (double)diffOurs / diffTheirs;
        }
        ratio /= 20;

        // seems to usually be ~26 on my machine
        assert(ratio < 40.0);
    }

    @Test(groups = {"basic"})
    public void testAdminHeader () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("<script>");
        p.print("\"\"");
        p.print("</script ><img src='/1.jpg'>");
        assertClose("<script>\"\"</script><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testScriptInComment () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("<script>");
        p.print("abc 123 // </script>");
        p.print("<img src='/1.jpg'>");
        p.print("</script>");
        p.print("<img src='/1.jpg'>");
        assertClose("<script>abc 123 // </script><img src='/1.jpg'></script><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testScriptInComment2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("<script>");
        p.print("abc 123 /* </script>*/");
        p.print("<img src='/1.jpg'>");
        p.print("</script>");
        p.print("<img src='/1.jpg'>");
        assertClose("<script>abc 123 /* </script>*/<img src='/1.jpg'></script><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuoteInComment () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("<script>");
        p.print("abc 123 // ' ");
        p.print("</script>");
        p.print("<img src='/1.jpg'>");
        assertClose("<script>abc 123 // ' </script><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testQuoteInComment2 () {
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print("<script>");
        p.print("abc 123 /* ' */");
        p.print("</script>");
        p.print("<img src='/1.jpg'>");
        assertClose("<script>abc 123 /* ' */</script><img src='" + STATIC + "/1.jpg?lm=" + one.lastModified() + "'>", w.getContent());
    }

    @Test(groups = {"basic"})
    public void testBrokenUp(){

        final String correct = "<img src=\"SSSS/1.jpg?lm=" + one.lastModified() + "\" >";

        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print( "<img src=\"/1.jpg\" >" );
        assertClose( correct , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print( "<img src=\"" );
        p.print( "/1.jpg" );
        p.print( "\" >" );
        assertClose( correct , w.getContent() );

        w = new JxpWriter.Basic();
        p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);
        p.print( "<img src=" );
        p.print( "\"/1.jpg\"" );
        p.print( " >" );
        assertClose( correct , w.getContent() );
    }


    @Test(groups = {"basic"})
    public void testTagHandler1(){
        _countTags( 1 , "a" , "blah <a href='asd'></a>" );
        _countTags( 2 , "a" , "blah <a href='asd'></a><a>" );
        _countTags( 1 , "A" , "blah <a href='asd'></a>" );
        _countTags( 1 , "a" , "blah <A href='asd'></a>" );
        _countTags( 1 , "A" , "blah <A href='asd'></a>" );
    }

    @Test(groups = {"basic"})
    public void testTagHandler2(){
        _countTags( 1 , "a" , "<a href=\"/_blah\"></a>" );
        _countTags( 1 , "a" , "<a href=\"/" , "_blah" , "\"></a>" );
        _countTags( 1 , "a" , "<a href=\"/" , "_blah" , "\"></a>" );
        _countTags( 1 , "a" , "goofy" , "<a href=\"/" , "_blah" , "\"></a>" );
        _countTags( 1 , "a" , "goofy" , "<" , "a href=\"/" , "_blah" , "\"></a>" );
        _countTags( 1 , "a" , "goofy<script>\n//\"\n</script>\n<a href=\"/_blah\"></a>" );
        _countTags( 1 , "a" , "goofy" , "<script>\n//\"\n</script>\n" , "<" , "a href=\"/" , "_blah" , "\"></a>" );
    }

    private void _countTags( int number , String tag , String ... segments ){
        JxpWriter w = new JxpWriter.Basic();
        ServletWriter p = new ServletWriter(w, STATIC, SUFFIX, CONTEXT);

        final int count[] = new int[]{ 0 };

        p.addTagHandler( tag , new JSFunctionCalls1(){
                public Object call( Scope s , Object tag , Object[] extra ){
                    count[0]++;
                    return null;
                }
            }
            );

        for ( int i=0; i<segments.length; i++ )
            p.print( segments[i] );

        assertEquals( number , count[0] );
    }

    public static void main( String args[] ){
        (new JxpServletTest()).runConsole();
    }
}
