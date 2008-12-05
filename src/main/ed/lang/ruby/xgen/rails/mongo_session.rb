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

require 'base64'
require 'xgen/oid'              # defines ObjectId.marshal_{dump,load}

module XGen

  module Rails

    # MongoSession handles sessions for CGI. The 10gen session object uses the
    # Mongo database for persistence and it manages matching sessions with
    # requests.
    #
    # We store data in the session by marshalling the data to be saved, just
    # like CGI::Session::ActiveRecordStore does.
    #
    # See xgen/rails.rb for the code that tells Rails about this class.
    class MongoSession

      SESSION_DATA_KEY = :session_data

      attr_reader :session_id

      def initialize(session, options={})
        @session_id = session.session_id # unused
        @data = {}
      end

      # Return the session value stored at +key+.
      def [](key)
        @data[key]
      end

      # Set the session value +key+ to +value+.
      def []=(key, value)
        @data[key] = value
      end

      # Restore the contents of the session from the database.
      def restore
        @data = {}
        marshalled_data = $session[SESSION_DATA_KEY]
        @data = Marshal.load(Base64.decode64(marshalled_data)) if marshalled_data
        self
      end

      # Save the session to the database.
      def update
        $session[SESSION_DATA_KEY] = Base64.encode64(Marshal.dump(@data))
      end

      # Close the session. Calls update, which saves the session to the database.
      def close
        update
      end

      # Delete the contents of the session. The session may still be used.
      def delete
        @data = {}
        $session[SESSION_DATA_KEY] = nil
      end

    end
  end
end
