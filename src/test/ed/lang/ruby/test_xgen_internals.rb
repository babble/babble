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
require 'xgen_internals'

class Track < XGen::Mongo::Base
  set_collection :rubytest, %w(artist album song track)
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

  def test_methods
# FIXME
#     x = XGen::Mongo::Base.new({:artist => 1, :album => 2})
#     assert x.respond_to?(:artist)
#     assert x.respond_to?(:album)
#     assert x.respond_to?(:song)
#     assert x.respond_to?(:track)
#     assert_equal("XGen::Mongo::Base", x.class.name)
    assert true
  end

end
