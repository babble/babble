
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

def silly( n )
  puts "_"
  puts self.A
  puts n
end

class Lower

  A = 5
  
  attr_accessor :all
  
  def initialize
    puts "Lower.initialize"
    @all = [ "1" ]
  end
  
  def a
    return @all
  end
  
  def self.A
    return 111
  end
  
  def self.funny
    puts "funny"
  end
  
end

class Base < Lower
  
  silly( "2" )

  def blah(z)
    @z = z
  end

end

class A < Base
  def go
    blah( 5 )
  end

  def p
    puts @all.length
    puts @z
  end
end

class B < Base
  def go
    blah( 6 )
  end

  def p
    puts @all.length
    puts @z
  end

  def self.blah
    puts "B.self.blah"
    funny
  end
    
end

a = A.new
a.go
a.p

b = B.new
b.go
b.p

a.p


B.blah
