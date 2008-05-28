
class A  

  def self.foo
    puts "foo"
  end

  def self.bar(z)
    puts "bar#{z}"
  end
  
end

class B < A
  puts "h1"
  foo
  puts "h2"
  bar(5)
  puts "h3"
end


b = B.new
b = B.new
    
    
