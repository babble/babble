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

  def test_call_block_as_jsfunction
    $func = Proc.new { |i| i + 7 }
    run_js "answer = {}; answer.func = func(35);"
    assert_not_nil($answer)
    assert_equal(42, $answer['func'].to_i)
  end

end
