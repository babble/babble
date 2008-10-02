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
require 'address'
require 'student_has_one'
require 'student_has_many'
require 'student_array_field'

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

class MongoTest < RubyTest

  def setup
    super
    run_js <<EOS
db = connect('test');
db.rubytest_students.remove({});
db.rubytest.remove({});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});
song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});
EOS
    @song_id = $song_id._id

    @spongebob_addr = Address.new(:street => "3 Pineapple Lane", :city => "Bikini Bottom", :state => "HI", :postal_code => "12345")
    @bender_addr = Address.new(:street => "Planet Express", :city => "New New York", :state => "NY", :postal_code => "10001")
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
    assert x.respond_to?(:_id?)
    assert x.respond_to?(:artist?)
    assert x.respond_to?(:album?)
    assert x.respond_to?(:song?)
    assert x.respond_to?(:track?)

    assert_equal(1, x.artist)
    assert_equal(2, x.album)
    assert_nil(x.song)
    assert_nil(x.track)
  end

  def test_initialize_block
    track = Track.new { |t|
      t.artist = "Me'Shell Ndegeocello"
      t.album = "Peace Beyond Passion"
      t.song = "Bittersweet"
    }
    assert_equal "Me'Shell Ndegeocello", track.artist
    assert_equal "Peace Beyond Passion", track.album
    assert_equal "Bittersweet", track.song
    assert !track.track?
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
    str = Track.find(:conditions => {:album => 'Aliens Ate My Buick'}).inject('') { |str, t| str + t.to_s }
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
    t = Track.find(:first, :conditions => {:track => 3})
    assert_not_nil t, "oops: nill track returned"
    assert_equal "artist: XTC, album: Oranges & Lemons, song: King For A Day, track: 3", t.to_s
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
                 Track.find_or_create_by_song(:conditions => {:song => 'The Ability to Swing', :artist => 'Thomas Dolby'}).to_s)
  end

  def find_or_create_new_created
    assert_equal("artist: New Artist, album: New Album, song: New Song, track: ",
                 Track.find_or_create_by_song(:conditions => {:song => 'New Song', :artist => 'New Artist', :album => 'New Album'}).to_s)
  end

  def test_cursor_methods
    assert_equal 2, Track.find(:all).limit(2).length
  end

  def test_return_nil_if_no_match
    assert_nil Track.find(:first, :conditions => {:song => 'Does Not Compute'})
  end

  def test_return_nil_if_bogus_id
    assert_nil Track.find("bogus_id")
  end

  def test_return_nil_if_first_bogus_id
    assert_nil Track.find(:first, "bogus_id")
  end

  def test_return_nil_if_first_bogus_id_in_hash
    assert_nil Track.find(:first, :conditions => {:_id => "bogus_id"})
  end

  def test_find_options
    assert_equal 2, Track.find(:all, :limit => 2).length
  end

  def test_order_options
    tracks = Track.find(:all, :order => "song asc")
    assert_not_nil tracks
    assert_equal "Budapest by Blimp:Europa and the Pirate Twins:Garden Of Earthly Delights:King For A Day:The Ability to Swing:The Mayor Of Simpleton",
                 tracks.collect {|t| t.song }.join(':')

    # TODO this should work, but the database does not yet sort this properly
#     tracks = Track.find(:all, :order => "artist desc, song")
#     assert_not_nil tracks
#     assert_equal "Garden Of Earthly Delights:King For A Day:The Mayor Of Simpleton:Budapest by Blimp:Europa and the Pirate Twins:The Ability to Swing",
#                  tracks.collect {|t| t.song }.join(':')
  end

  def test_delete
    Track.find(:first, :conditions => {:song => 'King For A Day'}).delete
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
    assert_equal 3, Track.count(:conditions => {:artist => 'XTC'})
  end

  def test_select
    str = Track.find(:all, :select => :album).inject('') { |str, t| str + t.to_s }
    assert str.include?("artist: , album: Oranges & Lemons, song: , track:")
  end

  def test_find_one_using_id
    t = Track.findOne(@song_id)
    assert_equal "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2", t.to_s
  end

  def test_select_find_one
    t = Track.findOne(@song_id, :select => :album)
    assert t.album?
    assert !t.artist?
    assert !t.song?
    assert !t.track?
    assert_equal "artist: , album: Oranges & Lemons, song: , track: ", t.to_s
  end

  def test_has_one_initialize
    s = StudentHasOne.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :address => @spongebob_addr)

    assert_not_nil s.address, "Address not set correctly in StudentHasOne#initialize"
    assert_equal '3 Pineapple Lane', s.address.street
  end

  def test_has_one_save_and_find
    s = StudentHasOne.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :address => @spongebob_addr)
    s.save

    s2 = StudentHasOne.find(:first)
    assert_equal 'Spongebob Squarepants', s2.name
    assert_equal 'spongebob@example.com', s2.email
    a2 = s2.address
    assert_not_nil a2
    assert_kind_of Address, a2
    assert_equal @spongebob_addr.street, a2.street
    assert_equal @spongebob_addr.city, a2.city
    assert_equal @spongebob_addr.state, a2.state
    assert_equal @spongebob_addr.postal_code, a2.postal_code
  end

  def test_student_array_field
    s = StudentArrayField.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :math_scores => [100, 90, 80])
    s.save

    s2 = StudentArrayField.find(:first)
    assert_equal [100, 90, 80], s2.math_scores
  end

  def test_has_many_initialize
    addresses = [@spongebob_addr, @bender_addr]
    s = StudentHasMany.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :addresses => addresses)
    assert_not_nil s.addresses
    assert_equal 2, s.addresses.length
  end

  def test_has_many_initialize_one_value
    addresses = @spongebob_addr
    s = StudentHasMany.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :addresses => addresses)
    assert_not_nil s.addresses
    assert_equal 1, s.addresses.length
    assert_equal @spongebob_addr.street, s.addresses.first.street
  end

  def test_has_many_save_and_find
    addresses = [@spongebob_addr, @bender_addr]
    s = StudentHasMany.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :addresses => addresses)
    s.save

    s2 = StudentHasMany.find(:first)
    assert_equal 'Spongebob Squarepants', s2.name
    assert_equal 'spongebob@example.com', s2.email
    list = s2.addresses
    assert_not_nil list
    assert_equal 2, list.length
    a = list.first
    assert_not_nil a
    assert_kind_of Address, a
    assert (a.street == @spongebob_addr.street || a.street == @bender_addr.street), "oops: first address is unknown: #{a}"
  end

  def test_field_query_methods
    addresses = [@spongebob_addr, @bender_addr]
    s = StudentHasMany.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :addresses => addresses)
    assert s.name?
    assert s.email?
    assert s.addresses

    s = StudentHasMany.new(:name => 'Spongebob Squarepants')
    assert s.name?
    assert !s.email?
    assert !s.addresses?

    s.email = ''
    assert !s.email?
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
