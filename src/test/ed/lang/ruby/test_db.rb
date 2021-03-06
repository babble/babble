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

class DBTest < RubyTest

  def setup
    super
    $db = connect('test')
    $db.rubytest.remove({})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'Aliens Ate My Buick', :song => 'The Ability to Swing'})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'Aliens Ate My Buick', :song => 'Budapest by Blimp'})
    $db.rubytest.save({:artist => 'Thomas Dolby', :album => 'The Golden Age of Wireless', :song => 'Europa and the Pirate Twins'})
    $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'Garden Of Earthly Delights', :track => 1})
    @track = $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'The Mayor Of Simpleton', :track => 2});
    $db.rubytest.save({:artist => 'XTC', :album => 'Oranges & Lemons', :song => 'King For A Day', :track => 3})
    @song_id = @track._id.to_s
    @coll = $db.rubytest
  end

  def teardown
    $db.rubytest.remove({})
    super
  end

  def test_db_exists
    assert_not_nil($db)
  end

  def test_song_id_exists
    assert_not_nil(@track._id.to_s)
  end

  def test_collection_wrapper
    assert_not_nil(@coll)
    assert @coll.respond_to?(:findOne)
    assert @coll.respond_to?(:find)
    assert @coll.respond_to?(:find_one, "automagical alias 'find_one' not created") # automagical alias
  end

  def test_find_any_one
    json = tojson(@coll.findOne())
    assert(json =~ /artist/, "JSON representation missing 'artist': #{json}")
  end

  def find_one_by_object_id
    assert_simpleton(@coll.findOne(@track._id))
  end

  def find_one_by_id_using_string
    assert_simpleton(@coll.findOne(@song_id))
  end

  def test_find_one_by_id_using_hash
    assert_simpleton(@coll.findOne({:_id => @song_id}))
  end

  def test_find_one_by_song
    assert_simpleton(@coll.findOne({:song => 'The Mayor Of Simpleton'}))
  end

  def test_find
    assert_all_rows(@coll.find.inject('') {|str, row| str + tojson(row)})
  end

  def test_find_fields
    str = @coll.find({}, {:song => 1}).inject('') {|str, row| str + tojson(row)}
    assert_all_rows(str)        # only checks song column
    %w(artist album track).each { |f| assert(str !~ /#{f}/, "saw #{f} but did not expect it") }
  end

  def test_find_fields_with_criteria
    # only return song column
    json = tojson(@coll.findOne(@song_id, {:song => 1}))
    assert_match(/Simpleton/, json, "Should have seen 'Simpleton' in json: #{json}")
    %w(artist album track).each { |f| assert_no_match(/#{f}/, json, "saw #{f} but did not expect it") }
  end

  # length() turns results into an in-memory array (as do some other cursor
  # methods) and returns the length of the array.
  def test_find_limit
    assert_equal(2, @coll.find().limit(2).length())
  end

  # count() goes to the database and returns the total number of records that
  # match the query, ignoring limit and offset. For example if 100 records
  # match but the limit is 10, count() will return 100.
  def test_find_count
    assert_equal(6, @coll.find().limit(2).count())
  end

  def assert_simpleton(x)
    assert_not_nil(x)
    assert_equal(@song_id, x._id.to_s) # x.id returns string, x._id returns ObjectId
    assert_equal("XTC", x.artist)
    assert_equal("Oranges & Lemons", x.album)
    assert_equal("The Mayor Of Simpleton", x.song)
    assert_not_nil(x.track)
    assert_equal(2, x.track.to_i)
  end

  def assert_all_rows(str)
    assert str =~ /Swing/
    assert str =~ /Blimp/
    assert str =~ /Pirate/
    assert str =~ /Garden/
    assert str =~ /Simpleton/
    assert str =~ /King/
  end

end
