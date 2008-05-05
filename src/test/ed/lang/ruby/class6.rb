
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

class B

  foo = 5

  if foo == 5 
    def b
      puts "101"
    end
  elsif
    def b 
      puts "102"
    end
  end

  if foo == 6
    def c
      puts "103"
    end
  elsif
    def c 
      puts "104"
    end
  end


end

b = B.new
b.b
b.c

