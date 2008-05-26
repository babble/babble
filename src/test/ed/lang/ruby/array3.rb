

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
