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

import ed.js.JSObject;
import ed.js.Shell;

@Test(groups = {"ruby.db"})
public class RubyDBTest extends SourceRunner {
    
    public static final String DB_NAME = "test";

    @BeforeTest(groups={"ruby.db", "ruby.db.findone", "ruby.db.find"})
    public void globalSetUp() {
	super.globalSetUp();
	s.put("connect", new Shell.ConnectDB(), true);
    }

    @BeforeMethod(groups={"ruby.db", "ruby.db.findone", "ruby.db.find"})
    public void setUp() {
	runJS("db = connect('" + DB_NAME + "');" +
	      "db.rubytest.remove({});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});" +
	      "db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});" +
	      "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});" +
	      "song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});" +
	      "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});");
    }

    @AfterMethod(groups={"ruby.db", "ruby.db.findone", "ruby.db.find"})
    public void tearDown() {
	runJS("db.rubytest.remove({});");
    }

    @Test
    public void testSongIdExists() {
	assertRubyEqualsJS("puts song_id._id", "print(song_id._id);");
	assertNotNull(rubyOutput);
	assertTrue(rubyOutput.length() > 0);
	assertEquals(rubyOutput, jsOutput);
	assertNotNull(s.get("song_id"));
    }

    @Test
    public void testFindAnyOne() {
	runRuby("x = db.rubytest.findOne(); puts tojson(x)");
	assertTrue(rubyOutput.contains("\"artist\""), "string \"artist\" missing: " + rubyOutput);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneById() {
	runRuby("$scope.set('x', db.rubytest.findOne(song_id))");
	// TODO use s.get("x") when Ruby can create new top-level vars without using $scope.set
	JSObject x = (JSObject)s.get("x");
	assertNotNull(x);

	assertEquals(x.get("_id").toString(), ((JSObject)s.get("song_id")).get("_id").toString());
	assertEquals(x.get("artist").toString(), "XTC");
	assertEquals(x.get("album").toString(), "Oranges & Lemons");
	assertEquals(x.get("song").toString(), "The Mayor Of Simpleton");
	assertNotNull(x.get("track"));
	assertEquals(x.get("track"), 2.0);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneByIdUsingHash() {
	runRuby("$scope.set('x', db.rubytest.findOne({:_id => song_id._id}))");
	// TODO use s.get("x") when Ruby can create new top-level vars without using $scope.set
	JSObject x = (JSObject)s.get("x");
	assertNotNull(x);

	assertEquals(x.get("_id").toString(), ((JSObject)s.get("song_id")).get("_id").toString());
	assertEquals(x.get("artist").toString(), "XTC");
	assertEquals(x.get("album").toString(), "Oranges & Lemons");
	assertEquals(x.get("song").toString(), "The Mayor Of Simpleton");
	assertNotNull(x.get("track"));
	assertEquals(x.get("track"), 2.0);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneBySong() {
	JSObject x = ((RubyJSObjectWrapper)runRuby("x = db.rubytest.findOne({:song => 'Budapest by Blimp'})")).getJSObject();
	// TODO use s.get("x") when that is fixed
	assertEquals(x.get("artist").toString(), "Thomas Dolby");
	assertEquals(x.get("album").toString(), "Aliens Ate My Buick");
	assertEquals(x.get("song").toString(), "Budapest by Blimp");
    }

    // FIXME
//     @Test(groups={"ruby.db.find"})
//     public void testForEach() {
// 	runRuby("coll = db.rubytest;" +
// 		"cursor = coll.find();" +
// 		"cursor.forEach { |row| puts tojson(row) }");
// 	System.err.println(rubyOutput); // DEBUG
//     }
}
