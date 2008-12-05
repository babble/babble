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

class String
  # Convert this String to an ObjectId.
  def to_oid
    ObjectId.new(self)
  end
end

# An ObjectId. The primary key for all objects stored into Mongo through
# Babble, stored in the _id field.
#
# Normally, you don't have to worry about ObjectIds. You can treat _id values
# as strings and XGen::Mongo::Base or Babble will covert them for you.
#
# The ObjectId class constructor and initialize methods are defined in Java.
class ObjectId
  # Convert this object to an ObjectId.
  def to_oid
    self
  end

  # Tells Marshal how to dump this object.
  def marshal_dump
    to_s
  end

  # Tells Marshal how to load this object.
  def marshal_load(oid)
    _internal_oid_set(oid.to_s)
  end
end
