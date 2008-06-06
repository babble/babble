module A
  def self.a
    puts "a"
  end
end

module A
  
  FOO = { "a" => 7 }

  def self.b
    puts "b"
    puts FOO["a"]
  end
end

A::a
A::b

