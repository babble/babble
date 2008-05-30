
class A
  PUNCT_Q = Regexp::quote( '*-_+^~%' );
  R = /([^\s\[{(>#{PUNCT_Q}][#{PUNCT_Q}]*)\'/;
end


