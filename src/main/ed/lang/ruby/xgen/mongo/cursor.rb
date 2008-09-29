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
  end
end
