
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

def foo z
  puts "foo"
  puts z
  raise 5
end

def save
  puts "save"
  return 111;
end

def bar
  return ( foo 6 rescue save )
end

puts bar

def bar2
  return ( foo 7 rescue nil )
end

bar2

def bar3
  6
end

puts bar3
