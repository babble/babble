
p = /a
b/

puts p.match( "ab" ) == nil;
puts p.match( "a\nb" ) == nil;

p = /a\nb/
puts p.match( "ab" ) == nil;
puts p.match( "a\nb" ) == nil;
puts p.match( "a\nbc" ) == nil;
puts p.match( "a\nb" );

p = /a\nb/
puts p.match( "a\nb" ) == nil;
puts p.match( "a\nb" );
