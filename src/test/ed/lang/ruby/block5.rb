
def foo( a , &block )
  bar( a , &block )
end

def bar( z )
  yield( z )
end

foo 5 do|y|
  puts y
end


