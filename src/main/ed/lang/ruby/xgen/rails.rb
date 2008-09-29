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
  if File.exist?(File.join($local.getRoot.getPath, 'public', uri[1..-1]))
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
require 'dispatcher'
require 'xgen/rails/dispatcher'
