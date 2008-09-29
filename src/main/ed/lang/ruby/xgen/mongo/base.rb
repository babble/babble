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

require 'xgen/mongo/cursor'

module XGen

  module Mongo

    # A superclass for database collection instances.
    #
    # If you override initialize, make sure to call the superclass version,
    # passing it the database row or hash that it was given.
    #
    # Example:
    #
    #    class MP3Track < XGen::Mongo::Base
    #      collection_name :mp3_track
    #      fields :artist, :album, :song, :track
    #      def to_s
    #        "artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{track}"
    #      end
    #    end
    #
    #    track = MP3Track.find_by_song('She Blinded Me With Science')
    #    puts track.to_s
    class Base

      class << self # Class methods

        def inherited(subclass)
          subclass.instance_variable_set("@coll_name", subclass.name.gsub(/([A-Z])/, '_\1').downcase.sub(/^_/, ''))
          subclass.instance_variable_set("@field_names", [])
          subclass.field(:_id)
        end

        # Call this method to set the Mongo collection name for this class.
        # The default value is the class name turned into
        # lower_case_with_underscores.
        def collection_name(coll_name)
          @coll_name = coll_name
        end

        # Creates one or more collection fields. Each field will be saved to
        # and loaded from the database. Then field named "_id" is
        # automatically saved and loaded.
        #
        # The method "field" is also called "fields"; you can use either one.
        def field(*fields)
          fields.each { |field|
            field = field.to_sym
            unless @field_names.include?(field)
              ivar_name = "@" + field.to_s
              define_method(field, lambda { instance_variable_get(ivar_name) })
              define_method("#{field}=".to_sym, lambda { |val| instance_variable_set(ivar_name, val) })
              @field_names << field
            end
          }
        end
        alias_method :fields, :field

        def field_names
          @field_names ||= []
        end

        # The collection object.
        def coll
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
        #
        # * Find all records if there are no args
        def find(*args)
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
        def findOne(*args)
          find(:first, *args)
        end

        # Returns all records matching mql. Not yet implemented.
        def find_by_mql(mql)    # :nodoc:
          raise "not implemented"
        end
        alias_method :find_by_sql, :find_by_mql

        # Returns the number of matching records.
        def count(*args)
          find(:all, *args).length
        end

        # Deletes the record with the given id from the collection.
        def delete(id)
          coll.remove({:_id => id})
        end
        alias_method :remove, :delete

        # Deletes all matching records. If you want to remove everything in
        # the collection, pass in an empty hash.
        def delete_all(*args)
          coll.remove(*args)
        end

        # Creates, saves, and returns a new database object.
        def create(values_hash)
          self.new(values_hash).save
        end

        # Handles find_* methods such as find_by_name and find_all_by_shoe_size.
        def method_missing(sym, *args)
          if match = /^find_(all_by|by)_([_a-zA-Z]\w*)$/.match(sym.to_s)
            find_how_many = ($1 == 'all_by') ? :all : :first
            field_names = $2.split(/_and_/)
            super unless all_fields_exist?(field_names)
            search = search_from_names_and_values(field_names, args)
            self.find(find_how_many, search, *args[field_names.length..-1])
          elsif match = /^find_or_(initialize|create)_by_([_a-zA-Z]\w*)$/.match(sym.to_s)
            create = $1 == 'create'
            field_names = $2.split(/_and_/)
            super unless all_fields_exist?(field_names)
            search = search_from_names_and_values(field_names, args)
            row = self.find(:first, search, *args[field_names.length..-1])
            obj = self.new(row)
            obj.save if create
            obj
          else
            super
          end
        end

        private

        # Returns true if all field_names are in @field_names.
        def all_fields_exist?(field_names)
          (field_names - @field_names.collect{|f| f.to_s}).empty?
        end

        # Returns a db search hash, given field_names and values.
        def search_from_names_and_values(field_names, values)
          h = {}
          field_names.each_with_index { |iv, i| h[iv.to_sym] = values[i] }
          h
        end
      end

      # Initialize a new object with either a hash of values or a row returned
      # from the database.
      def initialize(row={})
        case row
        when Hash
          row.each { |k, v|
            k = '_id' if k == 'id' # Rails helper
            v = nil if v == '' && k == '_id'
            instance_variable_set("@#{k}", v)
          }
        else
          row.instance_variables.each { |v|
            name = v[1..-1]
            instance_variable_set("@#{name}", row.get(name))
          }
        end
        self.class.field_names.each { |iv|
          iv = "@#{iv}"
          instance_variable_set(iv, nil) unless instance_variable_defined?(iv)
        }
      end

      def id=(val); @_id = (val == '' ? nil : val); end
      def id; @_id ? @_id.to_s : nil; end

      # Rails convenience method.
      def to_param
        @_id.to_s
      end

      # Saves and returns self.
      def save
        h = {}
        self.class.field_names.each { |iv| h[iv] = instance_variable_get("@#{iv}") }
        row = self.class.coll.save(h)
        if @_id == nil
          @_id = row._id
        elsif row._id != @_id
          raise "Error: after save, database id changed"
        end
        self
      end

      # Removes self from the database and sets @_id to nil. If self has no
      # @_id, does nothing.
      def delete
        if @_id
          self.class.coll.remove({:_id => self._id})
          @_id = nil
        end
      end
      alias_method :remove, :delete

    end

  end

end
