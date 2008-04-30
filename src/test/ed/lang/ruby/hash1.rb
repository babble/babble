
def foo( a , b )
  puts a 

  puts b[:a]
  puts b[:b]
end


foo 'abc' , :a => 1 , :b => 2
