# class A
#   def foo( z )
#     "A.foo";
#   end
# end

# class B < A
#   def foo( z )
#     if ( z )
#        return bar
#     end
#     super
#   end
#   def bar
#     return "B.bar"
#   end
# end

# class C < B
#   def foo( z )
#     super
#   end
#   def bar
#     return "C.bar"
#   end
# end

# c = C.new
# puts c.foo( false )
# puts c.foo( true )

