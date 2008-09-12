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
