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


=begin
To get Rails to work with babble, you need to create two files. First,
create a file named _init.rb in the root directory of your Rails app that
looks like this:

# Look for the requested URI in the public directory. If not found, pass it on
# to the 10gen Rails dispatcher.
$mapUrlToJxpFile = Proc.new do |uri, req|
  uri = '/index.html' if uri == '/'
  if File.exist?(File.join(File.dirname(__FILE__), 'public', uri[1..-1]))
    "/public" + uri
  else
    "public/xgen_dispatch.rb"
  end
end

Next, add public/xgen_dispatch.rb:

require 'xgen/rails'
load 'xgen/cgi_env.rb'
Dispatcher.dispatch
=end


# RAILS_ENV is set to the name of your cloud environment. If none is specified
# (for example, you are running locally), then 'development' is used.
ENV['RAILS_ENV'] = $scope['__instance__'].getEnvironmentName() || 'development'

require File.join($local.getRoot.getPath, "config/environment") unless defined?(RAILS_ROOT)
require "dispatcher"

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
