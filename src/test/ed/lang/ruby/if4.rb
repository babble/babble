
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

def blah(z)
  gar = nil;
  foo = 
    if z == 5
      gar = 1
      "five"
    elsif z == 6
      gar = 2
      "six"
    elsif z == 7
      gar = 3
      "six"
    else
      gar = 4
      "stupid"
    end
  puts gar
  return foo
end

puts blah(5)
puts blah(6)
puts blah(7)
puts blah(8)
