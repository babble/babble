
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

begin
  puts "A"
rescue
  puts "B"
end

begin
  puts "A"
  raise "Asd"
  puts "C"
rescue
  puts "B"
end

class Silly < Exception
  def silly
    puts "silly"
  end
end

begin
  begin
    raise 5
  rescue Silly
    puts "inner1"
  end
  puts "what1"
rescue
  puts "outer1"
end


begin
  begin
    raise Silly
  rescue Silly
    puts "inner2"
  end
  puts "what2"
rescue
  puts "outer2"
end
