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

module Kernel

  unless Kernel.private_methods.include?('xgen_old_method_missing')
    alias_method :xgen_old_method_missing, :method_missing
  end

  def method_missing(sym, *args, &block)
    name = sym.to_s
    if name[-1,1] == "="        # writer
      if self.respond_to?(:set)
        key = name[0..-2]
        return self.set(key.to_xgen, args[0].to_xgen)
      else
        return xgen_old_method_missing(sym, *args, &block)
      end
    elsif self.respond_to?(:call) # self is a function; call it
      return self.call($scope, *(args.collect{|a| a.to_xgen}))
    elsif self.respond_to?(:get)
      val = self.get(name.to_xgen)
      if val.respond_to?(:call) # function call
        return val.call($scope, *(args.collect{|a| a.to_xgen}))
      else                    # reader
        return val
      end
    else
      ivar_name = "@#{sym}"
      if self.instance_variable_defined?(ivar_name) && self.instance_variable_get(ivar_name).respond_to?(:call)
        return self.instance_variable_get(ivar_name).call($scope, *(args.collect{|a| a.to_xgen}))
      else
        return xgen_old_method_missing(sym, *args, &block)
      end
    end
  end

end

class Object
  def to_xgen
    self
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
  def to_oid
    Java::EdDb::ObjectId.new(self)
  end
end

class Symbol
  def to_xgen
    Java::EdJs::JSString.new(self.to_s)
  end
end

class Java::EdJs::JSObjectBase
  def to_hash
    h = {}
    keySet.to_a.each { |k| h[k] = get(k) }
    h
  end
end

# A convenience method that escapes text for HTML.
def h(o)
  o.to_s.gsub(/&/, '&amp;').gsub(/</, '&lt;').gsub(/>/, '&gt;').gsub(/'/, '&apos;').gsub(/"/, '&quot;')
end
