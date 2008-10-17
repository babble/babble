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

    # A destination for Ruby's built-in Logger class. It writes log messages
    # to a Mongo database collection. Each item in the collection consists of
    # two fields (besides the _id): +time+ and +msg+. +time+ is automatically
    # generated when +write+ is called.
    #
    # The collection is capped, which means after the limit is reached old
    # records are deleted when new ones are inserted. See the Mongo
    # documentation for details.
    #
    # The default
    #
    # Example:
    #
    #   logger = Logger.new(XGen::Mongo::LogDevice('my_log_name'))
    module LogDevice

      DEFAULT_CAP_SIZE = (1024 * 1024)

      # +name+ is the name of the Mongo database collection that will hold all
      # log messages. +cap_size+ is the max size of the collection. If it is
      # nil, not a number, or negative then +DEFAULT_CAP_SIZE+ is used.
      def inititalize(name, cap_size=nil)
        @collection_name = name

        cap_size ||= DEFAULT_CAP_SIZE
        cap_size = cap_size.to_i
        cap_size = DEFAULT_CAP_SIZE if cap_size <= 0
        # It's OK to call create_collection if the collection already exists
        $db.create_collection(@collection_name, {:size => 1_000_000, :capped => true})
      end

      def write(str)
        $db[@collection_name].save({:time => Time.now.to_i, :msg => str})
      end

      def close
      end

    end
  end
end
