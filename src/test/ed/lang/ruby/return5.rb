
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


class B 
  def redirect_to(z)
    puts z
    return 6;
  end
  
  def foo
    zz = ( redirect_to :login_path and return unless @user );
    return 7;
  end
  
  def foo2
    redirect_to :login_path and return unless @user;
    return 7
  end

 def z
   @user = 5
 end

end

b = B.new
b.foo
b.foo2
b.z
b.foo



def blah(z)
  return z ? [ 1 , 2 ] : [ nil , nil ]
end

puts blah( true ) == 1
puts blah( false ) == nil
