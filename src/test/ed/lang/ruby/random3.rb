
class Foo
    attr_accessor :a
    attr_accessor :b
end

a = 7;

f = Foo.new
f.a = a;
f.b = a;

puts f.a
puts f.b
