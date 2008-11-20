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

require 'xgen/mongo/base'

module XGen

  module Mongo

    class Subobject < Base

      class << self # Class methods

        # Subobjects ignore the collection name.
        def collection_name(coll_name)
        end

        def find(*args)
          complain("found")
        end

        # Returns the number of matching records.
        def count(*args)
          complain("counted")
        end

        def delete(id)
          complain("deleted")
        end
        alias_method :remove, :delete

        def destroy(id)
          complain("destroyed")
        end

        def destroy_all(conditions = nil)
          complain("destroyed")
        end

        def delete_all(conditions=nil)
          complain("deleted")
        end

        private

        def complain(cant_do_this)
          raise "Subobjects can't be #{cant_do_this} by themselves. Use a subobject query."
        end

      end                       # End of class methods

      public

      def id=(val); raise "Subobjects don't have ids"; end
      # You'll get a deprecation warning if you call this outside of Rails.
      def id; raise "Subobjects don't have ids"; end

      def to_param; raise "Subobjects don't have ids"; end

      def new_record?; raise "Subobjects don't have ids"; end

      def create
        self.class.complain("saved")
      end

      def update
        self.class.complain("saved")
      end

      # Removes self from the database and sets @_id to nil. If self has no
      # @_id, does nothing.
      def delete
        self.class.complain("deleted")
      end
      alias_method :remove, :delete

    end

  end

end
