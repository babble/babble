
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

class A 

  def initialize
    puts 1
  end
  
  def foo
    puts "foo"
  end
end

class A 

  def initialize
    puts 2
  end

  def bar
    puts "bar"
  end
end

a = A.new
a.bar
a.foo

class B

  foo = 5

  if foo == 5 
    def b
      puts "101"
    end
  elsif
    def b 
      puts "102"
    end
  end

  if foo == 6
    def c
      puts "103"
    end
  elsif
    def c 
      puts "104"
    end
  end


end

b = B.new
b.b
b.c
