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
    # Example:
    #
    #   logger = Logger.new(XGen::Mongo::LogDevice('my_log_name'))
    class LogDevice

      DEFAULT_CAP_SIZE = (10 * 1024 * 1024)

      # +name+ is the name of the Mongo database collection that will hold all
      # log messages. +options+ is a hash that may have the following entries:
      #
      # :size:: Optional. The max size of the collection, in bytes. If it is
      #         nil or negative then +DEFAULT_CAP_SIZE+ is used.
      #
      # :max:: Optional. Specifies the maximum number of log records, after
      #        which the oldest items are deleted as new ones are inserted.
      #
      # Note: a non-nil :max_records requires a :size value. The collection
      # will never grow above :size. If you leave :size nil then it will be
      # +DEFAULT_CAP_SIZE+.
      #
      # Note: once a capped collection has been created, you can't redefine
      # the size or max falues for that collection. To do so, you must drop
      # and recreate (or let a LogDevice object recreate) the collection.
      def initialize(name, options = {})
        @collection_name = name
        options[:size] ||= DEFAULT_CAP_SIZE
        options[:size] = DEFAULT_CAP_SIZE if options[:size] <= 0
        options[:capped] = true

        # It's OK to call createCollection if the collection already exists.
        # Size and max won't change, though.
        #
        # Note we can't use the name "create_collection" because a DB JSObject
        # does not have normal keys and returns collection objects as the
        # value of all unknown names.
        $db.createCollection(@collection_name, options)

        # If we are running outside of the cloud, echo all log messages to
        # $stderr. If app_context is nil we are outside the cloud, too, but we
        # don't write to the console because if app_context is null then we
        # are probably running unit tests.
        app_context = $scope['__instance__']
        @console = app_context != nil && app_context.getEnvironmentName() == nil
      end

      def write(str)
        $stderr.puts str if @console
        $db[@collection_name].save({:time => Time.now.to_i, :msg => str})
      end

      def close
      end
    end
  end
end
