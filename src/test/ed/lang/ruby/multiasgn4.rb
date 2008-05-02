
def foo
  return [ 1 , 2 ];
end

puts( foo().length );
a , b = foo;
puts a
puts b

class A
  attr_accessor :c
end

a = A.new

a.c , b = foo
puts a.c
puts b

a = [5,6];
puts a[0]
puts a[1]
a[5-5] , a[1] = foo
puts a[0]
puts a[1]


puts( ( a , b = foo ).length );
puts a
puts b
