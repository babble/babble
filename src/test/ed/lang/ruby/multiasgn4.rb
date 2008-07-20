
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

def foo
  return [ 1 , 2 ];
end

puts( foo().length );
a , b = foo;
puts a
puts b

class A
  attr_accessor :c
end

a = A.new

a.c , b = foo
puts a.c
puts b

a = [5,6];
puts a[0]
puts a[1]
a[5-5] , a[1] = foo
puts a[0]
puts a[1]


puts( ( a , b = foo ).length );
puts a
puts b

def blah
  yield 1 , 2 
end

blah do |a,b|
  puts a + b
end


class BAR
  def bar
    @a , @b = foo
    puts @a
    puts @b
  end
end

b = BAR.new
b.bar
