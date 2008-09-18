# Copyright (C) 2008 10gen Inc.
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU Affero General Public License, version 3, as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.

module XGen

  module Mongo

    class Cursor
      include Enumerable

      # Forward missing methods to the cursor itself.
      def method_missing(sym, *args, &block)
        return @cursor.send(sym, *args)
      end

      def initialize(db_cursor, model_class)
        @cursor, @model_class = db_cursor, model_class
      end

      def each
        @cursor.forEach { |row| yield @model_class.new(row) }
      end

      %w(sort limit skip).each { |name|
        eval "def #{name}(*args); @cursor.#{name}(*args); return self; end"
      }

      # This is for JavaScript code that needs to call toArray on the @cursor.
      def toArray
        @cursor.toArray
      end
    end

    # A superclass for database collection instances. It creates find_by_*
    # methods for the instance variables you pass to set_collection.
    #
    # If you override initialize, make sure to call the superclass version,
    # passing it the database row or hash that it was given.
    #
    # Example:
    # =>
    #    class MP3Track < XGen::Mongo::Base
    #      set_collection :mp3_track, %w(artist album song track)
    #      def to_s
    #        "artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{track}"
    #      end
    #    end
    # =>
    #    track = MP3Track.find_by_song('She Blinded Me With Science')
    #    puts track.to_s
    class Base

      # Call this method to initialize your class with the database collection
      # and instance variable names. If coll_name is not given, the collection
      # name is assumed to be the class name turned into
      # lower_case_with_underscores.
      #
      #    set_collection :collection_name, %w(var1 var2)
      #    set_collection %w(var1 var2)
      def self.set_collection(coll_name, ivar_names=nil)
        @coll_name, @ivar_names = coll_name, ivar_names
        if coll_name.kind_of?(Array)
          @ivar_names = coll_name
          @coll_name = self.name.gsub(/([A-Z])/, '_\1').downcase.sub(/^_/, '')
        end

        @ivar_names << '_id' unless @ivar_names.include?('_id')
      @ivar_names.each { |ivar|
          attr_method = ivar == '_id' ? 'attr_reader' : 'attr_accessor'
          eval <<EOS
#{attr_method} :#{ivar}
def self.find_by_#{ivar}(v, *args)
  self.find(:first, {:#{ivar} => v}, *args)
end
def self.find_all_by_#{ivar}(v, *args)
  self.find(:all, {:#{ivar} => v}, *args)
end
if "#{ivar}" != '_id'
  def self.find_or_create_by_#{ivar}(h)
    o = self.find(:first, {:#{ivar} => h[:#{ivar}]})
    return o if o && o._id != nil
    self.new(h).save
  end
end
EOS
          }
      end

      def self.ivar_names
        @ivar_names ||= []
      end

      # The collection object.
      def self.coll
        @coll ||= $db[@coll_name.to_s]
      end

      # Find one or more database objects.
      #
      # * Find by id (a single id or an array of ids)
      #
      # * Find :first that matches hash search params
      #
      # * Find :all records; returns a Cursor that can iterate over raw
      #   records
      def self.find(*args)
        return Cursor.new(coll.find(), self) unless args.length > 0 # no args, find all
        return case args[0]
               when String      # id
                 row = coll.findOne(args[0])
                 (row.nil? || row['_id'] == nil) ? nil : self.new(row)
               when Array       # array of ids
                 args.collect { |arg| find(arg.to_s) }
               when :first
                 args.shift
                 row = coll.findOne(*args)
                 (row.nil? || row['_id'] == nil) ? nil : self.new(row)
               when :all
                 args.shift
                 Cursor.new(coll.find(*args), self)
               else
                 Cursor.new(coll.find(*args), self)
               end
      rescue => ex
        nil
      end

      # Find a single database object. See find().
      def self.findOne(*args)
        find(:first, *args)
      end

      # Creates, saves, and returns a new database object.
      def self.create(values_hash)
        self.new(values_hash).save
      end

      # If an unknown method is an assignment, create the ivar and assign it.
      # Else pass the unknown method call to the collection.
      def method_missing(sym, *args, &block)
        if sym.to_s[-1,1] == '='       # assignment to an unknown ivar
          name = sym.to_s[0..-2]
          instance_variable_set("@#{name}", args[0])
          instance_eval "def #{name}; @#{name}; end; def #{name}=(val); @name = val; end"
        else
          super
        end
      end

      # Initialize a new object with either a hash of values or a row returned
      # from the database.
      def initialize(row={})
        case row
        when Hash
          row.each { |k, v|
            instance_variable_set("@#{k}", v)
          }
        else
          row.instance_variables.each { |v|
            name = v[1..-1]
            instance_variable_set("@#{name}", row.get(name))
          }
        end
        self.class.ivar_names.each { |iv|
          iv = "@#{iv}"
          instance_variable_set(iv, nil) unless instance_variable_defined?(iv)
        }
      end

      # Saves and returns self.
      def save
        h = {}
        self.class.ivar_names.each { |iv| h[iv] = instance_variable_get("@#{iv}") }
        row = self.class.coll.save(h)
        if self._id == nil
          self._id = row._id
        elsif row._id != self._id
          raise "Error: after save, database id changed"
        end
        self
      end

      # Removes self from the database. Must have an _id.
      def remove
        self.class.coll.remove({:_id => self._id}) if self._id
      end

    end

  end

end

# A convenience method that escapes text for HTML.
def h(o)
  o.to_s.gsub(/&/, '&amp;').gsub(/</, '&lt;').gsub(/>/, '&gt;').gsub(/'/, '&apos;').gsub(/"/, '&quot;')
end
