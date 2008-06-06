
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

PUNCT_Q = Regexp::quote( '*-_+^~%' );
puts PUNCT_Q

a = /([^\s\[{(>#{PUNCT_Q}][#{PUNCT_Q}]*)\'/;

puts Regexp::quote( "a\\" )
puts Regexp::quote( "a/" )

def closing_tag?(item)
  #item =~ %r!^\s*</[^>]+>\s*$!
end

puts /https:\/\//.match( "https://" )
puts %r!https://!.match( "https://" )

puts "abc".split( // )[0]
