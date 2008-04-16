
def silly
  puts "this is silly"
end

class Foo
  
  attr_accessor :a
  
  silly();

  def initialize
    puts "in init";
    @a = 7;
  end

  def go
    puts "in go"
  end
  
end

f = Foo.new();
f = Foo.new();
puts f.a();


f.go;
f.go();



