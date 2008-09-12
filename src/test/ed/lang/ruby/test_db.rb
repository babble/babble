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
    @coll = $db.rubytest
  end

  def teardown
    run_js 'db.rubytest.remove({});'
    super
  end

  def test_db_exists
    assert_not_nil($db)
  end

  def test_song_id_exists
    run_js 'x = song_id._id.toString()'
    assert_equal($song_id._id, $x)
  end

  def test_collection_wrapper
    assert_not_nil(@coll)
    assert @coll.respond_to?(:findOne)
    assert @coll.respond_to?(:find)
  end

  def test_find_any_one
    json = tojson($db.rubytest.findOne())
    assert(json =~ /artist/, "JSON representation missing 'artist': #{json}")
  end

  def find_one_by_object_id
    assert_simpleton(@coll.findOne($song_id))
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

  def assert_simpleton(x)
    assert_not_nil(x)
    assert_equal(@song_id, x._id)
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
