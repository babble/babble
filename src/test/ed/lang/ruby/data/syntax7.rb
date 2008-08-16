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

def valid_email?(email)
  email.size < 100 && email =~ /.@.+\../ && email.count('@') == 1
end

def foo(z)
  case z
  when 6
    "six"
  when 7
    "seven"
  else
    "blah"
  end
end

foo( 6 );
foo( 7 );
foo( 8 );
  

class A
  def delete
    puts "here"
  end
end

a = A.new
a.delete

def blah( a , b )
  puts "a:#{a} b:#{b}"
end

blah( 7 , 10 );


def blah2( a , b )
  puts "a:#{} b:#{b}"
end

blah( 7 , 10 );

z = nil
puts "abc#{ z }def"
z = 5
puts "abc#{ z unless z == 5 }def"
puts "abc#{ z unless z == 4 }def"
