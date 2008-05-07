
class A
  def initialize
    @foo = 7
    puts @foo
  end
  
  def foo=(z)
    @foo = z + 1
    puts "yo #{@foo}"
  end
  
  def go(n)
    @foo = n
    puts @foo
  end

  def p
    puts @foo
  end
end

a = A.new
a.go(5)
a.go(6);
    
a.foo= 19

a.p
