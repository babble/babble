
module A
  class B
    module C
      module D
        def foo
          puts "foo"
        end
      end
    end
  end
end

include A::B::C::D
foo

 
