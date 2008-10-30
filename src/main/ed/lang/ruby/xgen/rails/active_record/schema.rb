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

  # Only used to read db/schema.rb, so there will not be statements like
  # drop_table or remove_column.
  class Schema

    cattr_reader :collection_info
    @@collection_info = {}

    class << self

      def define(info={}, &block)
        self.verbose = false
        @collection_info = {}
        instance_eval(&block)
      end

      def create_table(name, options)
        t = ActiveRecord::ConnectionAdapters::TableDefinition.new(self)
        t.primary_key('_id')
        @@collection_info[name] = t
        yield t
      end

      def add_index(table_name, column_name, options = {})
        $db[table_name].ensureIndex(column_name.to_s => 1)
      end
    end

  end
end
