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

require 'mutex_m'

module XGen

  # Use BabbleThread instead of Thread. Normal Ruby threads are not allowed
  # because Babble applications run in a shared environment.
  #
  # Example:
  #   t = XGen::BabbleThread.new(35) { |arg| arg + 7 }
  #   puts t.join    # => 42
  class BabbleThread

    include Mutex_m

    @@thread_id = Struct.new(:thread_id).new(0)
    @@thread_id.extend Mutex_m

    # Create a new BabbleThread that will execute the given block with
    # arguments in +args+.
    def initialize(*args)
      raise "must supply a block" unless block_given?

      # Generate the next thread id
      @instance_thread_id = nil
      @@thread_id.mu_synchronize {
        @@thread_id.thread_id += 1
        @instance_thread_id = @@thread_id.thread_id
      }

      @work = $scope['__instance__'].queueWork("XGen::BabbleThread #{@instance_thread_id}", Proc.new, *args)
    end

    # Return the value of the block given to #new.
    def join
      @work.getResult()
    end
  end
end
