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

package ed.lang.ruby;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.js.Shell;

@Test(groups = {"ruby.db"})
public class RubyDBTest extends SourceRunner {
    
    public static final String DB_NAME = "test";

    @BeforeTest(groups={"ruby.db"})
    public void globalSetUp() {
	super.globalSetUp();
	s.put("connect", new Shell.ConnectDB(), true);
    }

    @BeforeMethod(groups={"ruby.db"})
    public void setUp() {
	runJS("db = connect('" + DB_NAME + "');" +
	      "db.rubytest.remove({});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});" +
	      "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});" +
	      "song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});" +
	      "song_id = song_id._id;" +
	      "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});");
    }

    @AfterMethod(groups={"ruby.db"})
    public void tearDown() {
	runJS("db.rubytest.remove({});");
    }

    @Test
    public void testSongIdExists() {
	runJS("print(song_id);");
	runRuby("puts song_id");
	assertNotNull(rubyOutput);
	assertTrue(rubyOutput.length() > 0);
	assertEquals(rubyOutput, jsOutput);
    }

    @Test
    public void testFindAnyOne() {
	runRuby("x = db.rubytest.findOne(); puts tojson(x)");
	assertTrue(rubyOutput.contains("\"artist\""), "string \"artist\" missing: " + rubyOutput);
    }

    @Test
    public void testFindOneById() {
	runRuby("x = db.rubytest.findOne(song_id); puts tojson(x)");
	System.err.println(rubyOutput);
// 	assertRubyEquals("puts x.artist", "XTC");
// 	assertRubyEquals("puts x.album", "Oranges & Lemons");
// 	assertRubyEquals("puts x.song", "The Mayor Of Simpleton");
// 	assertRubyEquals("puts x.track", "2");
    }

//     @Test
//     public void testFindOneBySong() {
// 	runRuby("x = db.rubytest.findOne({:song => 'Budapest by Blimp'})");
// 	assertRubyEquals("puts x.artist", "Thomas Dolby");
// 	assertRubyEquals("puts x.album", "Aliens Ate My Buick");
// 	assertRubyEquals("puts x.song", "Budapest by Blimp");
//     }
}
