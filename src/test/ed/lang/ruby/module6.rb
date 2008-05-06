module A
  def self.a
    puts "a"
  end
end

module A
  def self.b
    puts "b"
  end
end

A::a
A::b

