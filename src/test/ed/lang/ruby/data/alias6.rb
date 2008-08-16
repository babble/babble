
=begin
    Copyright (C) 2008 10gen Inc.
  
    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
  
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
=end

class Foo
  
  def silly
    puts "A"
  end

  alias silly2 silly
  
end

f = Foo.new
f.silly
f.silly2

class Foo
  alias silly3 silly
  def silly
    puts "B"
  end
end

f = Foo.new
f.silly
f.silly2
f.silly3

class Foo
  alias silly3 silly
  def silly
    puts "B"
  end
  alias << silly
end

f = Foo.new
f.silly
f.silly2
f.silly3
