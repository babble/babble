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

class ScopeTest < RubyTest

  def test_local
    assert_not_nil $local
    assert_instance_of JSFunction, $local
  end

  # TODO test for more
  # This really tests $core as defined by this test harness, but it's a start.
  def test_core
    assert_not_nil $core
    assert_instance_of JSFunction, $core
  end

end
