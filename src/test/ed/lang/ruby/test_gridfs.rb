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

    @str = "Hello, GridFS!"
    GridFS.open('myfile', 'w') { |f| f.write @str }
  end

  def teardown
    run_js "db._files.remove({});"
    super
  end

  def test_read_write
    read_str = GridFS.open('myfile', 'r') { |f| f.read }
    assert_equal @str, read_str
  end

  def test_exist?
    assert GridFS.exist?('myfile')
    assert GridFS.exists?('myfile') # make sure the alias works, too
    assert !GridFS.exist?('does-not-exist')
  end

  def test_delete
    GridFS.delete('myfile')
    assert !GridFS.exist?('myfile')
  end

end
