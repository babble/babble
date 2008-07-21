
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

class SillyA < Exception
  def silly
    puts "silly"
  end
end

class SillyB < Exception
  def silly
    puts "silly"
  end
end

def go(z)
  begin
    if z == 5
      raise SillyA
    else
      raise SillyB
    end
  rescue SillyA
    puts "a"
  rescue SillyB
    puts "b"
  ensure
    puts "basdlkajsd"
  end
  
end
    
go(5)
go(6)


def go2(z)
  begin
    if z == 5
      raise SillyA
    elsif z == 6
      raise SillyB
    else
      raise 5
    end
  rescue SillyA
    puts "a"
  rescue SillyB
    puts "b"
  rescue
    puts "c"
  end
  
end
    
go(5)
go(6)
go(7)
