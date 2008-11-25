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

module ActiveRecord

  module ConnectionAdapters

    class ColumnDefinition
      def sql_type; type; end
      def to_sql; ''; end
      def add_column_options!(sql, options); ''; end
    end

    class TableDefinition
      def native; {}; end
    end

    class MongoPseudoConnection

      attr_reader :db

      def initialize(db)
        @runtime = 0
        @db = db
      end

      def method_missing(sym, *args)
        $stderr.puts "#{sym}(#{args.inspect}) sent to conn" # DEBUG
      end

      def quote(val, column=nil)
        return val unless val.is_a?(String)
        "'#{val.gsub(/\'/, "\\\\'")}'" # " <= for Emacs font-lock
      end

      def quote_table_name(str)
        str.to_s
      end

      def quote_column_name(str)
        str.to_s
      end

      def reset_runtime
        rt, @runtime = @runtime, 0
        rt
      end

      def table_alias_for(table_name)
        table_name.gsub(/\./, '_')
      end

      def supports_count_distinct?
        false
      end

      def transaction(start_db_transaction=true)
        # Transactions are not yet supported by Mongo.
        yield
      end
    end
  end
end
