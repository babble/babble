
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
  def foo(z)
    "A.foo";
  end
end

a = A.new
puts a.foo( true )

class B < A
  def foo(z)
    "B.foo"
  end
end

b = B.new
puts b.foo( true )

class C < A
  def foo(z)
    if ( z )
      return "C.foo"
    end
    super
  end
end

c = C.new
puts c.foo( true )
puts c.foo( false )
