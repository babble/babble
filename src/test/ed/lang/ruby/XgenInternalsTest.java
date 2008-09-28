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

import ed.db.ObjectId;

@Test(groups = {"ruby", "ruby.required", "ruby.db"})
public class XgenInternalsTest extends RubyDBTest {

    static final String JS_RECORD_CREATION_CODE = "db.rubytest.remove({});" +
        "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});" +
        "db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});" +
        "db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});" +
        "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});" +
        "song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});" +
        "db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});";
    static final String JS_RECORD_DELETION_CODE = "db.rubytest.remove({})";

    @BeforeMethod(groups={"ruby", "ruby.required", "ruby.db"})
    public void setUp() {
        super.setUp();
        runJS(JS_RECORD_CREATION_CODE);
        runRuby("require 'xgen/mongo';" +
                "class Track < XGen::Mongo::Base;" +
                "  set_collection :rubytest, %w(artist album song track);" +
                "  def to_s;" +
                "    \"artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}\";" +
                "  end;" +
                "end");
    }

    @AfterMethod(groups={"ruby.db", "ruby.required", "ruby.db"})
    public void tearDown() {
        runJS(JS_RECORD_DELETION_CODE);
    }

    public void testRequired() {
        runRuby("$x = XGen::Mongo::Base.new({'a' => 1, 'b' => 2}); puts $x.class.name");
    }

    public void testNewAndSave() {
        runRuby("$x = Track.new(:artist => 'Level 42', :album => 'Standing In The Light', :song => 'Micro-Kid', :track => 1).save; puts $x.to_s");
        assertEquals(rubyOutput, "artist: Level 42, album: Standing In The Light, song: Micro-Kid, track: 1");
        Object x = s.get("x");
        assertNotNull(x);
        assertTrue(x instanceof ed.lang.ruby.JSObjectWrapper, "expected ed.lang.ruby.JSObjectWrapper, saw " + x.getClass().getName());
        Object id = ((JSObjectWrapper)x).get("_id");
        assertNotNull(id);
        assertTrue(ObjectId.isValid(id.toString()), "non-valid object id: " + id.toString());
    }
}
