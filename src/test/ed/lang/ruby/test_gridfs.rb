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
require 'xgen/gridfs'

class GridFSTest < RubyTest

  def setup
    super
    run_js "db = connect('test'); db._files.remove({});"
  end

  def teardown
    run_js "db._files.remove({});"
    super
  end

  def test_read_write
    str = "Hello, GridFS!"
    GridFS.open('myfile', 'w') { |f| f.write str }
    new_str = GridFS.open('myfile', 'r') { |f| f.read }
    assert_equal str, new_str
  end

end
