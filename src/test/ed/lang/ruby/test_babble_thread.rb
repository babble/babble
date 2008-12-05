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
require 'xgen/babble_thread'

# A class for testing how we handle limiting access to certain resources like
# threads.
class BabbleThreadTest < RubyTest

  @@app_context = Java::EdAppserver::AppContext.new(Java::JavaIo::File.new(File.dirname(__FILE__)))

  def setup
    $scope['__instance__'] = @@app_context
  end

  def teardown
    $scope['__instance__'] = nil
  end

  def test_simple
    t = XGen::BabbleThread.new(35) { |arg| arg + 7 }
    assert_equal 42, t.join
  end
end
