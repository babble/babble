
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

def foo(z)
  if ( z == 4 )
    "four"
  elsif ( z == 5 )
    "five"
  else
    true
  end
end

puts foo(4) 
puts foo(5) 
puts foo(6) 
    

def blah(z)
  (z==5) ? true : false
end

blah(5)
blah(6)

def blah2(z)
  if( z== 5)
    false
  else
    puts "a"
    true
  end
end


puts blah2(5)
puts blah2(6)


def blah3(z)
  if( z== 5)
    false
  else
    puts "a"
    true
  end
  puts "z"
end


puts blah3(5) == nil
puts blah3(6) == nil
