
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
    puts "in A initialize"
    @bar = 1;
  end
end

class B < A

  def initialize
    super
    puts "B"
    @foo = 2;
  end
  
  def go
    puts @bar;
    puts @foo;
  end
end

b = B.new
b.go
 

puts( "***" );

class AA

  def iamaa
  end
  
  def initialize( bar )
    @bar = bar;
  end
end

class BB < AA

  def iambb
  end

  def initialize
    super 3
    puts "BB"
    @foo = 2;
  end
  
  def go
    puts "bar #{@bar}"
    puts "foo #{@foo}"
  end
end

bb = BB.new
bb.go
 
