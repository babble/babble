
a = [5,7];
a << 3;
puts a.length;
puts a[2];

class Foo 
  def <<( blah )
      puts blah
  end
end

f = Foo.new
f << 5
