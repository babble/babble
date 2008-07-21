
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
    @good = {}
  end
  
  def blah
    puts "blah"
    return true
  end

  def foo
    self.blah and @good[:soon]="asdasd"
  end

  def foo2
    self.blah and @good[:soon]="asdasd"[:abc]
  end
  
  def foo3
    this is just to check compliaation
    self.blah and @good[:soon]="asdasd"[:abc]
    5
    respond_to do |format|
            format.html { redirect_to edit_user_path(@user) }
            format.xml  { head 200 }
  end
  end

end

a = A.new

a.foo
a.foo2


f = "asdsaD"
puts f[:asd] == nil
