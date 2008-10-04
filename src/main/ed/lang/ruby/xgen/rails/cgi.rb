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

    # A wrapper around the raw POST data held by the appserver request object
    # that lets it behave like $stdin for our CGI instance.
    class PostDataInput
      def initialize
        @data = StringIO.new($request.getPostData.toString)
      end
      def read(len)
        @data.read(len)
      end
    end

    class CGI < ::CGI

      def stdinput
        @post_data ||= PostDataInput.new
      end

      # Replace the default behavior by sending header values to $response and returning an empty string.
      def header(options = "text/html")
        case options
        when String
          options = { "type" => options }
        when Hash
          new_options = {}
          options.each { |k,v| new_options[k.downcase] = v }
          options = new_options
        end

        unless options.has_key?("type")
          options["type"] = "text/html"
        end

        if options.has_key?("charset")
          options["type"] += "; charset=" + options.delete("charset")
        end

        if options.has_key?("status")
          $response.setResponseCode(options.delete("status").split.first.to_i)
        end

        if options.has_key?("server")
          $response.setHeader('Server', options.delete("server"))
        end

        if options.has_key?("connection")
          $response.setHeader('Connection', options.delete("connection"))
        end

        $response.setContentType(options.delete("type"))

        if options.has_key?("length")
          $response.setContentLength(options.delete("length"))
        end

        if options.has_key?("language")
          $response.setHeader('Content-Language', options.delete("language"))
        end

        if options.has_key?("expires")
          $response.setHeader('Expires', CGI::rfc1123_date(options.delete("expires")))
        end

        if options.has_key?("cookie")
          if options["cookie"].kind_of?(String) or
              options["cookie"].kind_of?(Cookie)
            $response.addHeader('Set-Cookie', options.delete("cookie").to_s)
          elsif options["cookie"].kind_of?(Array)
            options.delete("cookie").each{|cookie|
              $response.addHeader('Set-Cookie', cookie.to_s)
            }
          elsif options["cookie"].kind_of?(Hash)
            options.delete("cookie").each_value{|cookie|
              $response.addHeader('Set-Cookie', cookie.to_s)
            }
          end
        end
        if @output_cookies
          for cookie in @output_cookies
            $response.addHeader('Set-Cookie', cookie.to_s)
          end
        end

        options.each{|key, value| $response.setHeader(key, value.to_s) }

        ''
      end
    end
  end
end
