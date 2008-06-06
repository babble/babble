def valid_email?(email)
  email.size < 100 && email =~ /.@.+\../ && email.count('@') == 1
end

def foo(z)
  case z
  when 6
    "six"
  when 7
    "seven"
  else
    "blah"
  end
end

foo( 6 );
foo( 7 );
foo( 8 );
  

class A
  def delete
    puts "here"
  end
end

a = A.new
a.delete

def blah( a , b )
  puts "a:#{a} b:#{b}"
end

blah( 7 , 10 );


def blah2( a , b )
  puts "a:#{} b:#{b}"
end

blah( 7 , 10 );

z = nil
puts "abc#{ z }def"
z = 5
puts "abc#{ z unless z == 5 }def"
puts "abc#{ z unless z == 4 }def"

