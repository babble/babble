
def silly( n )
  puts "_"
  puts self.A
  puts n
end

class Lower

  A = 5
  
  attr_accessor :all
  
  def initialize
    @all = [ "1" ]
  end
  
  def a
    return @all
  end
  
  def self.A
    return 111
  end
  
  def self.funny
    puts "funny"
  end
  
end

class Base < Lower
  
  silly( "2" )

  def blah(z)
    @z = z
  end

end

class A < Base
  def go
    blah( 5 )
  end

  def p
    puts @all.length
    puts @z
  end
end

class B < Base
  def go
    blah( 6 )
  end

  def p
    puts @all.length
    puts @z
  end

  def self.blah
    puts "B.self.blah"
    funny
  end
    
end

a = A.new
a.go
a.p

b = B.new
b.go
b.p

a.p


B.blah
