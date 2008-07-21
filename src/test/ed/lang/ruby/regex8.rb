
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
