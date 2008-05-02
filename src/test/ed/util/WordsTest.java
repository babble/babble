// WordsTest.java

package ed.util;

import org.testng.annotations.Test;

public class WordsTest extends ed.TestCase {

    Words us = Words.getWords( "en" , "us" );
    
    @Test(groups = {"basic"})
    public void testBasic(){
        assertTrue( us.isWord( "table" ) );
        assertFalse( us.isWord( "tabl" ) );
        assertTrue( us.getRandomWord() != null );
    }
    
}
