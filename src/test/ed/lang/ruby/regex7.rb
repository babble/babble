
def blah(a)
  puts "abcdef".sub( /.#{a}./ , "_" )
end

blah( "z" )
blah( "a" )
blah( "b" )
blah( "c" )
blah( "f" )
