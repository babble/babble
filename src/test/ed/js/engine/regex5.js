
var r = /the/g;
print( r );
print( "abe and the cow".match( r ) );
print( "abe and th cow".match( r ) );
print( "abe and the cow".match( "the.[abcow]\+" ) );

print( "foo the the the cow".replace( /the/ , "___" ) );
print( "foo the the the cow".replace( /the/g , "___" ) );

print( "the 123 food".match( /\d+/ ) );

print( "the 123 food".replace( /(\d+)/ , "__$_$1__") );


print( "the 123 food".replace( /(\d+)/ , 
                               function( $0 ){ 
                                   return "* " + $0 + " *" ; 
                               } 
                               ) 
       );


print( "===a===".replace( /(?:^|\xB6)(={1,6})(.*?)[=]*(?=\xB6|$)/g, 
                          function($0,$1,$2){ 
                              var h=$1.length; 
                              return "<h"+h+">"+$2+"</h"+h+">";
                          } ) );



/123/.test( "the 123 is here" );
/123/.test( "the 12 is here" );
/(is)/.test( "the 12 is here" );
