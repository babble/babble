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

    # A Mongo cursor is like Schrodenger's cat: it is neither an array nor an
    # enumerable collection until you use it. It can not be both. Once you
    # reference it as an array (by retrieving a record via index or asking for
    # the length or count), you can't iterate over the contents using +each+.
    # Likewise, once you start iterating over the contents using +each+ you
    # can't ask for the count of the number of records.
    #
    # Example:
    #   Person.find(:all).sort({:created_on => 1}).each { |p| puts p.to_s }
    #   n = Thing.find(:all).count()
    #
    # The sort, limit, and skip methods must be called before resolving the
    # quantum state of a cursor.
    #
    # See Base#find for more infromation.
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

      # Sort, limit, and skip methods that return self (the cursor) instead of
      # whatever those methods return.
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
