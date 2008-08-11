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

package ed.appserver;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import ed.appserver.jxp.*;

/**
 * Simple tests for AppContext 
 * 
 * (NG is there to no trip up the in-house frameworks interest in *Test.java)
 *
 */
 public class AppContextTestNG {

     private final static String _ROOT = "src/test/data/test/ed/appserver/jxp";
     AppContext ac = new AppContext(_ROOT);  

     @Test
     /**
      *   Tests for non-existent things
      */
     public void testGetSourceNoFile() throws IOException { 
         
         // non-existent stuff should return 
         assertNull(ac.getSource(new File("blargh.jxp")));
         assertNull(ac.getSource(new File("blargh_dir")));
         assertNull(ac.getSource(new File("blargh_dir1/")));
     }
     
     @Test
     /**
      *   Tests various properties of getting an index file
      */
     public void testGetSourceJxp() throws IOException {
         
         File goodIndex = new File(_ROOT, "/index.jxp");
         
         JxpSource res = ac.getSource(goodIndex);
         
         assertTrue( res instanceof JxpSource.JxpFileSource);         
         assertTrue(((JxpSource.JxpFileSource) res).getFile() == goodIndex);

         File a = new File(_ROOT, "/index");

         JxpSource res1 = ac.getSource(a);

         assertTrue(res1 == res);
         assertTrue(((JxpSource.JxpFileSource) res).getFile() == ((JxpSource.JxpFileSource) res1).getFile());
     }

     @Test
     public void testGetSourceServlet() throws IOException {

         File a = new File(_ROOT, "/servlet/woogie");

         JxpSource res = ac.getSource(a);

         assertTrue(((JxpSource.JxpFileSource) res).getFile().getName().endsWith("servlet.jxp"));
     }

     @Test
     /**
      *   Tests to see if a reference to a directory w/ a index.jxp returns the index file
      */
     public void testGetSourceDirIndex() throws IOException {
         File a = new File(_ROOT, "/directory1");

         JxpSource res = ac.getSource(a);

         assertTrue(((JxpSource.JxpFileSource) res).getFile().getAbsolutePath().endsWith("directory1/index.jxp"));

         a = new File(_ROOT, "/directory1/");

         res = ac.getSource(a);

         assertTrue(((JxpSource.JxpFileSource) res).getFile().getAbsolutePath().endsWith("directory1/index.jxp"));
     }
     
     @Test
     /**
      *   Tests to see if a reference to a directory that doesn't have an index file returns null
      */
     public void testGetSourceDirectory() throws IOException {

         File a = new File(_ROOT, "/directory2");
         JxpSource res = ac.getSource(a);         
         assertNull(res);

         a = new File(_ROOT, "/directory2/");
         res = ac.getSource(a);         
         assertNull(res);
    }

     @Test
     public void testGuessNameAndEnv(){
         assertEquals( "silly" , AppContext.guessNameAndEnv( "../silly/www" )[0] );
         assertEquals( "silly" , AppContext.guessNameAndEnv( "../silly/" )[0] );
         assertEquals( "silly" , AppContext.guessNameAndEnv( "../silly" )[0] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "../silly/www" )[1] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "/silly/www" )[1] );

         assertEquals( "test" , AppContext.guessNameAndEnv( "../test/www" )[0] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "../test/www" )[1] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "./test/www" )[0] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "./test/www" )[1] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "/data/sites/test/www" )[0] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "/data/sites/test/www" )[1] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "/asdasdasdsd123qsa////asds12zd../../.a.sd/sad/sites/test/www" )[0] );
         assertEquals( "www" , AppContext.guessNameAndEnv( "/aksjd12hlasnciuashdn!@#$!asd124/sites/test/www" )[1] );

         assertEquals( "test" , AppContext.guessNameAndEnv( "../test/test" )[0] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "../test/test" )[1] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "./test/test" )[0] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "./test/test" )[1] );
         
         assertEquals( "test" , AppContext.guessNameAndEnv( "../test" )[0] );
         assertEquals( "test" , AppContext.guessNameAndEnv( "./test" )[0] );
     }

     public static void main(String[] args) throws Exception{

         AppContextTestNG f = new AppContextTestNG();

         f.testGetSourceNoFile();
         f.testGetSourceJxp();
         f.testGetSourceServlet();
         f.testGetSourceDirIndex();
         f.testGetSourceDirectory();
         f.testGuessNameAndEnv();
      }
}
