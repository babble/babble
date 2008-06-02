
class A  

  Z = 101

  def self.foo
    puts "foo"
  end

  def self.bar(z)
    puts "bar#{z}"
  end
  
end

class B < A
  
  Z = 102
  
  puts "h1"
  foo
  puts "h2"
  bar(5)
  puts "h3"
end

puts A::Z
puts B::Z

b = B.new
b = B.new
    
class AA

  attr_accessor :z

  def initialize
    puts "AA cons"
    @z = 111;
  end

end

class BB < AA
end

bb = BB.new
puts bb.z
    
    
