
class Foo
  @@a = 1;
  
  def blah
    return @@a;
  end
end

a = Foo.new;
puts a.blah

b = Foo.new;
puts b.blah
