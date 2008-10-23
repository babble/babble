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

  module Rails

    class MongoSession

      attr_reader :session_id

      def self.find_by_session_id(session_id)
        @session_id = session_id
        $session
      end

      def initialize(hash_of_session_id_and_data)
        @session_id = attributes[:session_id]
        self.data = attributes[:data]
      end

      def session_id
        @session_id
      end

      def data
        data = {}
        $session.keys.each { |k| data[k] = $session[k] }
        data
      end

      def data=(session_data)
        session_data.each { |k, v| $session[k] = v } if attributes[:data]
      end

      def save
        $session[:_timestamp] = Time.new.to_i
      end

      def destroy
        $session.keys.each { |k| $session.remove_field(k) }
      end

    end
  end
end
