
class Foo

  def initialize(b)
    @blah = b
  end
  
  def a 
    puts "a1"
    b
    puts "a2"
    c( 5 )
    puts "a3"
  end
  
  def b
    puts "b"
  end

  def c(z)
    puts "from c #{@blah} "
    puts "c #{z}"
    zz
  end

  def zz
    puts "from zz #{@blah} "
  end
end

f = Foo.new(17171)
f.a

  
