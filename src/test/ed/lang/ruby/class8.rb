
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

  def initialize(b)
    @blah = b
  end
  
  def a 
    puts "a1"
    b
    puts "a2"
    c( 5 )
    puts "a3"
  end
  
  def b
    puts "b"
  end

  def c(z)
    puts "from c #{@blah} "
    puts "c #{z}"
    zz
  end

  def zz
    puts "from zz #{@blah} "
  end
end

f = Foo.new(17171)
f.a

  
