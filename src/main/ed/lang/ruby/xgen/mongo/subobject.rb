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

require 'xgen/mongo/base'

module XGen

  module Mongo

    # A XGen::Mongo::Subobject is an XGen::Mongo::Base subclass that disallows
    # many operations. Subobjects are those that are contained within and
    # saved with some other object.
    #
    # Using XGen::Mongo::Subobject is completely optional.
    #
    # As an example, say a Student object contains an Address. You might want
    # to make Address a subclass of Subobject so that you don't accidentally
    # try to save an address to a collection by itself.
    class Subobject < Base

      class << self # Class methods

        # Subobjects ignore the collection name.
        def collection_name(coll_name)
        end

        # Disallow find.
        def find(*args)
          complain("found")
        end

        # Disallow count.
        def count(*args)
          complain("counted")
        end

        # Disallow delete.
        def delete(id)
          complain("deleted")
        end
        alias_method :remove, :delete

        # Disallow destroy.
        def destroy(id)
          complain("destroyed")
        end

        # Disallow destroy_all.
        def destroy_all(conditions = nil)
          complain("destroyed")
        end

        # Disallow delete_all.
        def delete_all(conditions=nil)
          complain("deleted")
        end

        private

        def complain(cant_do_this)
          raise "Subobjects can't be #{cant_do_this} by themselves. Use a subobject query."
        end

      end                       # End of class methods

      public

      # Subobjects do not have their own ids.
      def id=(val); raise "Subobjects don't have ids"; end

      # Subobjects do not have their own ids.
      # You'll get a deprecation warning if you call this outside of Rails.
      def id; raise "Subobjects don't have ids"; end

      # to_param normally returns the id of an object. Since subobjects don't
      # have ids, this is disallowed.
      def to_param; raise "Subobjects don't have ids"; end

      # Disallow new_record?
      def new_record?; raise "Subobjects don't have ids"; end

      # Disallow create.
      def create
        self.class.complain("created")
      end

      # Disallow udpate.
      def update
        self.class.complain("updated")
      end

      # Disallow delete and remove.
      def delete
        self.class.complain("deleted")
      end
      alias_method :remove, :delete

    end

  end

end
