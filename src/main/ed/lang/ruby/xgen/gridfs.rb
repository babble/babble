require 'stringio'

# A GridFS file lives in the database. You can retrieve it by name.
#
# Example:
#
#   GridFS.open("myfile", 'w') { |f| f.puts "Hello, GridFS!" }
#   GridFS.open("myfile", 'r') { |f| puts f.read }
#   # => Hello, GridFS!
#
# TODO: allow retrieval by _id, return _id on close, expose _id, more modes
class GridFS < StringIO

  def self.open(name, mode)
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

  private

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
    write($db['_files'].findOne({:filename => name}).asString()) if @mode == :read || @mode == :append
    rewind() if @mode == :read
  end

  public

  def close
    rewind()
    $db['_files'].save(javaCreate("ed.js.JSInputFile", @name, nil, read())) if @mode == :write || @mode == :append
    super
  end

end
