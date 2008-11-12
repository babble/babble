

var p = /(\d)/g

var a = "a";
var b = "1";

var c = "1a2b3c";
while ( r = p.exec( c ) ){
    print( r );
}

x = "1a2";
y = "1b";
print( p.exec( x ) );
print( p.lastIndex );

print( p.test( y ) );
print( p.lastIndex );

print( p.exec( y ) );
print( p.lastIndex );

