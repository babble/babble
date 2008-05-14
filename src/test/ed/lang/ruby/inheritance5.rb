
class A 
  def foo(z)
    "A.foo";
  end
end

a = A.new
puts a.foo( true )

class B < A
  def foo(z)
    "B.foo"
  end
end

b = B.new
puts b.foo( true )

class C < A
  def foo(z)
    if ( z )
      return "C.foo"
    end
    super
  end
end

c = C.new
puts c.foo( true )
puts c.foo( false )


