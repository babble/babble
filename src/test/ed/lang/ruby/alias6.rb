
class Foo
  
  def silly
    puts "A"
  end

  alias silly2 silly
  
end

f = Foo.new
f.silly
f.silly2

class Foo
  alias silly3 silly
  def silly
    puts "B"
  end
end

f = Foo.new
f.silly
f.silly2
f.silly3

class Foo
  alias silly3 silly
  def silly
    puts "B"
  end
  alias << silly
end

f = Foo.new
f.silly
f.silly2
f.silly3
