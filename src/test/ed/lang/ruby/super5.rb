
class A
  def initialize 
    puts "in A initialize"
    @bar = 1;
  end
end

class B < A

  def initialize
    super
    puts "B"
    @foo = 2;
  end
  
  def go
    puts @bar;
    puts @foo;
  end
end

b = B.new
b.go
 

puts( "***" );

class AA

  def iamaa
  end
  
  def initialize( bar )
    @bar = bar;
  end
end

class BB < AA

  def iambb
  end

  def initialize
    super 3
    puts "BB"
    @foo = 2;
  end
  
  def go
    puts "bar #{@bar}"
    puts "foo #{@foo}"
  end
end

bb = BB.new
bb.go
 
