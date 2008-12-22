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

require 'stringio'

# A GridFile file lives in the database. GridFiles can be retrieved by name.
#
# GridFiles have attributes. You can read/write them using [] and []=. One
# interesting one is 'contentType', which tells Babble how to serve the file.
#
# Example:
#
#   GridFile.open("myfile", 'w') { |f|
#     f['contentType'] = 'text/plain'  # or f.contentType = 'text/plain'
#     f['creator'] = 'Spongebob Squarepants'
#     f.puts "Hello, GridFS!"
#   }
#   GridFile.open("myfile", 'r') { |f|
#     puts f.read
#     puts f['contentType']            # or f.contentType
#     puts f['creator']
#   }
#   # => Hello, GridFS!
#   # => text/plain
#   # => Spongebob Squarepants
#   GridFile.exist?("myfile")
#   # => true
#   GridFile.delete("myfile")
#
# A file's data is not saved to the database until the close method is called.
# close is automatically called at the end of the block passed to open.
#
# The database connection defaults to the global <code>$db</code>. You can set
# the connection using GridFile.connection= and read it with
# GridFile.connection.
#
#   # Set the connection to something besides $db
#   GridFile.connection = connect('my-database')
#
# A GridFile is a StringIO that reads from the database when it is created and
# writes to the database when it is closed. <em>This will change in the
# future, so that a file's contents are not read into memory right away.</em>
#
# TODO: allow retrieval by +_id+, return +_id+ on close, expose +_id+, more
# modes, perhaps use delegation instead of inheritance.
class GridFile < StringIO

  RESERVED_KEYS = %w(_ns _id filename contentType length chunkSize next uploadDate _save _update)

  @@connection = nil

  class << self                 # Class methods

    # Return the database connection. The default value is <code>$db</code>.
    def connection
      conn = @@connection || $db
      raise "connection not defined" unless conn
      conn
    end

    # Set the database connection. If the connection is set to +nil+, then
    # <code>$db</code> will be used.
    def connection=(val)
      @@connection = val
    end

    # Read a GridFile from the database and returns it, or +nil+ if is not
    # found.
    def find(name)              # :nodoc:
      connection['_files'].findOne({:filename => name})
    end

    # Open a GridFile with the given mode. If a block is given then the file
    # is passed in to the block and is closed (thus saved to the database) at
    # the end of the block.
    def open(name, mode)
      grid_file = GridFile.new(name, mode)
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

    # Delete the named GridFile from the database.
    def unlink(name)
      Java::EdLangRuby::GridFS.remove(connection, name)
    end
    alias_method :delete, :unlink

    # If the named GridFile exists in the database, return +true+. (Also
    # aliased to "exists?".)
    def exist?(name)
      find(name) != nil
    end
    alias_method :exists?, :exist?

  end

  # Open a GridFile with the given mode.
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
              raise "illegal GridFile mode #{mode}"
            end

    @f = self.class.find(@name)  # may be nil

    @metadata = {}
    @f.keySet().each { |key| @metadata[key] = @f.get(key) } if @f

    if @f && (@mode == :read || @mode == :append)
      write(@f.asString())
    end
    rewind() if @mode == :read
  end

  # Return the metadata named +key+.
  def [](key)
    @metadata[key]
  end

  # Set metadata named +key+ to +value+.
  def []=(key, value)
    @metadata[key] = value unless RESERVED_KEYS.include?(key.to_s)
  end

  # Close a GridFile, saving the contents of the file to the database. The
  # data is not saved to the database until this method is called.
  def close
    rewind()
    if @mode == :write || @mode == :append
      Java::EdLangRuby::GridFS.save(self.class.connection, self)
    end
    super
  end

  # Turn f.foo into f['foo'] and f.foo = bar into f['foo'] = bar.
  def method_missing(sym, *args)
    if sym.to_s[-1,1] == "="
      if args.length == 1       # f.foo = bar
        self[sym.to_s[0,-2]] = args[0]
      end
    elsif args.length == 0      # f.foo
      self[sym.to_s]
    else                        # anything else
      super
    end
  end

end
