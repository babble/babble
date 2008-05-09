
def go(z)
  begin
    if ( z == 4 )
      raise 1
    end
    puts z if z == 5
    if ( z == 6 )
      puts "six"
      return
    end
  rescue 
    puts "r"
  else
    puts "e"
  end
end

go( 5 )
go( 4 )
go( 6 )
