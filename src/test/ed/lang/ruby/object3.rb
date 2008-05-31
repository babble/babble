
class A
  def initialize
    @foo = nil
  end
  
  def go
    puts "foo=#{@foo}"
    puts "" == @foo.to_s
  end
end

a = A.new
a.go
    
