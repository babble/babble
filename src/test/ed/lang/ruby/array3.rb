

a = [ 1 , 2 , 3 ].collect{ |z| z + 1 }
puts a.length
puts a[0]
puts a[2]

puts a.join( "|" )


a = [ 1 , 2 , 3 ].collect!{ |z| z + 1 }
puts a.length
puts a[0]
puts a[2]

puts a.join( "|" )

a = [ 1 , 2 , 3 ]
puts a[0]
a.collect!{ |z| z + 1 }
puts a.length
puts a[0]
puts a[2]

puts a.join( "|" )

a = [ [ 1 , 2 , 3 ] , [ 2 , 3 ] ]
a.collect!{ |z| z.length }
puts a.length
puts a[0]
puts a[1]

puts "---"

a = [ [ 1 , 2 , 3 ] , [ 2 , 3 , 1 ] ]
a.collect do|z,y|
  puts z
  puts y
end

puts "***"

TEXTILE_TAGS =
  [ [67, 0 ], [69, 0], [75, 8218] ].
  
  collect! do |a, b|
  puts a
  puts b
  puts a.chr
  [a.chr, ( b.zero? ? "" : "&#{ b };" )]
end

TEXTILE_TAGS.collect do |z|
  puts "."
  puts z[0]
  puts z[1]
  z.join( "|" )
end

puts TEXTILE_TAGS.join( "," );


