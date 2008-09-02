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

  # A superclass for database collection instances. It creates find_by_*
  # methods for the instance variables you pass to init.
  #
  # If you override initialize, make sure to call the superclass version,
  # passing it the database row that it was given.
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
    # and instance variable names.
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
def self.find_by_#{ivar}(v)
  self.new(@coll.findOne({:#{ivar} => v}))
end
EOS
      }
    end

    def self.coll
      @coll
    end

    # Returns a cursor object.
    def self.find(*args)
      @coll.find(*args)
    end

    # Returns a new subclass instance.
    def self.findOne(*args)
      self.new(@coll.findOne(*args))
    end

    def method_missing(sym, *args, &block)
      puts "method_missing #{sym}"
      o = self.class.coll.send(sym, args)
      yield o if block_given?
    end

    def initialize(row)
      case row
      when Hash
        row.each { |k, v|
          instance_variable_set("@#{k}", v)
        }
      else
        puts "class of row is #{row.class.name}"
        row.instance_variables.each { |v|
          name = v[1..-1]
          instance_variable_set("@#{name}", row.get(name))
        }
      end
    end

    def save
      self.class.coll.save(self)
    end

  end

end

# A convenience method that escapes text for HTML.
def h(o)
  o.to_s.gsub(/&/, '&amp;').gsub(/</, '&lt;').gsub(/>/, '&gt;').gsub(/'/, '&apos;').gsub(/"/, '&quot;')
end
