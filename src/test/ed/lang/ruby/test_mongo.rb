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
require 'course'
require 'address'
require 'student'

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
    $db = connect('test')
    $db.rubytest_students.remove({})
    $db.rubytest_courses.remove({})
    $db.rubytest.remove({})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'Aliens Ate My Buick', :song => 'The Ability to Swing'})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'Aliens Ate My Buick', :song => 'Budapest by Blimp'})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'The Golden Age of Wireless', :song => 'Europa and the Pirate Twins'})
    $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'Garden Of Earthly Delights', :track => 1})
    song_id = $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'The Mayor Of Simpleton', :track => 2})
    $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'King For A Day', :track => 3})

    @mayor_id = song_id['_id']
    @mayor_str = "artist: XTC, album: Oranges & Lemons, song: The Mayor Of Simpleton, track: 2"
    @mayor_song = 'The Mayor Of Simpleton'

    XGen::Mongo::Base.connection = $db

    @spongebob_addr = Address.new(:street => "3 Pineapple Lane", :city => "Bikini Bottom", :state => "HI", :postal_code => "12345")
    @bender_addr = Address.new(:street => "Planet Express", :city => "New New York", :state => "NY", :postal_code => "10001")
    @course1 = Course.new(:name => 'Introductory Testing')
    @course2 = Course.new(:name => 'Advanced Phlogiston Combuston Theory')
    @score1 = Score.new(:for_course => @course1, :grade => 4.0)
    @score2 = Score.new(:for_course => @course2, :grade => 3.5)
  end

  def teardown
    $db.rubytest_students.remove({})
    $db.rubytest_courses.remove({})
    $db.rubytest.remove({})
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

  def test_find_by_id
    assert_equal(@mayor_str, Track.find_by_id(@mayor_id).to_s)
  end

  def test_find_by_song
    assert_equal("artist: Thomas Dolby, album: Aliens Ate My Buick, song: Budapest by Blimp, track: ", Track.find_by_song('Budapest by Blimp').to_s)
  end

# TODO Delete? Commented out because lack of collection_name means lack of _id
#   def test_collection_name_using_class_name
#     assert_equal(@mayor_str, Rubytest.find_by_id(@mayor_id).to_s)
#   end

  def test_update
    t = Track.find_by_track(2)
    t.track = 99
    t.save
    str = @mayor_str.sub(/2/, '99')
    assert_equal(str, t.to_s)
    assert_equal(str, Track.find_by_track(99).to_s)
  end

  def test_find_all
    assert_all_songs Track.find(:all).inject('') { |str, t| str + t.to_s }
  end

  def test_find_using_hash
    str = Track.find(:all, :conditions => {:album => 'Aliens Ate My Buick'}).inject('') { |str, t| str + t.to_s }
    assert_match(/song: The Ability to Swing/, str)
    assert_match(/song: Budapest by Blimp/, str)
  end

  def test_find_first
    t = Track.find(:first)
    assert t.kind_of?(Track)
    str = t.to_s
    assert_match(/artist: [^,]+,/, str, "did not find non-empty artist name")
  end

  def test_find_first_with_search
    t = Track.find(:first, :conditions => {:track => 3})
    assert_not_nil t, "oops: nil track returned"
    assert_equal "artist: XTC, album: Oranges & Lemons, song: King For A Day, track: 3", t.to_s
  end

  def test_find_first_returns_nil_if_not_found
    assert_nil Track.find(:first, :conditions => {:track => 666})
  end

  def test_find_all_by
    str = Track.find_all_by_album('Oranges & Lemons').inject('') { |str, t| str + t.to_s }
    assert_match(/song: Garden Of Earthly Delights/, str)
    assert_match(/song: The Mayor Of Simpleton/, str)
    assert_match(/song: King For A Day/, str)
  end

  def test_find_using_hash_with_array_and_range
    sorted_track_titles = ['Garden Of Earthly Delights', 'King For A Day', @mayor_song]

    # Array
    list = Track.find(:all, :conditions => {:track => [1,2,3]}).collect
    assert_equal 3, list.length
    assert_equal sorted_track_titles, list.collect{|t| t.song}.sort

    # Range
    list = Track.find(:all, :conditions => {:track => 1..3}).collect
    assert_equal 3, list.length
    assert_equal sorted_track_titles, list.collect{|t| t.song}.sort
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
    assert x.save, "x.save returned false; expected true"
    assert_not_nil(x._id)
    z = Track.find(x._id)
    assert_equal(x.to_s, z.to_s)
    assert_equal(x._id, z._id)
  end

  def find_or_create_but_already_exists
    assert_equal("artist: Thomas Dolby, album: Aliens Ate My Buick, song: The Ability to Swing, track: ",
                 Track.find_or_create_by_song('The Ability to Swing', :artist => 'ignored because song found').to_s)
  end

  def find_or_create_new_created
    assert_equal("artist: New Artist, album: New Album, song: New Song, track: ",
                 Track.find_or_create_by_song('New Song', :artist => 'New Artist', :album => 'New Album').to_s)
  end

  def test_cursor_methods
    assert_equal 2, Track.find(:all).limit(2).length
  end

  def test_return_nil_if_no_match
    assert_nil Track.find(:first, :conditions => {:song => 'Does Not Compute'})
  end

  def test_raise_error_if_bogus_id
    Track.find("bogus_id")
    fail 'expected "invalid ObjectId" exception'
  rescue => ex
    assert_match /invalid ObjectId/, ex.to_s
  end

  def test_raise_error_if_first_and_bogus_id_in_hash
    Track.find(:first, :conditions => {:_id => "bogus_id"})
    fail 'expected "invalid ObjectId" exception'
  rescue => ex
    assert_match /invalid ObjectId/, ex.to_s
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
    assert_match(/song: The Ability to Swing/, str)
    assert_match(/song: Budapest by Blimp/, str)
    assert_match(/song: Europa and the Pirate Twins/, str)
    assert_match(/song: Garden Of Earthly Delights/, str)
    assert_match(/song: The Mayor Of Simpleton/, str)
    assert_no_match(/song: King For A Day/, str)
  end

  def test_class_delete
    Track.delete(@mayor_id)
    assert_no_match(/song: The Mayor Of Simpleton/, Track.find(:all).inject('') { |str, t| str + t.to_s })
  end

  def test_delete_all
    Track.delete_all({:artist => 'XTC'})
    assert_no_match(/artist: XTC/, Track.find(:all).inject('') { |str, t| str + t.to_s })

    Track.delete_all(["song = ?", 'The Mayor Of Simpleton'])
    assert_no_match(/song: The Mayor Of Simpleton/, Track.find(:all).inject('') { |str, t| str + t.to_s })

    Track.delete_all("song = 'King For A Day'")
    assert_no_match(/song: King For A Day/, Track.find(:all).inject('') { |str, t| str + t.to_s })

    Track.delete_all()
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

  def test_find_using_id
    t = Track.find_by_song('King For A Day')
    tid = t._id
    # first is string id, second is ObjectId
    str = Track.find([@mayor_id, tid]).inject('') { |str, t| str + t.to_s }
    assert str.include?(@mayor_str)
    assert str.include?('King For A Day')
  end

  def test_find_one_using_id
    t = Track.find(@mayor_id)
    assert_not_nil t
    assert_match /song: The Mayor Of Simpleton/, t.to_s
  end

  def test_select_find_one
    t = Track.find(@mayor_id, :select => :album)
    assert t.album?
    assert !t.artist?
    assert !t.song?
    assert !t.track?
    assert_equal "artist: , album: Oranges & Lemons, song: , track: ", t.to_s
  end

  def test_has_one_initialize
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :address => @spongebob_addr)

    assert_not_nil s.address, "Address not set correctly in Student#initialize"
    assert_equal '3 Pineapple Lane', s.address.street
  end

  def test_has_one_save_and_find
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :address => @spongebob_addr)
    s.save

    s2 = Student.find(:first)
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
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :num_array => [100, 90, 80])
    s.save

    s2 = Student.find(:first)
    assert_equal [100, 90, 80], s2.num_array
  end

  def test_has_many_initialize
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :scores => [@score1, @score2])
    assert_not_nil s.scores
    assert_equal 2, s.scores.length
    assert_equal @score1, s.scores[0]
    assert_equal @score2, s.scores[1]
  end

  def test_has_many_initialize_one_value
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :scores => @score1)
    assert_not_nil s.scores
    assert_equal 1, s.scores.length
    assert_equal @score1, s.scores[0]
  end

  def test_has_many_save_and_find
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :scores => [@score1, @score2])
    s.save

    s2 = Student.find(:first)
    assert_equal 'Spongebob Squarepants', s2.name
    assert_equal 'spongebob@example.com', s2.email
    list = s2.scores
    assert_not_nil list
    assert_equal 2, list.length
    score = list.first
    assert_not_nil score
    assert_kind_of Score, score
    assert (score.for_course.name == @score1.for_course.name && score.grade == @score1.grade), "oops: first score is wrong: #{score}"
  end

  def test_field_query_methods
    s = Student.new(:name => 'Spongebob Squarepants', :email => 'spongebob@example.com', :scores => [@score1, @score2])
    assert s.name?
    assert s.email?
    assert s.scores

    s = Student.new(:name => 'Spongebob Squarepants')
    assert s.name?
    assert !s.email?
    assert !s.scores?

    s.email = ''
    assert !s.email?
  end

  def test_new_record
    t = Track.new
    assert t.new_record?
    t.save
    assert !t.new_record?

    t = Track.create(:artist => 'Level 42', :album => 'Standing In The Light', :song => 'Micro-Kid', :track => 1)
    assert !t.new_record?

    t = Track.find(:first)
    assert !t.new_record?

    t = Track.find_or_create_by_song('New Song', :artist => 'New Artist', :album => 'New Album')
    assert !t.new_record?

    t = Track.find_or_initialize_by_song('Newer Song', :artist => 'Newer Artist', :album => 'Newer Album')
    assert t.new_record?
  end

  def test_sql_parsing
    t = Track.find(:first, :conditions => "song = '#{@mayor_song}'")
    assert_equal @mayor_str, t.to_s
  end

  def test_sql_substitution
    s = @mayor_song
    t = Track.find(:first, :conditions => ["song = ?", s])
    assert_equal @mayor_str, t.to_s
  end

  def test_sql_named_substitution
    t = Track.find(:first, :conditions => ["song = :song", {:song => @mayor_song}])
    assert_equal @mayor_str, t.to_s
  end

  def test_sql_like
    t = Track.find(:first, :conditions => "song like '%Simp%'")
    assert_equal @mayor_str, t.to_s
  end

  def test_sql_in
    str = Track.find(:all, :conditions => "song in ('#{@mayor_song}', 'King For A Day')").inject('') { |str, t| str + t.to_s }
    assert str.include?(@mayor_song)
    assert str.include?('King For A Day')

    list = Track.find(:all, :conditions => "track in (1,2,3)").collect
    assert_equal 3, list.length
    assert_equal ['Garden Of Earthly Delights', 'King For A Day', @mayor_song], list.collect{|t| t.song}.sort
  end

  def test_in_array
    str = Track.find(:all, :conditions => ["song in (?)", [@mayor_song, 'King For A Day']]).inject('') { |str, t| str + t.to_s }
    assert str.include?(@mayor_song)
    assert str.include?('King For A Day')
  end

  def test_in_array_rails_syntax
    str = Track.find(:all, :conditions => {:song => [@mayor_song, 'King For A Day']}).inject('') { |str, t| str + t.to_s }
    assert str.include?(@mayor_song)
    assert str.include?('King For A Day')
  end

  def test_in_named_array
    str = Track.find(:all, :conditions => ["song in (:songs)", {:songs => [@mayor_song, 'King For A Day']}]).inject('') { |str, t| str + t.to_s }
    assert str.include?(@mayor_song)
    assert str.include?('King For A Day')
  end

  def test_where
    str = Track.find(:all, :where => "function() { return obj.song == '#{@mayor_song}'; }").inject('') { |str, t| str + t.to_s }
    assert_equal @mayor_str, str
  end

  def test_destroy
    Track.destroy(@mayor_id)
    begin
      Track.find(@mayor_id)
      fail "expected exception about missing ID"
    rescue => ex
      assert_match /Couldn't find Track with ID=#@mayor_id/, ex.to_s # ' <= for Emacs font lock mode
    end
  end

  # Potential bug: if this test runs at midnight, a create runs before midnight
  # and the update runs after, then this test will fail.
  def test_time_updates
    s = Student.new(:name => 'Spongebob Squarepants')
    assert s.instance_variable_defined?(:@created_at)

    assert !s.created_at?
    assert !s.created_on?
    assert !s.updated_on?

    s.save
    assert s.created_at?
    assert_kind_of Time, s.created_at
    assert s.created_on?
    assert_kind_of Time, s.created_on
    assert !s.updated_on?
    t = Time.now
    assert_equal Time.local(t.year, t.month, t.day), s.created_on

    s.save
    assert s.created_at?
    assert s.created_on?
    assert s.updated_on?
    assert_kind_of Time, s.created_at
    assert_equal s.created_on, s.updated_on
  end

  # This reproduces a bug where DBRefs weren't being created properly because
  # the XGen::Mongo::Base objects weren't storing the magic _ns, _update, and
  # other values set by the database.
  def test_db_ref
    s = Student.new(:name => 'Spongebob Squarepants', :address => @spongebob_addr)
    s.save

    @course1.save
    assert_not_nil @course1.id

    s.add_score(@course1.id, 3.5)
    s.save                      # This used to blow up

    score = s.scores.first
    assert_not_nil score
    assert_equal @course1.name, score.for_course.name

    # Now change the name of @course1 and see the student's score's course
    # name change.
    @course1.name = 'changed'
    @course1.save

    s = Student.find(:first, :conditions => "name = 'Spongebob Squarepants'")
    assert_not_nil s
    assert_equal 1, s.scores.length
    assert_equal 'changed', s.scores.first.for_course.name
  end

  def test_subobjects_have_no_ids
    @spongebob_addr.id
  rescue => ex
    assert_match /Subobjects don't have ids/, ex.to_s # ' <= for Emacs font-lock mode
  end

  def test_can_not_save_subobject
    @spongebob_addr.save
    fail "expected failed save of address"
  rescue => ex
    assert_match /Subobjects/, ex.to_s
  end

  def test_alternate_connection
    old_db = XGen::Mongo::Base.connection
    assert_equal $db, old_db
    alt_db = connect('test-alternate-connection')
    assert_not_equal old_db, alt_db
    alt_db.rubytest_students.drop()
    begin
      $db = nil
      XGen::Mongo::Base.connection = alt_db
      assert_equal alt_db, XGen::Mongo::Base.connection

      assert_equal 0, alt_db.students.find().count()
      s = Student.new(:name => 'Spongebob Squarepants', :address => @spongebob_addr)
      assert s.save, "save failed"
      assert_equal 1, alt_db.rubytest_students.find().count()
    ensure
      $db = old_db
      XGen::Mongo::Base.connection = $db
      alt_db.rubytest_students.drop()
    end
  end

  def test_method_missing
    begin
      Track.foobar
      fail "expected 'undefined method' exception"
    rescue => ex
      assert_match /undefined method \`foobar\' for Track:Class/, ex.to_s
    end
  end

  def assert_all_songs(str)
    assert_match(/song: The Ability to Swing/, str)
    assert_match(/song: Budapest by Blimp/, str)
    assert_match(/song: Europa and the Pirate Twins/, str)
    assert_match(/song: Garden Of Earthly Delights/, str)
    assert_match(/song: The Mayor Of Simpleton/, str)
    assert_match(/song: King For A Day/, str)
  end

end
