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
