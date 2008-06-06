
class A
  @@a ||= 5;
  
  def blah
    puts @@a
  end
end

a = A.new
a.blah
