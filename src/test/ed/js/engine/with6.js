
a = { z : 5 };
with( a ) print( z );

b = { z : 6 };
with( a ){
    print( z );
    with( b ){
        print( z );
    }
    print( z );
}


a = { z : 1 };
b = null;
print( "1 = " + a. z );

with ( a ){

    print( "1 = " + z );
    z = 4;
    print( "4 = " + z );

    b = 3;
    print( "3 = " + b );
}

print( "*4 = " + a.z );
print( "null = null " );
print( "3 = " + b );

// ----
print( "****" );

A = function(){
    this.buf = "";
};

A.prototype.print = function( s ){
    this.buf += s;
};
A.prototype.z = 17;

a = new A();
with( a ){ 
    print( "hehe" );
}

print( a.z );
print( a.buf );
print( a.buf.length );

