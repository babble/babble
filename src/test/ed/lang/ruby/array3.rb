

=begin
    Copyright (C) 2008 10gen Inc.
  
    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
  
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
=end

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

a = [ [ 1 , 2 , 3 ] , [ 2 , 3 , 1 ] ]
a.collect! do|z,y|
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
