
module Moo
  def m
    puts "a #{@yay}"
    return 1
  end
end

class A
  include Moo
  
  def initialize
    @yay = 17
  end
  
end

a = A.new
puts a.m
  
