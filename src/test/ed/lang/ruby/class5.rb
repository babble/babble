
class Foo
  @@a = 1;

  def initialize( foo )
    puts foo
  end
    
  
  def blah
    return @@a;
  end
end

a = Foo.new(21);
puts a.blah

b = Foo.new(22);
puts b.blah
