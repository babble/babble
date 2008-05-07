
def go( a , b )
  return a , b
end

class A
  def blah
    @a , @b = go( 3 , 5 )
    puts @a
    puts @b
    @a , @b = go( 4 , 7 )
  end
end

a = A.new
z = a.blah
puts z[0] + z[1]
m,n = a.blah
puts m
puts n

