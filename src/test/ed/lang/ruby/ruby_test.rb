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

require 'test/unit'

class RubyTest < Test::Unit::TestCase

  def setup
    @existing_keys = $scope.keys
  end

  def teardown
    others = $scope.keys - @existing_keys
    others.each { |k| $scope.remove(k) }
  end

  def run_js(js)
    fname = '__temp_run_js' + rand(0xffffffff).to_s
    path = File.join(File.dirname(__FILE__), fname + '.js')
    $scope['jsout'] = ''
    begin
      File.open(path, 'w') { |f| f.puts js }
      load fname
    rescue => ex
      fail(ex.to_s)
    ensure
     File.delete(path) if File.exist?(path)
    end
    return $scope['jsout']
  end

  # A dummy test is necessary because the TestUnit code will look for at least
  # one test in this class, even though we just want it to be the superclass
  # of the other tests.
  def test_dummy
    assert true
  end
end
