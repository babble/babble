
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

def go( a , b )
  return a , b
end

class A
  def blah
    @a , @b = go( 3 , 5 )
    puts @a
    puts @b
    @a , @b = go( 4 , 7 )
  end
end

a = A.new
z = a.blah
puts z[0] + z[1]
m,n = a.blah
puts m
puts n


class B 
  def redirect_to(z)
    puts z
    return 6;
  end
  
  def foo
    zz = ( redirect_to :login_path and return unless @user );
    return 7;
  end
  
  def foo2
    redirect_to :login_path and return unless @user;
    return 7
  end

 def z
   @user = 5
 end

end

b = B.new
b.foo
b.foo2
b.z
b.foo



def blah(z)
  return z ? [ 1 , 2 ] : [ nil , nil ]
end

puts blah( true ) == 1
puts blah( false ) == nil
