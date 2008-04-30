
class Foo

  def Foo.bar
    puts "A"
  end
  
  def bar 
    puts "B"
  end
end

f = Foo.new
f.bar

Foo.bar


class A
  
  attr_accessor :foo;
  
  def initialize
    @foo = 5;
  end
  
  def go
    puts self.foo
  end
end

a = A.new
a.go
