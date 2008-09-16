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

class JSFunctionTest < RubyTest

  def setup
    super
    run_js <<EOS
JSFunctionTestJSClass = function(foo) {
  this.foo = foo;
}
JSFunctionTestJSClass.prototype.reverse_foo = function() {
  return this.foo.reverse();
}
EOS
  end

  def test_call_block_as_jsfunction
    $func = Proc.new { |i| i + 7 }
    run_js "answer = {}; answer.func = func(35);"
    assert_not_nil($answer)
    assert_equal(42, $answer['func'].to_i)
  end

  def test_class_info
    assert_not_nil($scope['JSFunctionTestJSClass'])
    assert_kind_of(JSFunction, $scope['JSFunctionTestJSClass'])
    assert(Object.constants.include?('JSFunctionTestJSClass'), "Constant JSFunctionTestJSClass should be defined for Object")
    assert_equal('Class', JSFunctionTestJSClass.class.name)
    assert_equal('JSFunctionTestJSClass', JSFunctionTestJSClass.name)
  end

  # Create a new JavaScript constructor within Ruby by loading JS code, then
  # create an instance of that object and use it.
  def test_new_js_object_in_ruby
    x = JSFunctionTestJSClass.new('Ruby')
    assert_not_nil(x)
    assert_equal("ybuR", x.reverse_foo)
  end

  # Same as test_new_js_object_in_ruby but without an intermediary object.
  def test_new_js_object_in_ruby_inline
    assert_equal("ybuR", JSFunctionTestJSClass.new('Ruby').reverse_foo)
  end

end
