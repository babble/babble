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

class ObjectIdTest < RubyTest

  def setup
    @good_id = '4910688f14f4ab850008ace1'
    @bad_id = 'bad_id'
  end

  def test_good_id
    oid = ObjectId.new(@good_id)
    assert_equal @good_id, oid.to_s
  end

  def test_bad_id
    ObjectId.new(@bad_id)
    fail "expected 'bad object id' exception"
  rescue => ex
    assert_equal "bad object id: #{@bad_id}", ex.to_s
  end

end
