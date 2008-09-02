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

import org.jruby.*;
import org.jruby.runtime.builtin.IRubyObject;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import ed.db.DBCollection;
import ed.js.JSObject;
import ed.js.Shell;
import static ed.lang.ruby.RubyObjectWrapper.toJS;

@Test(groups = {"ruby.db"})
public class RubyDBTest extends SourceRunner {
    
    static final String DB_NAME = "test";
    static final String JS_RECORD_CREATION_CODE = "db = connect('" + DB_NAME + "');" +
	"db.rubytest.remove({});" +
	"db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});" +
	"db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});" +
	"db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});" +
	"db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});" +
	"song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});" +
	"db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});";
    static final String JS_RECORD_DELETION_CODE = "db.rubytest.remove({})";

    @BeforeMethod(groups={"ruby.db", "ruby.db.findone", "ruby.db.find"})
    public void setUp() {
	super.setUp();
	s.put("connect", new Shell.ConnectDB(), true);
	runJS(JS_RECORD_CREATION_CODE);
    }

    @AfterMethod(groups={"ruby.db", "ruby.db.findone", "ruby.db.find"})
    public void tearDown() {
	runJS(JS_RECORD_DELETION_CODE);
    }

    public void testSongIdExists() {
	assertRubyEqualsJS("puts $song_id._id", "print(song_id._id);");
	assertNotNull(rubyOutput);
	assertTrue(rubyOutput.length() > 0);
	assertEquals(rubyOutput, jsOutput);
	assertNotNull(s.get("song_id"));
    }

    public void testCollectionWrapper() {
	Object ro = runRuby("$db.rubytest");
	assertNotNull(ro);
	assertTrue(ro instanceof RubyJSObjectWrapper, "ro is not a RubyJSObjectWrapper; it's " + ro.getClass().getName());
	assertTrue(((RubyObject)ro).respond_to_p(RubySymbol.newSymbol(r, ":findOne")).isTrue());
	assertTrue(((RubyObject)ro).respond_to_p(RubySymbol.newSymbol(r, ":find")).isTrue());

	Object o = toJS(s, (IRubyObject)ro);
	assertTrue(o instanceof DBCollection, "o is not a DBCollection; it's " + o.getClass().getName());
	assertSame(toJS(s, (IRubyObject)ro), ((RubyJSObjectWrapper)ro).getJSObject());
    }
	
    @Test(groups={"ruby.db.findone"})
    public void testFindAnyOne() {
	runRuby("x = $db.rubytest.findOne(); puts tojson(x)");
	assertTrue(rubyOutput.contains("\"artist\""), "string \"artist\" missing: " + rubyOutput);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneById() {
	runRuby("$x = $db.rubytest.findOne($song_id)");
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
    public void testFindOneByIdUsingString() {
	String idString = ((JSObject)s.get("song_id")).get("_id").toString();
	runRuby("$x =$db.rubytest.findOne('" + idString + "')");
	JSObject x = (JSObject)s.get("x");
	assertEquals(x.get("_id").toString(), idString);
	assertEquals(x.get("artist").toString(), "XTC");
	assertEquals(x.get("album").toString(), "Oranges & Lemons");
	assertEquals(x.get("song").toString(), "The Mayor Of Simpleton");
	assertNotNull(x.get("track"));
	assertEquals(x.get("track"), 2.0);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneByIdUsingHash() {
	runRuby("$x = $db.rubytest.findOne({:_id => $song_id._id})");
	JSObject x = (JSObject)s.get("x");
	assertEquals(x.get("_id").toString(), ((JSObject)s.get("song_id")).get("_id").toString());
	assertEquals(x.get("artist").toString(), "XTC");
	assertEquals(x.get("album").toString(), "Oranges & Lemons");
	assertEquals(x.get("song").toString(), "The Mayor Of Simpleton");
	assertNotNull(x.get("track"));
	assertEquals(x.get("track"), 2.0);
    }

    @Test(groups={"ruby.db.findone"})
    public void testFindOneBySong() {
	runRuby("$x = $db.rubytest.findOne({:song => 'Budapest by Blimp'})");
	JSObject x = (JSObject)s.get("x");
	assertEquals(x.get("artist").toString(), "Thomas Dolby");
	assertEquals(x.get("album").toString(), "Aliens Ate My Buick");
	assertEquals(x.get("song").toString(), "Budapest by Blimp");
    }

    @Test(groups={"ruby.db.find"})
    public void testCursorWrapper() {
	runRuby("puts \"#{$db.rubytest.respond_to? :find}\"");
	assertEquals(rubyOutput, "true");
	Object o = runRuby("$db.rubytest.find()");
	assertTrue(o instanceof RubyDBCursorWrapper, "Oops: o should be RubyDBCursorWrapper; it is " + o.getClass().getName());
    }

    @Test(groups={"ruby.db.find"})
    public void testForEach() {
	runRuby("$db.rubytest.find().forEach { |row| puts tojson(row) }");
	lookForAllRows();
    }

    @Test(groups={"ruby.db.find"})
    public void testEach() {
	runRuby("$db.rubytest.find().each { |row| puts tojson(row) }");
	lookForAllRows();
    }

    protected void lookForAllRows() {
	// Can't just look at the whole string because (A) we don't know the
	// order of the results and (B) the output gets wrapped.
	assertTrue(rubyOutput.contains("Swing"));
	assertTrue(rubyOutput.contains("Blimp"));
	assertTrue(rubyOutput.contains("Pirate"));
	assertTrue(rubyOutput.contains("Garden"));
	assertTrue(rubyOutput.contains("Simpleton"));
	assertTrue(rubyOutput.contains("King"));
    }
}
