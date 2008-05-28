
Foo = Struct.new( :a , :b )
f = Foo.new
f.a = 5
puts f.a

f = Foo.new( "hi" , "bye" )
puts f.a
puts f.b
