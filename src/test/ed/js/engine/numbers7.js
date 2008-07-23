
a = new Number( 5 )
print( a );
print( a == 5 );

print( ( new Number( new Date( 12312312 ) ) ) );

n = 5;
Number.prototype.a = function(){ return 6; };
n.a();

// TODO
//n = 5;
//Number.prototype.b = function(){ return 7; };
//print( n.b );
