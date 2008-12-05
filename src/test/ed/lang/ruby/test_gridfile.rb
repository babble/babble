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
require 'xgen/gridfile'

class GridFileTest < RubyTest

  def setup
    super
    $db = connect('test')
    $db._files.remove({})
    $db._chunks.remove({})

    @str = "Hello, GridFS!"
    GridFile.open('myfile', 'w') { |f| f.write @str }
  end

  def teardown
    $db._files.remove({})
    $db._chunks.remove({})
    super
  end

  def test_read_write
    read_str = GridFile.open('myfile', 'r') { |f| f.read }
    assert_equal @str, read_str
  end

  def test_exist?
    assert GridFile.exist?('myfile')
    assert GridFile.exists?('myfile') # make sure the alias works, too
    assert !GridFile.exist?('does-not-exist')
  end

  def test_delete
    GridFile.delete('myfile')
    assert !GridFile.exist?('myfile')

    assert_equal 0, $db['_chunks'].find().count(), "chunks were not deleted"
  end

  def test_attributes
    f = GridFile.open('myfile', 'r')
    assert_equal 'myfile', f['filename']

    f = GridFile.open('another', 'w')
    f.puts 'content of another file'
    f['foo'] = 'bar'
    f.close

    f = GridFile.open('another', 'r')
    assert_equal 'bar', f['foo']
  end

  def test_method_missing
    f = GridFile.open('myfile', 'r')
    assert_not_nil f.filename
    assert_equal 'myfile', f.filename
    assert_not_nil f.chunkSize
    assert_not_nil f.uploadDate
  end

  def test_raise_if_no_db
    old_db = $db
    $db = nil
    begin
      GridFile.connection = nil
      f = GridFile.open('myfile', 'r')
      f.close
      fail 'should have raised an error'
    rescue => ex
      assert_equal 'connection not defined', ex.to_s
    ensure
      $db = old_db
    end
  end

  def test_each
    GridFile.open('multi-line', 'w') { |f|
      f.puts 'line 1'
      f.puts 'line 2'
      f.puts 'line 3'
    }
    GridFile.open('multi-line', 'r') { |f|
      i = 1
      f.each { |line| assert_equal "line #{i}\n", line; i += 1 }
      assert_equal 4, i
    }
  end

  def test_alternate_connection
    $db._files.remove({})
    $db._chunks.remove({})
    assert_equal 0, $db._files.find().count()
    old_db = $db
    alt_db = connect('test-alternate-connection')
    assert_not_equal old_db, alt_db
    begin
      $db = nil
      GridFile.connection = alt_db
      assert_equal alt_db, GridFile.connection

      GridFile.open('myfile', 'w') { |f| f.write @str }
      read_str = GridFile.open('myfile', 'r') { |f| f.read }
      assert_equal @str, read_str

      assert_equal 0, old_db._files.find().count()
      assert_equal 1, alt_db._files.find().count()
    ensure
      $db = old_db
      GridFile.connection = $db
      alt_db._chunks.drop()
      alt_db._files.drop()
    end
  end

end
