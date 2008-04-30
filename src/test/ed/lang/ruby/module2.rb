
module A
  def silly
    puts "hi from silly"
  end
end

include A
silly

module B
  module C
    def blah
      puts "blah"
    end
  end
end

include B
include C
blah
