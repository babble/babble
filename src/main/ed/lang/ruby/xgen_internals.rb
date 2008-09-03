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

  class ModelCursor
    include Enumerable

    def initialize(db_cursor, model_class)
      @cursor, @model_class = db_cursor, model_class
    end

    def each
      @cursor.forEach { |row| yield @model_class.new(row) }
    end

  end

  # A superclass for database collection instances. It creates find_by_*
  # methods for the instance variables you pass to set_collection.
  #
  # If you override initialize, make sure to call the superclass version,
  # passing it the database row or hash that it was given.
  #
  # Example:
  #
  #    class MP3Track < XGen::ModelBase
  #      set_collection :mp3_track, %w(artist album song track)
  #      def to_s
  #        "artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{track}"
  #      end
  #    end
  #
  #    track = MP3Track.find_by_song('She Blinded Me With Science')
  #    puts track.to_s
  class ModelBase

    # Call this method to initialize your class with the database collection
    # and instance variable names. If coll_name is not given, the collection
    # name is assumed to be the class name turned into
    # lower_case_with_underscores.
    #
    #    set_collection :collection_name, %w(var1 var2)
    #    set_collection %w(var1 var2)
    def self.set_collection(coll_name, ivars=nil)
      @coll_name, @ivars = coll_name, ivars
      if coll_name.kind_of?(Array)
        @ivars = coll_name
        @coll_name = self.name.gsub(/([A-Z])/, '_\1').downcase.sub(/^_/, '')
      end
      @coll = $db[@coll_name.to_s]

      @ivars << '_id' unless @ivars.include?('_id')
      @ivars.each { |ivar|
        eval <<EOS
attr_accessor :#{ivar}
def self.find_by_#{ivar}(v, *args)
  self.find(:first, {:#{ivar} => v}, *args)
end
def self.find_all_by_#{ivar}(v, *args)
  self.find(:all, {:#{ivar} => v}, *args)
end
def self.find_or_create_by_#{ivar}(h)
  o = self.find(:first, {:#{ivar} => h[:#{ivar}]})
  return o if o && o._id != nil
  self.new(h).save
end
EOS
      }
    end

    def self.coll
      @coll
    end

    # Find one or more database objects.
    #
    # * Find by id (a single id or an array of ids)
    #
    # * Find :first that matches hash search params
    #
    # * Find :all records; returns a ModelCursor that can iterate over raw
    #   records
    def self.find(*args)
      return ModelCursor.new(@coll.find(), self) unless args.length > 0
      return case args[0]
             when String        # id
               self.new(@coll.findOne(args[0]))
             when Array         # array of ids
               args.collect { |arg| self.new(@coll.findOne(arg.to_s)) }
             when :first
               self.new(@coll.findOne(*args[1..-1]))
             when :all
               ModelCursor.new(@coll.find(*args[1..-1]), self)
             end
    end

    # Find a single database object. See find().
    def self.findOne(*args)
      find(:first, *args)
    end

    # Creates, saves, and returns a new database object.
    def self.create(values_hash)
      self.new(values_hash).save
    end

    # Pass unknown methods to the collection.
    def method_missing(sym, *args, &block)
      o = self.class.coll.send(sym, args)
      yield o if block_given?
    end

    # Initialize a new object with either a hash of values or a row returned
    # from the database.
    def initialize(row)
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
    end

    # Saves and returns self.
    def save
      row = self.class.coll.save(self)
      if self._id == nil
        self._id = row._id
      elsif row._id != self._id
        raise "Error: after save, database id changed"
      end
      self
    end

  end

end

# A convenience method that escapes text for HTML.
def h(o)
  o.to_s.gsub(/&/, '&amp;').gsub(/</, '&lt;').gsub(/>/, '&gt;').gsub(/'/, '&apos;').gsub(/"/, '&quot;')
end
