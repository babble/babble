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

    @BeforeMethod(groups={"ruby", "ruby.required", "ruby.db"})
    public void setUp() {
	super.setUp();
	runRuby("require 'xgen_internals.rb';" +
		"class Track < XGen::ModelBase;" +
		"  def method_missing(sym, args); puts \"oh, no: mm #{sym}\"; end;" +
		"  set_collection :rubytest, %w(artist album song track);" +
		"  def to_s;" +
		"    \"artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}\";" +
		"  end;" +
		"end");
    }

    @AfterMethod(groups={"ruby.db", "ruby.required", "ruby.db"})
    public void tearDown() {
	super.tearDown();
    }

    public void testRequired() {
	runRuby("$x = XGen::ModelBase.new({'a' => 1, 'b' => 2}); puts $x.class.name");
    }

    public void testFindById() {
	runRuby("puts Track.find_by__id($song_id._id).to_s");
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2");
    }

    public void testFindBySong() {
	runRuby("puts Track.find_by_song('Budapest by Blimp').to_s");
	assertEquals(rubyOutput, "artist: Thomas Dolby, album: Aliens Ate My Buick, song: Budapest by Blimp, track:");
    }

    public void testSetCollectionUsingClassName() {
	runRuby("class Rubytest < XGen::ModelBase;" +
		"  set_collection %w(artist album song track);" +
		"  def to_s;" +
		"    \"artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}\";" +
		"  end;" +
		"end;" +
		"puts Rubytest.find_by__id($song_id._id).to_s");
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2");
    }

    public void testUpdate() {
	runRuby("t = Track.find_by_track(2); t.track = 99; t.save; puts Track.find_by_track(99).to_s");
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 99");
    }

    public void testFind() {
	runRuby("Track.find.each { |t| puts t.to_s }");
	assertTrue(rubyOutput.contains("song: The Ability to Swing"));
	assertTrue(rubyOutput.contains("song: Budapest by Blimp"));
	assertTrue(rubyOutput.contains("song: Europa and the Pirate Twins"));
	assertTrue(rubyOutput.contains("song: Garden Of Earthly Delights"));
	assertTrue(rubyOutput.contains("song: The Mayor Of Simpleton"));
	assertTrue(rubyOutput.contains("song: King For A Day"));
    }

    public void testFindAll() {
	runRuby("Track.find(:all).each { |t| puts t.to_s }");
	assertTrue(rubyOutput.contains("song: The Ability to Swing"));
	assertTrue(rubyOutput.contains("song: Budapest by Blimp"));
	assertTrue(rubyOutput.contains("song: Europa and the Pirate Twins"));
	assertTrue(rubyOutput.contains("song: Garden Of Earthly Delights"));
	assertTrue(rubyOutput.contains("song: The Mayor Of Simpleton"));
	assertTrue(rubyOutput.contains("song: King For A Day"));
    }

    public void testFindFirst() {
	runRuby("puts Track.find(:first).to_s");
	assertTrue(rubyOutput.contains("artist: ") && !rubyOutput.contains("artist: ,"), // non-empty artist
		   "did not find non-empty artist name");
    }

    public void testFindFirstWithSearch() {
	runRuby("puts Track.find(:first, {:track => 3}).to_s");
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: King For A Day, track: 3");
    }

    public void testFindAllBy() {
	runRuby("puts Track.find_all_by_album('Oranges & Lemons').each { |t| puts t.to_s }");
	assertTrue(rubyOutput.contains("song: Garden Of Earthly Delights"), rubyOutput);
	assertTrue(rubyOutput.contains("song: The Mayor Of Simpleton"), rubyOutput);
	assertTrue(rubyOutput.contains("song: King For A Day"), rubyOutput);
    }

    public void testNewByHash() {
	runRuby("puts Track.new(:song => 'Micro-Kid', :album => 'Standing In The Light', :artist => 'Level 42', :track => 1).to_s");
	assertEquals(rubyOutput, "artist: Level 42, album: Standing In The Light, song: Micro-Kid, track: 1");
    }

    public void testNewAndSave() {
	runRuby("$x = Track.new(:artist => 'Level 42', :album => 'Standing In The Light', :song => 'Micro-Kid', :track => 1).save; puts $x.to_s");
	assertEquals(rubyOutput, "artist: Level 42, album: Standing In The Light, song: Micro-Kid, track: 1");
	Object x = s.get("x");
	assertNotNull(x);
	assertTrue(x instanceof ed.lang.ruby.JSObjectWrapper, "expected ed.lang.ruby.JSObjectWrapper, instead saw " + x.getClass().getName());
	Object id = ((JSObjectWrapper)x).get("_id");
	assertNotNull(id);
	assertTrue(ObjectId.isValid(id.toString()), "non-valid object id: " + id.toString());
    }

    public void testFindOrCreateBy_AlreadyExists() {
	runRuby("puts Track.find_or_create_by_song({:song => 'The Ability to Swing', :artist => 'Thomas Dolby'}).to_s");
	assertEquals(rubyOutput, "artist: Thomas Dolby, album: Aliens Ate My Buick, song: The Ability to Swing, track:");
    }

    public void testFindOrCreateBy_New() {
	runRuby("puts Track.find_or_create_by_song({:song => 'New Song', :artist => 'New Artist', :album => 'New Album'}).to_s");
	assertEquals(rubyOutput, "artist: New Artist, album: New Album, song: New Song, track:");
    }
}
