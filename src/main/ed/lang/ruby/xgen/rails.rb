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

  require 'xgen/rails/init'

Next, add public/xgen_dispatch.rb:

  require 'xgen/rails'
  Dispatcher.dispatch
=end


# RAILS_ENV is set to the name of your cloud environment. If none is specified
# (for example, you are running locally), then 'development' is used. If the
# cloud environment name is "www" (the default production name), then use
# "production" for RAILS_ENV instead.
app_context = $scope['__instance__']
ENV['RAILS_ENV'] = (app_context && app_context.getEnvironmentName()) || 'development'
ENV['RAILS_ENV'] = 'production' if ENV['RAILS_ENV'] == 'www'

# Logging
require 'logger'
require 'xgen/mongo/log_device'
# Default LogDevice capped collection size is 10 Mb.
RAILS_DEFAULT_LOGGER = Logger.new(XGen::Mongo::LogDevice.new("rails_log_#{ENV['RAILS_ENV']}"))

# XGen::Mongo classes
require 'xgen/mongo'

# Normal Rails configuration
require File.join($local.getRoot.getPath, "config/environment") unless defined?(RAILS_ROOT)
require 'dispatcher'

# Patch Rails
require 'xgen/rails/active_record'

# Session
require 'xgen/rails/mongo_session'
ActionController::CgiRequest::DEFAULT_SESSION_OPTIONS[:database_manager] = XGen::Rails::MongoSession
