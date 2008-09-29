# Copyright (C) 2008 10gen Inc.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License, version 3, as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

require 'ruby_test'
require 'xgen/mongo'

class Track < XGen::Mongo::Base
  collection_name :rubytest
  fields :artist, :album, :song, :track
  def to_s
    # Uses both accessor methods and ivars themselves
    "artist: #{artist}, album: #{album}, song: #@song, track: #{@track ? @track.to_i : nil}"
  end
end

# Same class, but this time class.name.downcase == collection name so we don't
# have to call collection_name.
class Rubytest < XGen::Mongo::Base
  fields :artist, :album, :song, :track
  def to_s
    "artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}"
  end
end

class XGenInternalsTest < RubyTest

  def setup
    super
    run_js <<EOS
db = connect('test');
db.rubytest.remove({});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});
song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});
EOS
    @song_id = $song_id._id
  end

  def teardown
    run_js 'db.rubytest.remove({});'
    super
  end

  def test_ivars_created
    t = Track.new
    %w(_id artist album song track).each { |iv|
      assert t.instance_variable_defined?("@#{iv}")
    }
  end

  # Making sure we see the ivar, not the getter method
  def test_ivars_into_js
    $t = Track.new
    run_js <<EOS
print("t.album = " + t.album);
print("typeof(t.album) = " + typeof(t.album));
EOS
    assert_equal("t.album = null\ntypeof(t.album) = undefined", $jsout.strip)
  end

  def test_require_and_ivars_into_js
    require 'track2'
    $t = Track2.new
    run_js <<EOS
print("t.album = " + t.album);
print("typeof(t.album) = " + typeof(t.album));
EOS
    assert_equal("t.album = null\ntypeof(t.album) = undefined", $jsout.strip)
  end

# FIXME need to make require/load that uses local (JSFileLibrary) use built-in
# JRuby require if it sees it's a Ruby file.

#   def test_require_local_and_ivars_into_js
#     require '/local/track3'
#     $t = Track3.new
#     run_js <<EOS
# print("t.album = " + t.album);
# print("typeof(t.album) = " + typeof(t.album));
# EOS
#     assert_equal("t.album = null\ntypeof(t.album) = undefined", $jsout.strip)
#   end

  def test_method_generation
    x = Track.new({:artist => 1, :album => 2})

    assert x.respond_to?(:_id)
    assert x.respond_to?(:artist)
    assert x.respond_to?(:album)
    assert x.respond_to?(:song)
    assert x.respond_to?(:track)
    assert x.respond_to?(:_id=)
    assert x.respond_to?(:artist=)
    assert x.respond_to?(:album=)
    assert x.respond_to?(:song=)
    assert x.respond_to?(:track=)

    assert_equal(1, x.artist)
    assert_equal(2, x.album)
    assert_nil(x.song)
    assert_nil(x.track)
  end

  def test_find_by__id
    assert_equal("artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2", Track.find_by__id(@song_id).to_s)
  end

  def test_find_by_song
    assert_equal("artist: Thomas Dolby, album: Aliens Ate My Buick, song: Budapest by Blimp, track: ", Track.find_by_song('Budapest by Blimp').to_s)
  end

  def test_collection_name_using_class_name
    assert_equal("artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2", Rubytest.find_by__id(@song_id).to_s)
  end

  def test_update
    t = Track.find_by_track(2)
    t.track = 99
    t.save
    assert_equal("artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 99", t.to_s)
    assert_equal("artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 99", Track.find_by_track(99).to_s)
  end

  def test_find
    assert_all_songs Track.find.inject('') { |str, t| str + t.to_s }
  end

  def test_find_all
    assert_all_songs Track.find(:all).inject('') { |str, t| str + t.to_s }
  end

  def test_find_using_hash
    str = Track.find({:album => 'Aliens Ate My Buick'}).inject('') { |str, t| str + t.to_s }
    assert str =~ /song: The Ability to Swing/
    assert str =~ /song: Budapest by Blimp/
  end

  def test_find_first
    t = Track.find(:first)
    assert t.kind_of?(Track)
    str = t.to_s
    assert str =~ /artist: [^,]+,/, "did not find non-empty artist name"
  end

  def test_find_first_with_search
    assert_equal("artist: XTC, album: Oranges & Lemons, song: King For A Day, track: 3",
                 Track.find(:first, {:track => 3}).to_s)
  end

  def test_find_all_by
    str = Track.find_all_by_album('Oranges & Lemons').inject('') { |str, t| str + t.to_s }
    assert str =~ /song: Garden Of Earthly Delights/
    assert str =~ /song: The Mayor Of Simpleton/
    assert str =~ /song: King For A Day/
  end

  def test_new_no_arg
    assert_equal "artist: , album: , song: , track: ", Track.new.to_s
  end

  def test_new_by_hash
    assert_equal("artist: Level 42, album: Standing In The Light, song: Micro-Kid, track: 1",
                 Track.new(:song => 'Micro-Kid', :album => 'Standing In The Light', :artist => 'Level 42', :track => 1).to_s)
  end

  def test_new_and_save
    x = Track.new(:artist => 'Level 42', :album => 'Standing In The Light', :song => 'Micro-Kid', :track => 1)
    assert_nil(x._id)
    y = x.save
    assert_equal(x.to_s, y.to_s)
    assert_equal("artist: Level 42, album: Standing In The Light, song: Micro-Kid, track: 1", y.to_s)
    assert_not_nil(y._id)
    z = Track.find(y._id)
    assert_equal(y.to_s, z.to_s)
    assert_equal(y._id, z._id)
  end

  def find_or_create_but_already_exists
    assert_equal("artist: Thomas Dolby, album: Aliens Ate My Buick, song: The Ability to Swing, track: ",
                 Track.find_or_create_by_song({:song => 'The Ability to Swing', :artist => 'Thomas Dolby'}).to_s)
  end

  def find_or_create_new_created
    assert_equal("artist: New Artist, album: New Album, song: New Song, track: ",
                 Track.find_or_create_by_song({:song => 'New Song', :artist => 'New Artist', :album => 'New Album'}).to_s)
  end

  def test_cursor_methods
    assert_equal 2, Track.find(:all).limit(2).length
  end

  def test_return_nil_if_no_match
    assert_nil Track.find(:first, {:song => 'Does Not Compute'})
  end

  def test_return_nil_if_bogus_id
    assert_nil Track.find("bogus_id")
  end

  def test_return_nil_if_first_bogus_id
    assert_nil Track.find(:first, "bogus_id")
  end

  def test_return_nil_if_first_bogus_id_in_hash
    assert_nil Track.find(:first, {:_id => "bogus_id"})
  end

  def test_delete
    Track.find(:first, {:song => 'King For A Day'}).delete
    str = Track.find(:all).inject('') { |str, t| str + t.to_s }
    assert str =~ /song: The Ability to Swing/
    assert str =~ /song: Budapest by Blimp/
    assert str =~ /song: Europa and the Pirate Twins/
    assert str =~ /song: Garden Of Earthly Delights/
    assert str =~ /song: The Mayor Of Simpleton/
    assert str !~ /song: King For A Day/
  end

  def test_class_delete
    Track.delete(@song_id)
    assert Track.find(:all).inject('') { |str, t| str + t.to_s } !~ /song: The Mayor Of Simpleton/
  end

  def test_delete_all
    Track.delete_all({:artist => 'XTC'})
    assert Track.find(:all).inject('') { |str, t| str + t.to_s } !~ /artist: XTC/

    Track.delete_all({})        # must explicitly pass in {} if you want to delete everything
    assert_equal 0, Track.count
  end

  def test_find_by_mql_not_implemented
    Track.find_by_mql("")
    fail "should have raised a 'not implemented' exception"
  rescue => ex
    assert_equal("not implemented", ex.to_s)
  end

  def test_count
    assert_equal 6, Track.count
    assert_equal 3, Track.count({:artist => 'XTC'})
  end

  def assert_all_songs(str)
    assert str =~ /song: The Ability to Swing/
    assert str =~ /song: Budapest by Blimp/
    assert str =~ /song: Europa and the Pirate Twins/
    assert str =~ /song: Garden Of Earthly Delights/
    assert str =~ /song: The Mayor Of Simpleton/
    assert str =~ /song: King For A Day/
  end

end
