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

# This file must be loaded (not required) on every dispatch. It copies
# information from the babble request object into the environment so CGI can
# do its magic.

ENV['SERVER_PROTOCOL'] = $request.getProtocol
ENV['HTTP_HOST'] = $request.getHeader('Host')
ENV['SERVER_NAME'] = $request.getHost
ENV['SERVER_PORT'] = $request.getPort.to_s
ENV['REQUEST_METHOD'] = $request.getMethod.upcase
ENV['PATH_INFO'] = $request.getPathInfo
ENV['REQUEST_URI'] = ENV['PATH_INFO']
ENV['QUERY_STRING'] = $request.getQueryString
ENV['REMOTE_HOST'] = $request.getRemoteHost
ENV['REMOTE_ADDR'] = $request.getRemoteIP
ENV['CONTENT_TYPE'] = $request.getContentType
ENV['CONTENT_LENGTH'] = $request.getContentLength.to_s
# TODO cookies
# TODO session
