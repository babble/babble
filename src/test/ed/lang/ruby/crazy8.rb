
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
    @foo = 7
    puts @foo
  end
  
  def foo=(z)
    @foo = z + 1
    puts "yo #{@foo}"
  end
  
  def go(n)
    @foo = n
    puts @foo
  end

  def p
    puts @foo
  end
end

a = A.new
a.go(5)
a.go(6);
    
a.foo= 19

a.p
