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
# A GridFile is a StringIO that reads from the database when it is created and
# writes to the database when it is closed.
#
# TODO: allow retrieval by _id, return _id on close, expose _id, more modes,
# perhaps use delegation instead of inheritance.
class GridFile < StringIO

  RESERVED_KEYS = %w(_ns _id filename contentType length chunkSize next uploadDate _save _update)

  class << self                 # Class methods

    # Reads a GridFile from the database and returns it, or +nil+ if is not
    # found.
    def find(name)              # :nodoc:
      raise "$db not defined" unless $db
      $db['_files'].findOne({:filename => name})
    end

    # Opens a GridFile with the given mode. If a block is given then the file
    # is passed in to the block and is closed at the end of the block.
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
      raise "$db not defined" unless $db
      $db['_files'].remove({:filename => name})
    end
    alias_method :delete, :unlink

    # If the named GridFile exists in the database, returns +true+.
    def exist?(name)
      find(name) != nil
    end
    alias_method :exists?, :exist?

  end

  # Opens a GridFile with the given mode.
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

  def [](key)
    @metadata[key]
  end

  def []=(key, value)
    @metadata[key] = value unless RESERVED_KEYS.include?(key.to_s)
  end

  # Closes a GridFile. The data is not saved to the database until this method
  # is called.
  def close
    rewind()
    if @mode == :write || @mode == :append
      raise "$db not defined" unless $db
      f = Java::EdJs::JSInputFile.new(@name, nil, read())
      @metadata.each { |k, v| f.set(k, v) unless RESERVED_KEYS.include?(key.to_s) }
      $db['_files'].save(f)
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
