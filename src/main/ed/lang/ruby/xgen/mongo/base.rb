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
    #      set_collection :mp3_track, %w(artist album song track)
    #      def to_s
    #        "artist: #{self.artist}, album: #{self.album}, song: #{self.song}, track: #{track}"
    #      end
    #    end
    #
    #    track = MP3Track.find_by_song('She Blinded Me With Science')
    #    puts track.to_s
    class Base

      class << self # Class methods
        # Call this method to initialize your class with the database
        # collection and instance variable names. If collection_name is not
        # given, the collection name is assumed to be the class name turned
        # into lower_case_with_underscores.
        #
        #    set_collection :collection_name, %w(var1 var2)
        #    set_collection %w(var1 var2)
        def set_collection(coll_name, ivar_names=nil)
          @coll_name, @ivar_names = coll_name, ivar_names
          if coll_name.kind_of?(Array)
            @ivar_names = coll_name
            @coll_name = self.name.gsub(/([A-Z])/, '_\1').downcase.sub(/^_/, '')
          end

          @ivar_names << '_id' unless @ivar_names.include?('_id')
          @ivar_names.each { |ivar|
            attr_method = ivar == '_id' ? 'attr_reader' : 'attr_accessor'
            ivar_name = "@" + ivar
            define_method(ivar.to_sym, lambda { instance_variable_get(ivar_name) })
            define_method("#{ivar}=".to_sym, lambda { |val| instance_variable_set(ivar_name, val) }) unless ivar == '_id'
          }
        end

        def ivar_names
          @ivar_names ||= []
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
            ivar_names = $2.split(/_and_/)
            super unless all_ivars_exist?(ivar_names)
            search = search_from_names_and_values(ivar_names, args)
            self.find(find_how_many, search, *args[ivar_names.length..-1])
          elsif match = /^find_or_(initialize|create)_by_([_a-zA-Z]\w*)$/.match(sym.to_s)
            create = $1 == 'create'
            ivar_names = $2.split(/_and_/)
            super unless all_ivars_exist?(ivar_names)
            search = search_from_names_and_values(ivar_names, args)
            row = self.find(:first, search, *args[ivar_names.length..-1])
            obj = self.new(row)
            obj.save if create
            obj
          else
            super
          end
        end

        private

        # Returns true if all ivar_names are in @ivar_names.
        def all_ivars_exist?(ivar_names)
          (ivar_names - @ivar_names).empty?
        end

        # Returns a db search hash, given ivar_names and values.
        def search_from_names_and_values(ivar_names, values)
          h = {}
          ivar_names.each_with_index { |iv, i| h[iv.to_sym] = values[i] }
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
        self.class.ivar_names.each { |iv|
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
        self.class.ivar_names.each { |iv| h[iv] = instance_variable_get("@#{iv}") }
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
