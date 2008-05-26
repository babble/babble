
class Foo
  Z = /^(#{
        ['*', '-', '_'].collect { |ch| '( ?' + Regexp::quote( ch ) + ' ?){3,}' }.join( '|' )
    })$/
end


