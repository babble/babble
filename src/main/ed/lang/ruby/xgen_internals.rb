# Copyright (C) 2008 10gen Inc.
#
# This program is free software: you can redistribute it and/or  modify
# it under the terms of the GNU Affero General Public License, version 3,
# as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

class Object

  def to_xgen
    self
  end

  alias_method :old_method_missing, :method_missing

  def method_missing(sym, *args, &block)
    name = sym.to_s
    if name[-1,1] == "="        # reader
      if self.respond_to?(:set)
        key = name[0..-2]
        return self.set(key.to_xgen, args[0].to_xgen)
      else
        return old_method_missing(name, args, block)
      end
    else
      if self.respond_to?(:get)
        val = self.get(name.to_xgen)
        if val.respond_to?(:call) # function
          return val.call($scope, *args)
        else                    # writer
          return val
        end
      else
        return old_method_missing(sym, args, block)
      end
    end
  end

end

class Hash
  def to_xgen
    h = Java::EdJs::JSObjectBase.new
    self.each { |key, val| h.set(key.to_xgen, val.to_xgen) }
    h
  end
end

class Array
  def to_xgen
    a = Java::EdJs::JSArray.new
    self.each { |obj| a.add(obj.to_xgen) }
    a
  end
end

class String
  def to_xgen
    Java::EdJs::JSString.new(self)
  end
end
