
class A
  def silly( a )
    yield( a )
  end
end

a = A.new

a.silly(5) do |z|
  puts z 
end

  
