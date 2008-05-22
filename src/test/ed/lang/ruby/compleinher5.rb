
module Foo

  def fun(z)
    helper( z + 3 )
    puts @a
  end
  
  def helper(y)
    puts "helper #{@a + y} "
    helper2(2)
  end

  def helper2(z)
    puts @a
    puts "2 #{z}"
    helper3(z)
    self.helper3(z)
  end

  def helper3(z)
    puts "3 #{z + @a}"
  end
end

class A
  def initialize
    @a = 6
  end
end

A.send :include , Foo

a = A.new
a.fun(5)

class B < A
  def blah(z)
    fun(z+1)
  end
end

b = B.new
b.fun(9)
b.blah(12)


