
a = new Number( 5 )
print( a );
print( a == 5 );

print( ( new Number( new Date( 12312312 ) ) ) );

n = 5;
Number.prototype.a = function(){ return 6; };
n.a();

print( Number('0.12') );
print( Number('-0.12') );
print( Number('.12') );
print( Number('-.12') );

print( Number('5.12321') );
print( Number('5') );

print( Number('0') );
print( Number('-123') );


function c( a , b ){
    print( a < b );
}

c( 4 , null );
c( null , 4 );
c( null , -4 );
c( -4 , null );

// TODO
//n = 5;
//Number.prototype.b = function(){ return 7; };
//print( n.b );
