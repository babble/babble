
class SillyA < Exception
  def silly
    puts "silly"
  end
end

class SillyB < Exception
  def silly
    puts "silly"
  end
end

def go(z)
  begin
    if z == 5
      raise SillyA
    else
      raise SillyB
    end
  rescue SillyA
    puts "a"
  rescue SillyB
    puts "b"
  ensure
    puts "basdlkajsd"
  end
  
end
    
go(5)
go(6)


def go2(z)
  begin
    if z == 5
      raise SillyA
    elsif z == 6
      raise SillyB
    else
      raise 5
    end
  rescue SillyA
    puts "a"
  rescue SillyB
    puts "b"
  rescue
    puts "c"
  end
  
end
    
go(5)
go(6)
go(7)
