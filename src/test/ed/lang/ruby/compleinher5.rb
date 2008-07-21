
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

module Foo

  def fun(z)
    #helper( z + 3 )
    puts @a
  end
  
  def helper(y)
    puts "helper #{@a + y} "
    helper2(2)
  end

  def helper2(z)
    puts @a
    puts "2 #{z}"
    helper3(z)
    self.helper3(z)
  end

  def helper3(z)
    puts "3 #{z + @a}"
  end
end

class A
  def initialize
    @a = 6
  end
end

A.send :include , Foo

a = A.new
a.fun(5)

puts( "----" );


class B < A
  def blah(z)
    fun(z+1)
  end
end

b = B.new
b.fun(9)
# b.blah(12)
