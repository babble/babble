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

# This file is a typical _init.rb file for a Rails site running on Babble. You
# can use this file by making your _init.rb file contain
#
#   require 'xgen/rails/init'
#
# You don't have to use this file---you can copy and modify the code below
# and put it in your _init.rb instead.

# Set adapter to CGI.
$adapterType = 'CGI'

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
