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
      require "local/#{fname}"
      assert_equal(2, $foo.count)
      require "local/#{fname}"
      assert_equal(2, $foo.count)
      require "/local/#{fname}" # since path is different,should reload
      assert_equal(3, $foo.count)
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
      load "local/#{fname}"
      assert_equal(2, $foo.count)
      load "local/#{fname}"
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

    $foo.bar = 'xyzzy'
    $new_thing = 'hello'
    out = run_js <<EOS
print("foo = " + foo);
print("foo.bar = " + foo.bar);
print("new_thing = " + new_thing);
EOS

    assert_equal("foo = Object\nfoo.bar = xyzzy\nnew_thing = hello\n", out)
  end

  def test_load_js
    load 'core/core/routes'
    assert(Object.constants.include?('Routes'), "Constant Routes should be defined")
    assert_not_nil($scope['Routes'], "Routes is not in scope")
    assert_not_nil(Routes)
    assert_equal('Class', Routes.class.name)
    assert_equal('Routes', Routes.name)
    x = Routes.new
    assert_not_nil(x, "Routes constructor returned nil")
    assert_equal('Routes', x.class.name)
  end

# FIXME

#   def test_load_js_using_jsfilelibrary
#     $core.content.forms

#     # Forms.Form is the class. Forms will be in scope, class Form will be created.
#     # See FIXME note below, however.
#     assert_not_nil($scope['Forms'], "Forms is not in scope")
#     assert(Object.constants.include?('Form'), "Constant Form should be defined")
#     assert_equal('Class', Form.class.name)
#     assert_equal('Form', Form.name)
#     assert_not_nil($Forms, "global $Forms should be defined")

#     # FIXME should use $Forms.Form or Forms::Form, not just Form. Need to get
#     # "namespace" ($Forms.Form) working
#     x = Form.new({}, "prefix")
#     assert_not_nil(x, "Form constructor returned nil")
#     assert_equal('Form', x.class.name)
#   end

end
