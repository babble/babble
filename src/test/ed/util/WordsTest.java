// WordsTest.java

package ed.util;

public class WordsTest extends ed.TestCase {

    Words us = Words.getWords( "en" , "us" );
    
    public void testBasic(){
        assertTrue( us.isWord( "table" ) );
        assertFalse( us.isWord( "tabl" ) );
        assertTrue( us.getRandomWord() != null );
    }
    
}
