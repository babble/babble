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

require 'xgen/sql'
require 'xgen/mongo/cursor'

class JSObject
  def to_ar_hash
    # Remove keys with underscores, except for _id, before passing on to the model
    to_hash.delete_if { |k,v| k.to_s[0,1] == '_' && k.to_s != '_id' }
  end
end

class XGen::Mongo::Cursor
  def each
    @cursor.forEach { |row|
      yield @model_class.new(row.to_ar_hash)
    }
  end
end

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

module ActiveRecord

  class Base

    @@mongo_connection = nil

    class << self               # Class methods

      # Return information about the schema defined in db/schema.rb.
      def collection_info
        unless defined? @@collection_info
          file = ENV['SCHEMA'] || 'db/schema.rb'
          load(file)
          @@collection_info = ActiveRecord::Schema.collection_info
        end
        @@collection_info
      end

      # Return the Mongo collection for this class.
      def collection
        $db[table_name]
      end

      # ================ relational database connection handling ================

      # Return an object that does nothing, no matter what is passed to it
      def connection
        @@mongo_connection ||= ActiveRecord::ConnectionAdapters::MongoPseudoConnection.new
      end

      def establish_connection(spec = nil); end

      def retrieve_connection; connection end

      def connected?; true; end

      def remove_connection; end

      def connection=(spec); end

      # ================

      # Works like find(:all), but requires a complete SQL string. Examples:
      #   Post.find_by_sql "SELECT p.*, c.author FROM posts p, comments c WHERE p.id = c.post_id"
      #   Post.find_by_sql ["SELECT * FROM posts WHERE author = ? AND created > ?", author_id, start_date]
      def find_by_sql(sql)
        raise "not implemented"
      end

      # Deletes the record with the given +id+ without instantiating an object first. If an array of ids is provided, all of them
      # are deleted.
      def delete(id)
        collection.remove(id)
      end

      # Updates all records with the SET-part of an SQL update statement in +updates+ and returns an integer with the number of rows updated.
      # A subset of the records can be selected by specifying +conditions+. Example:
      #   Billing.update_all "category = 'authorized', approved = 1", "author = 'David'"
      def update_all(updates, conditions = nil)
        # TODO
        raise "not implemented"
#         sql  = "UPDATE #{table_name} SET #{sanitize_sql(updates)} "
#         add_conditions!(sql, conditions, scope(:find))
#         connection.update(sql, "#{name} Update")
      end

      # Deletes all the records that match the +condition+ without instantiating the objects first (and hence not
      # calling the destroy method). Example:
      #   Post.delete_all "person_id = 5 AND (category = 'Something' OR category = 'Else')"
      def delete_all(conditions = {})
        collection.remove(XGen::SQL::Parser.parse_where(conditions, true) || {})
      end

      # Returns the result of an SQL statement that should only include a COUNT(*) in the SELECT part.
      #   Product.count_by_sql "SELECT COUNT(*) FROM sales s, customers c WHERE s.customer_id = c.id"
      def count_by_sql(sql)
        sql =~ /.*\bwhere\b(.*)/i
        collection.find(XGEN::SQL::Parser.parse_where(conditions, true) || {}).count()
      end

      # Increments the specified counter by one. So <tt>DiscussionBoard.increment_counter("post_count",
      # discussion_board_id)</tt> would increment the "post_count" counter on the board responding to discussion_board_id.
      # This is used for caching aggregate values, so that they don't need to be computed every time. Especially important
      # for looping over a collection where each element require a number of aggregate values. Like the DiscussionBoard
      # that needs to list both the number of posts and comments.
      def increment_counter(counter_name, id)
        rec = collection.find(id)
        rec.instance_variable_set("@#{counter_name}", rec.instance_variable_get("@#{counter_name}") + 1)
        collection.save(rec)
      end

      # Works like increment_counter, but decrements instead.
      def decrement_counter(counter_name, id)
        rec = collection.find(id)
        rec.instance_variable_set("@#{counter_name}", rec.instance_variable_get("@#{counter_name}") - 1)
        collection.save(rec)
      end

      # Defines the primary key field -- can be overridden in subclasses. Overwriting will negate any effect of the
      # primary_key_prefix_type setting, though.
      def primary_key
        '_id'
      end

      def reset_sequence_name #:nodoc:
        default = nil
        set_sequence_name(default)
        default
      end

      # Indicates whether the table associated with this class exists
      def table_exists?
        true
      end

      # Returns an array of column objects for the table associated with this class.
      def columns
        unless @columns
          @columns = collection_info[table_name].columns.collect { |col_def|
            col = ActiveRecord::ConnectionAdapters::Column.new(col_def.name, col_def.default, col_def.sql_type, col_def.null)
            col.primary = col.name == primary_key
            col
          }
        end
        @columns
      end

      # Used to sanitize objects before they're used in an SELECT SQL-statement.
      def sanitize(object) #:nodoc:
        quote_value(object)
      end

      private

      def find_initial(options)
        # Note: must use merge! because JSObject (returned by
        # criteria_from_string) does not have an allocator (therefore dup()
        # is not allowed).
        criteria = criteria_from(options[:conditions]).merge!(where_func(options[:where]))
        fields = fields_from(options[:select])
        row = collection.find_one(criteria, fields)
        (row.nil? || row['_id'] == nil) ? nil : self.new(row.to_ar_hash)
      end

      def find_every(options)
        # Note: must use merge! because JSObject (returned by
        # criteria_from_string) does not have an allocator (therefore dup()
        # is not allowed).
        criteria = criteria_from(options[:conditions]).merge!(where_func(options[:where]))
        fields = fields_from(options[:select])
        db_cursor = collection.find(criteria, fields)
        db_cursor.limit(options[:limit].to_i) if options[:limit]
        db_cursor.skip(options[:offset].to_i) if options[:offset]
        sort_by = sort_by_from(options[:order]) if options[:order]
        db_cursor.sort(sort_by) if sort_by
        XGen::Mongo::Cursor.new(db_cursor, self)
      end

      def find_from_ids(ids, options)
        ids = ids.to_a.flatten.compact.uniq
        # Note: must use merge! because JSObject (returned by
        # criteria_from_string) does not have an allocator (therefore dup()
        # is not allowed).
        criteria = criteria_from(options[:conditions]).merge!(where_func(options[:where]))
        criteria[:_id] = ids_clause(ids)
        fields = fields_from(options[:select])
        db_cursor = collection.find(criteria, fields)
        sort_by = sort_by_from(options[:order]) if options[:order]
        db_cursor.sort(sort_by) if sort_by
        cursor = XGen::Mongo::Cursor.new(db_cursor, self)
        ids.length == 1 ? self.new(cursor[0].to_ar_hash) : cursor
      end

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
                str % values.collect {|value| quote_value(value) }
              end
        criteria_from_string(sql)
      end

      # Turns a string into a Mongo search condition hash.
      def criteria_from_string(sql) # :nodoc:
        XGen::SQL::Parser.parse_where(sql, true)
      end

      # Turns a hash that ActiveRecord would expect into one for Mongo.
      def criteria_from_hash(condition) # :nodoc:
        h = {}
        condition.each { |k,v|
          h[k] = case v
                 when Array
                   {:$in => k == 'id' || k == '_id' ? v.collect{ |val| val.to_oid} : v} # if id, can't pass in string; must be ObjectId
                 when Range
                   {:$gte => v.first, :$lte => v.last}
                 else
                   v
                 end
        }
        h
      end

      # Returns a hash useable by Mongo for applying +func+ on the db
      # server. +func+ must be a JavaScript function in a string.
      def where_func(func)    # :nodoc:
        func ? {:$where => func} : {}
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
        sort_by = []
        case option
        when Symbol           # Single value
          sort_by << {option.to_sym => 1}
        when String
          # TODO order these by building an array of hashes
          fields = option.split(',')
          fields.each {|f|
            name, order = f.split
            order ||= 'asc'
            sort_by << {name.to_sym => sort_value_from_arg(order)}
          }
        when Array            # Array of field names; assume ascending sort
          # TODO order these by building an array of hashes
          sort_by = option.collect {|o| {o.to_sym => 1}}
        else                  # Hash (order of sorts is not guaranteed)
          sort_by = option.collect {|k, v| {k.to_sym => sort_value_from_arg(v)}}
        end
        return nil unless sort_by.length > 0
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
          arg.to_i >= 0 ? 1 : -1
        else
          arg ? 1 : -1
        end
      end

      # Default implementation doesn't work for "_id".
      def all_attributes_exists?(attribute_names)
        attribute_names.collect! {|n| n == 'id' ? '_id' : n}
        attribute_names.all? { |name| column_methods_hash.include?(name.to_sym) }
      end

    end                         # End of class methods

    public

    # Deletes the record in the database and freezes this instance to reflect that no changes should
    # be made (since they can't be persisted).
    def destroy
      unless new_record?
        self.class.collection.remove({:id => self.id})
      end
      freeze
    end

    def to_mongo_value
      h = {}
      self.class.column_names.each {|iv|
        val = read_attribute(iv)
        h[iv] = val == nil ? nil : val.to_mongo_value
      }
      h
    end

    private

    def create_or_update
      raise ReadOnlyRecord if readonly?
      result = new_record? ? create : update
      result != false
    end

    # Saves and returns self.
    def save_data
      row = self.class.collection.save(to_mongo_value)
      self.id = row._id if new_record?
      self
    end

    # Updates the associated record with values matching those of the instance attributes.
    # Returns the number of affected rows.
    def update
      save_data
    end

    # Creates a record with values matching those of the instance attributes
    # and returns its id.
    def create
      save_data
      @new_record = false
      self.id
    end


    # Quote strings appropriately for SQL statements.
    def quote_value(value, column = nil)
      self.class.quote(value, column)
    end

    # Deprecated, use quote_value
    def quote(value, column = nil)
      self.class.quote(value, column)
    end
    deprecate :quote => :quote_value

  end

end
