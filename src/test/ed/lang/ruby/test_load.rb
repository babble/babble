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

class LoadTest < RubyTest

  def test_load
    assert_nil($foo)
    run_js "foo = {}; foo.bar = 'bletch';"
    assert_not_nil($foo)
    assert_equal($foo.bar, 'bletch')
  end

  def test_require_only_loads_once
    assert_nil($foo)
    run_js "foo = {}; foo.count = 1;"
    assert_equal($foo.count, 1)

    fname = 'require1'
    path = File.join(File.dirname(__FILE__), fname + '.js')
    File.open(path, 'w') { |f| f.puts 'foo.count += 1;' }

    begin
      require fname
      assert_equal(2, $foo.count)
      require fname
      assert_equal(2, $foo.count)
    ensure
      File.delete(path) if File.exist?(path)
    end
  end

  def test_load_loads_multiple_times
    assert_nil($foo)
    run_js "foo = {}; foo.count = 1;"
    assert_equal($foo.count, 1)

    fname = 'load1'
    path = File.join(File.dirname(__FILE__), fname + '.js')
    File.open(path, 'w') { |f| f.puts 'foo.count += 1;' }
    begin
      load fname
      assert_equal(2, $foo.count)
      load fname
      assert_equal(3, $foo.count)
    rescue => ex
      fail(ex.to_s)
    ensure
      File.delete(path) if File.exist?(path)
    end
  end

  def test_back_into_js
    run_js "foo = {}; foo.bar = 'bletch';"
    out = run_js <<EOS
print("foo = " + foo);
print("foo.bar = " + foo.bar);
print("new_thing = " + new_thing);
EOS
    assert_equal("foo = Object\nfoo.bar = bletch\nnew_thing = null\n", out)

# FIXME this should work
#     $foo.bar = 'xyzzy'
#     $new_thing = 'hello'
#     run_js <<EOS
# print("foo = " + foo);
# print("foo.bar = " + foo.bar);
# print("new_thing = " + new_thing);
# EOS
#     assert_equal("foo = Object\nfoo.bar = xyzzy\nnew_thing = hello\n", out)
  end

end
