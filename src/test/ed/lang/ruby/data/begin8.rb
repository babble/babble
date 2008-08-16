
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

def go(z)
  begin
    if ( z == 4 )
      raise 1
    end
    puts z if z == 5
    if ( z == 6 )
      puts "six"
      return
    end
  rescue 
    puts "r"
  else
    puts "e"
  end
end

go( 5 )
go( 4 )
go( 6 )
