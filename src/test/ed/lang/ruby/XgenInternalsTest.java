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
		"    \"artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{self.track}\";" +
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
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2.0");
    }

    public void testFindBySong() {
	runRuby("puts Track.find_by_song('Budapest by Blimp').to_s");
	assertEquals(rubyOutput, "artist: Thomas Dolby, album: Aliens Ate My Buick, song: Budapest by Blimp, track:");
    }

    public void testSetCollectionUsingClassName() {
	runRuby("class Rubytest < XGen::ModelBase;" +
		"  set_collection %w(artist album song track);" +
		"  def to_s;" +
		"    \"artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{self.track}\";" +
		"  end;" +
		"end;" +
		"puts Rubytest.find_by__id($song_id._id).to_s");
	assertEquals(rubyOutput, "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2.0");
    }
}
