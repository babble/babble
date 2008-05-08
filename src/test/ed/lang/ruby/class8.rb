
class Foo
  
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
    puts "c #{z}"
  end
end

f = Foo.new
f.a

  
