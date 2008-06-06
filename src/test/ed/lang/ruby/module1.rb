
module A;
  module B;

    FOO = {}
    
    def self.go
      [ 1 , 2 ].each do|z|
        puts z
        FOO["a"] = 1
        puts FOO["a"]
      end
      return 5
    end

  end
end

puts A::B.go
