
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

  Z = 101

  def self.foo
    puts "foo"
  end

  def self.bar(z)
    puts "bar#{z}"
  end
  
end

class B < A
  
  Z = 102
  
  puts "h1"
  foo
  puts "h2"
  bar(5)
  puts "h3"
end

puts A::Z
puts B::Z

b = B.new
b = B.new
    
class AA

  attr_accessor :z

  def initialize
    puts "AA cons"
    @z = 111;
  end

end

class BB < AA
end

bb = BB.new
puts bb.z
    
    
