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

      def initialize(session, options={})
        @session_id = session.session_id # unused
        @data = {}
      end

      def [](key)
        @data[key]
      end

      def []=(key, value)
        @data[key] = value
      end

      def restore
        @data = {}
        $session.keySet().each { |k| @data[k] = $session[k] unless k == '_key' }
        self
      end

      def update
        @data.each { |k, v| $session[k] = v unless k == '_key' }
        # FIXME we need to "tickle" the session with a new value because right
        # now sessions do not notice changes in sub-objects, only top-level
        # values and objects.
        $session[:_timestamp] = Time.new.to_i
      end

      def close
        update
      end

      def delete
        @data = {}
        $session.keys.each { |k| $session.removeField(k) }
      end

    end
  end
end
