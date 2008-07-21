
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

class Foo

  def initialize
    @counter = 1;
  end
  
  def inc
    @counter += 1;
  end
  
  def jump( num )
    @counter += num;
  end
  
  def print
    puts "num clicks #{@counter}\n";
  end
end


f = Foo.new();
f.print();
f.inc();
f.print();
f.jump( 5 );
f.print();

#testing empty class
class Foo2
end


class Foo3 < Foo 
  
  def inc
    @counter += 2;
  end
  
  def print
    puts "hhaha\n";
  end

end


f3 = Foo3.new();
f3.jump( 5 );
f3.print();
