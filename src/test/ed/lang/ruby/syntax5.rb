
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

puts( 5 | 2 )

if ( true | false )
  puts "y";
end

blah = 5;

crap = false;
silly = false;
silly |= ( blah == 5 ) if crap 
puts silly

crap = true;
silly = false;
silly |= ( blah == 5 ) if crap 
if( silly )
  puts "y"
end
