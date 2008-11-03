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
require 'xgen/sql'

class SQLTest < RubyTest

  include XGen::SQL

  def assert_done(t)
    assert !t.more?
    assert_nil t.next_token
  end

  def test_tokenizer
    t = Tokenizer.new('clicked = 1')
    assert_equal 'clicked', t.next_token
    assert_equal '=', t.next_token
    assert_equal 1, t.next_token
    assert_done t

    t = Tokenizer.new('clicked=1 ')
    assert_equal 'clicked', t.next_token
    assert_equal '=', t.next_token
    assert_equal 1, t.next_token
    assert !t.more?
    assert_done t

    t = Tokenizer.new('clicked2=1 ')
    assert_equal 'clicked2', t.next_token
    assert_equal '=', t.next_token
    assert_equal 1, t.next_token
    assert_done t

    t = Tokenizer.new('clicked=1 and foo = 5')
    assert_equal 'clicked', t.next_token
    assert_equal '=', t.next_token
    assert_equal 1, t.next_token
    assert_equal 'and', t.next_token
    assert_equal 'foo', t.next_token
    assert_equal '=', t.next_token
    assert_equal 5, t.next_token
    assert_done t

    t = Tokenizer.new("name = 'foo'")
    assert_equal 'name', t.next_token
    assert_equal '=', t.next_token
    assert_equal 'foo', t.next_token
    assert_done t

    t = Tokenizer.new("name = \"bar\"")
    assert_equal 'name', t.next_token
    assert_equal '=', t.next_token
    assert_equal 'bar', t.next_token
    assert_done t

    t = Tokenizer.new("name = 'foo''bar'")
    assert_equal 'name', t.next_token
    assert_equal '=', t.next_token
    assert_equal "foo'bar", t.next_token
    assert_done t

    t = Tokenizer.new("age <= 42")
    assert_equal 'age', t.next_token
    assert_equal '<=', t.next_token
    assert_equal 42, t.next_token
    assert_done t

    t = Tokenizer.new("age <> 42")
    assert_equal 'age', t.next_token
    assert_equal '<>', t.next_token
    assert_equal 42, t.next_token
    assert_done t
  end

  def test_strip_table_name
    w = Parser.parse_where("user.name = 'foo'", true)
    assert_equal 'foo', w['name']
    w = Parser.parse_where("schema.table.column = 'foo'", true)
    assert_equal 'foo', w['column']

    w = Parser.parse_where("user.name = 'foo'")
    assert_equal 'foo', w['user.name']
    w = Parser.parse_where("schema.table.column = 'foo'")
    assert_equal 'foo', w['schema.table.column']
  end

  def test_arrays
    w = Parser.parse_where("name in (1, 2, 42)")
    a = w['name'][:$in]
    assert_equal Array, a.class
    assert_equal 3, a.length
    assert_equal 1, a[0]
    assert_equal 2, a[1]
    assert_equal 42, a[2]
  end

  def test_regex
    p = Parser.new('')
    assert_equal /foo/i, p.regexp_from_string('%foo%')
    assert_equal /^foo/i, p.regexp_from_string('foo%')
    assert_equal /foo$/i, p.regexp_from_string('%foo')
    assert_equal /^foo$/i, p.regexp_from_string('foo')
  end

  def test_parser
    w = Parser.parse_where('clicked = 1 ')
    assert_equal 1, w['clicked']

    w = Parser.parse_where('clicked = 1 and z = 3')
    assert_equal 1, w['clicked']
    assert_equal 3, w['z']

    w = Parser.parse_where("name = 'foo'")
    assert_equal 'foo', w['name']

    w = Parser.parse_where("name like '%foo%'")
    assert_equal /foo/i, w['name']
    w = Parser.parse_where("name like 'foo%'")
    assert_equal /^foo/i, w['name']

    w = Parser.parse_where("foo <> 'bar'")
    assert_equal 'bar', w['foo'][:$ne]
    w = Parser.parse_where("foo != 'bar'")
    assert_equal 'bar', w['foo'][:$ne]

    w = Parser.parse_where("foo in (1, 2, 'a')")
    assert_equal "1, 2, a", w['foo'][:$in].join(', ')

    w = Parser.parse_where("foo in ('a', 'b', 'c')")
    assert_equal "a, b, c", w['foo'][:$in].join(', ')

    w = Parser.parse_where("name = 'the word '' or '' anywhere (surrounded by spaces) used to throw an error'")
    assert_equal "the word ' or ' anywhere (surrounded by spaces) used to throw an error", w['name']

    sql = "name = 'foo' or name = 'bar'"
    err = "sql parser can't handle ors yet: #{sql}"
    begin
      w = Parser.parse_where(sql)
      fail("expected to see \"#{err}\" error")
    rescue => ex
      assert_equal err, ex.to_s
    end
  end

end
