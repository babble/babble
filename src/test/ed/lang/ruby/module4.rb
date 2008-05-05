
module A

  module B
    
    class C
      def C.create
        return 17
      end
    end

    def B.b
      f = 1
      f = ::A::B::C::create
      puts f
    end
  end
end

A::B.b
