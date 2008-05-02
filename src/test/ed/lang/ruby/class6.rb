
class A 

  def initialize
    puts 1
  end
  
  def foo
    puts "foo"
  end
end

class A 

  def initialize
    puts 2
  end

  def bar
    puts "bar"
  end
end

a = A.new
a.bar
a.foo
  
