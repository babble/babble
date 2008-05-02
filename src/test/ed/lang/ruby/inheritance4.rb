
class A 
  def initialize( name )
    @name = name
  end
  
  def go
    puts @name
  end
end

class B < A
end

b = B.new( "a" );
b.go
