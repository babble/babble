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

require 'xgen/rails/cgi'

class Dispatcher

  class << self

    def new_cgi(output)
      failsafe_response(output, '400 Bad Request') { XGen::Rails::CGI.new }
    end

    # If the block raises, send status code as a last-ditch response.
    def failsafe_response(output, status, exception = nil)
      yield
    rescue Exception  # errors from executed block
      begin
        $response.setHeader("Status", status.to_s)
          
        if exception
          message    = exception.to_s + "\r\n" + exception.backtrace.join("\r\n")
          error_path = File.join(RAILS_ROOT, 'public', '500.html')

          if defined?(RAILS_DEFAULT_LOGGER) && !RAILS_DEFAULT_LOGGER.nil?
            RAILS_DEFAULT_LOGGER.fatal(message)

            $response.setContentType("text/html")

            if File.exists?(error_path)
              output.write(IO.read(error_path))
            else
              output.write("<html><body><h1>Application error (Rails)</h1></body></html>")
            end
          else
            $response.setContentType("text/plain")
            output.write(message)
          end
        end
      rescue Exception  # Logger or IO errors
      end
    end

  end
end
