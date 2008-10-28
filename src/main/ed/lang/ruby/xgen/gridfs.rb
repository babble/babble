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

require 'stringio'

# A GridFS file lives in the database. You can retrieve it by name.
#
# Example:
#
#   GridFS.open("myfile", 'w') { |f| f.puts "Hello, GridFS!" }
#   GridFS.open("myfile", 'r') { |f| puts f.read }
#   # => Hello, GridFS!
#   GridFS.exist?("myfile")
#   # => true
#   GridFS.delete("myfile")
#
# A GridFS is a StringIO that reads from the database when it is created and
# writes to the database when it is closed.
#
# TODO: allow retrieval by _id, return _id on close, expose _id, more modes,
# perhaps use delegation instead of inheritance.
class GridFS < StringIO

  class << self                 # Class methods

    # Reads a GridFS from the database and returns it, or +nil+ if not found.
    def find(name)              # :nodoc:
      $db['_files'].findOne({:filename => name})
    end

    # Opens a GridFS with the given mode. If a block is given then the file is
    # passed in to the block and is closed at the end of the block.
    def open(name, mode)
      grid_file = GridFS.new(name, mode)
      if block_given?
        begin
          yield grid_file
        ensure
          grid_file.close if grid_file
        end
      else
        grid_file
      end
    end

    # Delete the named GridFS from the database.
    def unlink(name)
      f = find(name)
      f.remove() if f
    end
    alias_method :delete, :unlink

    # If the named GridFS exists in the database, returns +true+.
    def exist?(name)
      find(name) != nil
    end
    alias_method :exists?, :exist?

  end

  # Opens a GridFS with the given mode.
  #
  # Modes:, 'r', 'w', or 'a'.
  def initialize(name, mode='w')
    super('', 'a+')
    @name = name
    @mode = case mode.downcase
            when /^r/
              :read
            when /^w/
              :write
            when /^a/
              :append
            else
              raise "illegal GridFS mode #{mode}"
            end
    if @mode == :read || @mode == :append
      f = self.class.find(@name)
      write(f.asString()) if f
    end
    rewind() if @mode == :read
  end

  # Closes a GridFS. The data is not saved to the database until this method
  # is called.
  def close
    rewind()
    $db['_files'].save(Java::EdJs::JSInputFile.new(@name, nil, read())) if @mode == :write || @mode == :append
    super
  end

end
