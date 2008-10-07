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
require '/core/db/sql.js'

class Object
  def to_mongo_value
    self
  end
end
class Array
  def to_mongo_value
    self.collect {|v| v.to_mongo_value}
  end
end
class Hash
  def to_mongo_value
    h = {}
    self.each {|k,v| h[k] = v.to_mongo_value}
    h
  end
end

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
          subclass.instance_variable_set("@coll_name", class_name_to_field_name(subclass.name)) # default name
          subclass.instance_variable_set("@field_names", []) # array of scalars names (symbols)
          subclass.instance_variable_set("@subobjects", {}) # key = name (symbol), value = class
          subclass.instance_variable_set("@arrays", {})     # key = name (symbol), value = class
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
              define_method("#{field}?", lambda {
                              val = instance_variable_get(ivar_name)
                              val != nil && (!val.kind_of?(String) || val != '')
                            })
              @field_names << field
            end
          }
        end
        alias_method :fields, :field

        def field_names; @field_names; end
        def subobjects; @subobjects; end
        def arrays; @arrays; end
        def mongo_ivar_names; @field_names + @subobjects.keys + @arrays.keys; end

        # Tells Mongo about a subobject.
        #
        # Options:
        # :class_name:: Name of the class of the subobject.
        def has_one(name, options={})
          name = name.to_sym
          unless @subobjects[name]
            ivar_name = "@" + name.to_s
            define_method(name, lambda { instance_variable_get(ivar_name) })
            define_method("#{name}=".to_sym, lambda { |val| instance_variable_set(ivar_name, val) })
            define_method("#{name}?", lambda {
                            val = instance_variable_get(ivar_name)
                            val != nil && (!val.kind_of?(String) || val != '')
                          })
            klass_name = options[:class_name] || field_name_to_class_name(name)
            @subobjects[name] = Kernel.const_get(klass_name)
          end
        end

        # Tells Mongo about an array of subobjects.
        #
        # Options:
        # :class_name:: Name of the class of the subobject.
        def has_many(name, options={})
          name = name.to_sym
          unless @arrays[name]
            ivar_name = "@" + name.to_s
            define_method(name, lambda { instance_variable_get(ivar_name) })
            define_method("#{name}=".to_sym, lambda { |val| instance_variable_set(ivar_name, val) })
            define_method("#{name}?", lambda { !instance_variable_get(ivar_name).empty? })
            klass_name = options[:class_name] || field_name_to_class_name(name)
            @arrays[name] = Kernel.const_get(klass_name)
          end
        end

        # The collection object.
        def coll
          @coll ||= $db[@coll_name.to_s]
        end

        # Find one or more database objects.
        #
        # * Find by id (a single id or an array of ids) returns one record or a Cursor.
        #
        # * Find :first returns the first record that matches the options used.
        #
        # * Find :all records; returns a Cursor that can iterate over raw
        #   records.
        #
        # * Find all records if there are no conditions.
        #
        # Options:
        #
        # :conditions:: Hash where key = field name and value = field value.
        #               Value may be a simple value like a string, number, or
        #               regular expression.
        #
        # :select:: Single field name or list of field names. If not
        #           specified, all fields are returned. Names may be symbols
        #           or strings. The database always returns _id field.
        #
        # :order:: If a symbol, orders by that field in ascending order. If a
        #          string like "field1 asc, field2 desc, field3", then sorts
        #          those fields in the specified order (default is ascending).
        #          If an array, each element is either a field name or symbol
        #          (which will be sorted in ascending order) or a hash where
        #          key = field and value = 'asc' or 'desc' (case-insensitive),
        #          1 or -1, or if any other value then true == 1 and false/nil
        #          == -1.
        #
        # :limit:: Maximum number of records to return.
        #
        # :offset:: Number of records to skip.
        #
        # Examples for find by id:
        #   Person.find("48e5307114f4abdf00dfeb86")     # returns the object for this ID
        #   Person.find(["a_hex_id", "another_hex_id"]) # returns a Cursor over these two objects
        #   Person.find(["a_hex_id"])                   # returns a Cursor over the object with this ID
        #   Person.find("a_hex_id", :conditions => {admin: 1}, :order => "created_on DESC")
        #
        # Examples for find first:
        #   Person.find(:first) # returns the first object in the collection
        #   Person.find(:first, :conditions => {user_name: user_name})
        #   Person.find(:first, :order => "created_on DESC", :offset => 5)
        #   Person.find(:first, :order => {:created_on => -1}, :offset => 5) # same as previous example
        #
        # Examples for find all:
        #   Person.find(:all) # returns a Cursor over all objects in the collection
        #   Person.find(:all, :conditions => {category: category}, :limit => 50)
        #   Person.find(:all, :offset => 10, :limit => 10)
        #   Person.find(:all, :select => :name) # Only returns name (and _id) fields
        #
        # As a side note, the :order, :limit, and :offset options are passed
        # on to the Cursor (after the :order option is rewritten to be a
        # hash). So
        #   Person.find(:all, :offset => 10, :limit => 10, :order => :created_on)
        # is the same as
        #   Person.find(:all).skip(10).limit(10).sort({:created_on => 1})
        def find(*args)
          options = extract_options_from_args!(args)
          case args.first
          when :first
            find_initial(options)
          when :all
            find_every(options)
          else
            find_from_ids(args, options)
          end
        rescue => ex
          nil
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

        # Handles find_* methods such as find_by_name, find_all_by_shoe_size,
        # and find_or_create_by_name.
        def method_missing(sym, *args)
          if match = /^find_(all_by|by)_([_a-zA-Z]\w*)$/.match(sym.to_s)
            find_how_many = ($1 == 'all_by') ? :all : :first
            field_names = $2.split(/_and_/)
            super unless all_fields_exist?(field_names)
            search = search_from_names_and_values(field_names, args)
            self.find(find_how_many, {:conditions => search}, *args[field_names.length..-1])
          elsif match = /^find_or_(initialize|create)_by_([_a-zA-Z]\w*)$/.match(sym.to_s)
            create = $1 == 'create'
            field_names = $2.split(/_and_/)
            super unless all_fields_exist?(field_names)
            search = search_from_names_and_values(field_names, args)
            row = self.find(:first, {:conditions => search})
            return self.new(row) if row # found
            obj = self.new(search.merge(args[field_names.length] || {})) # new object using search and remainder of args
            obj.save if create
            obj
          else
            super
          end
        end

        private

        def extract_options_from_args!(args)
          args.last.is_a?(Hash) ? args.pop : {}
        end

        def find_initial(options)
          criteria = criteria_from(options[:conditions])
          fields = fields_from(options[:select])
          row = coll.findOne(criteria, fields)
          (row.nil? || row['_id'] == nil) ? nil : self.new(row)
        end

        def find_every(options)
          criteria = criteria_from(options[:conditions])
          fields = fields_from(options[:select])
          db_cursor = coll.find(criteria, fields)
          db_cursor.limit(options[:limit].to_i) if options[:limit]
          db_cursor.skip(options[:offset].to_i) if options[:offset]
          sort_by = sort_by_from(options[:order]) if options[:order]
          db_cursor.sort(sort_by) if sort_by
          Cursor.new(db_cursor, self)
        end

        def find_from_ids(ids, options)
          ids = ids.to_a.flatten.compact.uniq
          criteria = criteria_from(options[:conditions]) || {}
          criteria[:_id] = ids_clause(ids)
          fields = fields_from(options[:select])
          db_cursor = coll.find(criteria, fields)
          sort_by = sort_by_from(options[:order]) if options[:order]
          db_cursor.sort(sort_by) if sort_by
          cursor = Cursor.new(db_cursor, self)
          ids.length == 1 ? self.new(cursor[0]) : cursor
        end

        def ids_clause(ids)
          ids.length == 1 ? ids[0] : {:$in => ids.collect{|id| mongo_id(id)}}
        end

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

        # Given a "SymbolOrStringLikeThis", return the string "symbol_or_string_like_this".
        def class_name_to_field_name(name)
          name.gsub(/([A-Z])/, '_\1').downcase.sub(/^_/, '')
        end

        # Given a "symbol_or_string_like_this", return the string "SymbolOrStringLikeThis".
        def field_name_to_class_name(name)
          name = name.to_s.dup.gsub(/_([a-z])/) {$1.upcase}
          name[0,1] = name[0,1].upcase
          name
        end

        protected

        # Turns array, string, or hash conditions into something useable by Mongo.
        #   ["name='%s' and group_id='%s'", "foo'bar", 4]  returns  {:name => 'foo''bar', :group_id => 4}
        #   "name='foo''bar' and group_id='4'" returns {:name => 'foo''bar', :group_id => 4}
        #   { :name => "foo'bar", :group_id => 4 }  returns the hash, modified for Mongo
        def criteria_from(condition) # :nodoc:
          case condition
          when Array
            criteria_from_array(condition)
          when String
            criteria_from_string(condition)
          when Hash
            criteria_from_hash(condition)
          else
            {}
          end
        end

        # Substitutes values at the end of an array into the string at its
        # start, sanitizing strings in the values. Then passes the string on
        # to criteria_from_string.
        def criteria_from_array(condition) # :nodoc:
          str, *values = condition
          sql = if values.first.kind_of?(Hash) and str =~ /:\w+/
                  replace_named_bind_variables(str, values.first)
                elsif str.include?('?')
                  replace_bind_variables(str, values)
                else
                  str % values.collect {|value| quote(value) }
                end
          criteria_from_string(sql)
        end

        # Turns a string into a Mongo search condition hash.
        def criteria_from_string(sql) # :nodoc:
          $SQL.parseWhere(sql)
        end

        # Turns a hash that ActiveRecord would expect into one for Mongo.
        def criteria_from_hash(condition) # :nodoc:
          h = {}
          condition.each { |k,v|
            h[k] = case v
                   when Array
                     {:$in => k == 'id' || k == '_id' ? v.collect{ |val| mongo_id(val)} : v} # if id, can't pass in string; must be ObjectId
                   when Range
                     {:$gte => v.first, :$lte => v.last}
                   else
                     v
                   end
          }
          h
        end

        def replace_named_bind_variables(str, h) # :nodoc:
          str.gsub(/:(\w+)/) do
            match = $1.to_sym
            if h.include?(match)
              quote(h[match])
            else
              raise "missing value for :#{match} in #{str}" # TODO this gets swallowed in find()
            end
          end
        end

        def replace_bind_variables(str, values) # :nodoc:
          raise "parameter count does not match value count" unless str.count('?') == values.length
          bound = values.dup
          str.gsub('?') { quote(bound.shift) }
        end

        # Returns value quoted if appropriate (if it's a string).
        def quote(val) # :nodoc:
          return val unless val.is_a?(String)
          return "'#{val.gsub(/\'/, "\\\\'")}'" # " <= for Emacs font-lock
        end

        def mongo_id(val) # :nodoc:
          return ObjectId(val.to_s)
        end

        def fields_from(a) # :nodoc:
          return nil unless a
          a = [a] unless a.kind_of?(Array)
          return nil unless a.length > 0
          fields = {}
          a.each { |k| fields[k.to_sym] = 1 }
          fields
        end

        def sort_by_from(option) # :nodoc:
          return nil unless option
          sort_by = {}
          case option
          when Symbol           # Single value
            sort_by[option.to_sym] = 1
          when String
            # TODO order these by building an array of hashes
            fields = option.split(',')
            fields.each {|f|
              name, order = f.split
              order ||= 'asc'
              sort_by[name.to_sym] = sort_value_from_arg(order)
            }
          when Array            # Array of field names; assume ascending sort
            # TODO order these by building an array of hashes
            option.each {|o| sort_by[o.to_sym] = 1}
          else                  # Hash (order of sorts is not guaranteed)
            option.each {|k,v| sort_by[k.to_sym] = sort_value_from_arg(v) }
          end
          return nil unless sort_by.keys.length > 0
          sort_by
        end

        # Turns "asc" into 1, "desc" into -1, and other values into 1 or -1.
        def sort_value_from_arg(arg) # :nodoc:
          case arg
          when /^asc/i
            arg = 1
          when /^desc/i
            arg = -1
          when Number
            arg.to_i == 1 ? 1 : -1
          else
            arg ? 1 : -1
          end
        end

      end

      public

      # Initialize a new object with either a hash of values or a row returned
      # from the database.
      def initialize(row={})
        case row
        when Hash
          row.each { |k, val|
            k = '_id' if k == 'id' # Rails helper
            val = nil if val == '' && k == '_id'
            init_ivar("@#{k}", val)
          }
        else
          row.instance_variables.each { |iv|
            init_ivar(iv, row.instance_variable_get(iv))
          }
        end
        # Default values for remaining fields
        (self.class.field_names + self.class.subobjects.keys).each { |iv|
          iv = "@#{iv}"
          instance_variable_set(iv, nil) unless instance_variable_defined?(iv)
        }
        self.class.arrays.keys.each { |iv|
          iv = "@#{iv}"
          instance_variable_set(iv, []) unless instance_variable_defined?(iv)
        }
        yield self if block_given?
      end

      def id=(val); @_id = (val == '' ? nil : val); end
      # You'll get a deprecation warning if you call this outside of Rails.
      def id; @_id ? @_id.to_s : nil; end

      # Rails convenience method.
      def to_param
        @_id.to_s
      end

      # Saves and returns self.
      def save
        row = self.class.coll.save(to_mongo_value)
        if @_id == nil
          @_id = row._id
        elsif row._id != @_id
          raise "Error: after save, database id changed"
        end
        self
      end

      def new_record?
        @_id == nil
      end

      def to_mongo_value
        h = {}
        self.class.mongo_ivar_names.each {|iv| h[iv] = instance_variable_get("@#{iv}").to_mongo_value }
        h
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

      private

      # Initialize ivar. +name+ must include the leading '@'.
      def init_ivar(ivar_name, val)
            sym = ivar_name[1..-1].to_sym
            if self.class.subobjects.keys.include?(sym)
              instance_variable_set(ivar_name, self.class.subobjects[sym].new(val))
            elsif self.class.arrays.keys.include?(sym)
              klazz = self.class.arrays[sym]
              val = [val] unless val.kind_of?(Array)
              instance_variable_set(ivar_name, val.collect {|v| v.kind_of?(XGen::Mongo::Base) ? v : klazz.new(v)})
            else
              instance_variable_set(ivar_name, val)
            end
      end

    end

  end

end
