
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
