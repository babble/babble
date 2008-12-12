#--
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
#++

require 'xgen/sql'
require 'xgen/oid'
require 'xgen/mongo/cursor'
require 'xgen/mongo/convert'

class JSObject
  # Convert this JSObject to a Hash usable by ActiveRecord for saving to Mongo.
  def to_ar_hash
    # Remove keys with underscores, except for _id, before passing on to the model
    to_hash.delete_if { |k,v| k.to_s[0,1] == '_' && k.to_s != '_id' }
  end
end

class XGen::Mongo::Cursor

  # Overrides the original version to call to_ar_hash on each row, and then
  # calling instantiate on the model class.
  def each
    @cursor.forEach { |row|
      yield @model_class.send(:instantiate, row.to_ar_hash)
    }
  end

  # Overrides the original version to call to_ar_hash on the row, and then
  # calling instantiate on the model class.
  def [](index)
    @model_class.send(:instantiate, @cursor[index].to_ar_hash)
  end
end

module ActiveRecord

  # We override a number of ActiveRecord::Base methods to make it work with Mongo.
  class Base

    @@mongo_connection = nil

    class << self               # Class methods

      # Return information about the schema defined in the file named by
      # ENV['SCHEMA'] or, if that is not defined, db/schema.rb.
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
        connection.db[table_name]
      end

      # ================ relational database connection handling ================

      # Return the database connection. The default value is an
      # ActiveRecord::ConnectionAdapters::MongoPseudoConnection that uses
      # <code>$db</code>.
      def connection
        @@mongo_connection ||= ActiveRecord::ConnectionAdapters::MongoPseudoConnection.new($db)
      end

      # Set the database connection. If the connection is set to +nil+, then
      # an ActiveRecord::ConnectionAdapters::MongoPseudoConnection that uses
      # <code>$db</code> will be used.
      def connection=(db)
        @@mongo_connection = ActiveRecord::ConnectionAdapters::MongoPseudoConnection.new(db || $db)
      end

      # Does nothing.
      def establish_connection(spec = nil); end

      # Return the connection.
      def retrieve_connection; connection; end

      # Always returns +true+.
      def connected?; true; end

      # Does nothing.
      def remove_connection; end

      # ================

      # Works like find(:all), but requires a complete SQL string. Examples:
      #   Post.find_by_sql "SELECT p.*, c.author FROM posts p, comments c WHERE p.id = c.post_id"
      #   Post.find_by_sql ["SELECT * FROM posts WHERE author = ? AND created > ?", author_id, start_date]
      #
      # Note: this method is not implemented. It will raise "not implemented".
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
      def delete_all(conditions = "")
        collection.remove(XGen::SQL::Parser.parse_where(conditions, true) || {})
      end

      # Count operates using two different approaches. 
      #
      # * Count all: By not passing any parameters to count, it will return a count of all the rows for the model.
      # * Count using options will find the row count matched by the options used.
      #
      # The last approach, count using options, accepts an option hash as the only parameter. The options are:
      #
      # * <tt>:conditions</tt> - An SQL fragment like "administrator = 1" or [ "user_name = ?", username ]. See conditions in the intro.
      # * <tt>:joins</tt> - An SQL fragment for additional joins like "LEFT JOIN comments ON comments.post_id = id". (Rarely needed).
      #   The records will be returned read-only since they will have attributes that do not correspond to the table's columns.
      # * <tt>:include</tt> - Named associations that should be loaded alongside using LEFT OUTER JOINs. The symbols named refer
      #   to already defined associations. When using named associations count returns the number DISTINCT items for the model you're counting.
      #   See eager loading under Associations.
      # * <tt>:order</tt> - An SQL fragment like "created_at DESC, name" (really only used with GROUP BY calculations).
      # * <tt>:group</tt> - An attribute name by which the result should be grouped. Uses the GROUP BY SQL-clause.
      # * <tt>:select</tt> - By default, this is * as in SELECT * FROM, but can be changed if you for example want to do a join, but not
      #   include the joined columns.
      # * <tt>:distinct</tt> - Set this to true to make this a distinct calculation, such as SELECT COUNT(DISTINCT posts.id) ...
      #
      # Examples for counting all:
      #   Person.count         # returns the total count of all people
      #
      # Examples for count by +conditions+ and +joins+ (this has been deprecated):
      #   Person.count("age > 26")  # returns the number of people older than 26
      #   Person.find("age > 26 AND job.salary > 60000", "LEFT JOIN jobs on jobs.person_id = person.id") # returns the total number of rows matching the conditions and joins fetched by SELECT COUNT(*).
      #
      # Examples for count with options:
      #   Person.count(:conditions => "age > 26")
      #   Person.count(:conditions => "age > 26 AND job.salary > 60000", :include => :job) # because of the named association, it finds the DISTINCT count using LEFT OUTER JOIN.
      #   Person.count(:conditions => "age > 26 AND job.salary > 60000", :joins => "LEFT JOIN jobs on jobs.person_id = person.id") # finds the number of rows matching the conditions and joins. 
      #   Person.count('id', :conditions => "age > 26") # Performs a COUNT(id)
      #   Person.count(:all, :conditions => "age > 26") # Performs a COUNT(*) (:all is an alias for '*')
      #
      # Note: Person.count(:all) will not work because it will use :all as the condition.  Use Person.count instead.
      def count(*args)
        # Ignore first arg if it is not a Hash
        a = self.respond_to?(:construct_count_options_from_legacy_args) ?
            construct_count_options_from_legacy_args(*args) :
            construct_count_options_from_args(*args)
        column_name, options = *a
        find_every(options).count()
      end

      # Returns the result of an SQL statement that should only include a COUNT(*) in the SELECT part.
      #   Product.count_by_sql "SELECT COUNT(*) FROM sales s, customers c WHERE s.customer_id = c.id"
      def count_by_sql(sql)
        count(sql)
        sql =~ /.*\bwhere\b(.*)/i
        count(:conditions => $1 || "")
      end

      # Increments the specified counter by one. So <tt>DiscussionBoard.increment_counter("post_count",
      # discussion_board_id)</tt> would increment the "post_count" counter on the board responding to discussion_board_id.
      # This is used for caching aggregate values, so that they don't need to be computed every time. Especially important
      # for looping over a collection where each element require a number of aggregate values. Like the DiscussionBoard
      # that needs to list both the number of posts and comments.
      def increment_counter(counter_name, id)
        rec = collection.findOne({:_id => id})
        rec[counter_name] += 1
        collection.save(rec)
      end

      # Works like increment_counter, but decrements instead.
      def decrement_counter(counter_name, id)
        rec = collection.findOne({:_id => id})
        rec[counter_name] -= 1
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
        row = collection.findOne(criteria, fields)
        (row.nil? || row['_id'] == nil) ? nil : self.send(:instantiate, row.to_ar_hash)
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
        ids.length == 1 ? cursor[0] : cursor
      end

      def ids_clause(ids)
        ids.length == 1 ? ids[0] : {:$in => ids.collect{|id| id.to_oid}}
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
        self.class.collection.remove({:_id => self.id})
      end
      freeze
    end

    # Convert this object to a Mongo value suitable for saving to the
    # database.
    def to_mongo_value
      h = {}
      self.class.column_names.each {|iv|
        val = read_attribute(iv)
        h[iv] = val == nil ? nil : val.to_mongo_value
      }
      h
    end

    private

    # Updates the associated record with values matching those of the instance attributes.
    # Returns the number of affected rows.
    def update_without_callbacks
      self.class.collection.save(to_mongo_value)
    end

    # Creates a record with values matching those of the instance attributes
    # and returns its id.
    def create_without_callbacks
      row = self.class.collection.save(to_mongo_value)
      self.id = row._id
      @new_record = false
      id
    end

  end

end
