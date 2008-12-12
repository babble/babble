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
require 'xgen/rails/mongo_session'

MSTSessionThing = Struct.new(:session_id)

# Tests marshal/unmarshal, not the Babble session object.
class MongoSessionTest < RubyTest

  def setup
    $session = {}
    @s = XGen::Rails::MongoSession.new(MSTSessionThing.new('session_id'))
  end

  def teardown
    $session = nil
  end

  def test_basic
    @s[:key] = 'value'
    @s.update
    @s.restore
    assert_equal 'value', @s[:key]

    h = {'a' => 1, :b => 2}
    @s[:key2] = h
    @s.update
    @s.restore
    assert_equal 'value', @s[:key]
    assert_equal h, @s[:key2]
  end

  def test_oid_marshal
    o = ObjectId.new("4921a0da14f4abe500e678a2")
    mo = Marshal.dump(o)
    o2 = Marshal.load(mo)
    assert_equal o.to_s, o2.to_s
    assert_equal o, o2
  end

  def test_oid
    o = ObjectId.new("4921a0da14f4abe500e678a2")
    @s[:key] = o
    @s.update
    @s.restore
    assert_equal "4921a0da14f4abe500e678a2", @s[:key].to_s
    assert_equal o, @s[:key]
  end

end
