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

# A class for testing how we handle limiting access to certain resources like
# threads.
class ResourceTest < RubyTest

  def test_no_new_threads
    begin
      Thread.new {
        fail "inside thread block; should not be here"
      }
      fail "expected an exception when Thread.new called"
    rescue => ex
      assert_match /Thread\.new is not allowed\. Use XGen::BabbleThread instead\./, ex.to_s
    end
  end
end
