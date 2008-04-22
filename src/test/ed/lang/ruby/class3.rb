
class Foo

  def initialize
    @a = 7;
  end

  def bar
    return @a
  end
  
end

f = Foo.new
puts f.bar
