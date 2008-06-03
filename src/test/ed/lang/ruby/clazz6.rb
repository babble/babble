

class A
  
  def self.foo
    puts "A self.foo"
  end

  def foo
    puts "A foo"
  end

end

A.foo
a = A.new
a.foo

class B <A
  
end

B.foo
b = B.new
b.foo
  
