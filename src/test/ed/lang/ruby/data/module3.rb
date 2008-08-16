
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

module A4

  def self.included( base )
    puts "yo"
    base.extend( B )
    base.send( :include ,  C );
  end
  
  module B
    def silly
      puts "silly"
      return 171
    end
  end
  
  module C 
    def barrrr
      puts "bar";
    end
  end
  
end

class Foo
  include A4

  def initialize
    puts "fooie"
  end
  
end


f = Foo.new
f = Foo.new

puts Foo.silly

f.barrrr


  
