
function zzz( a ){
    return 1 + a;
};

function xxx(){
    return zzz( 1 );
};

assert( 2 == xxx() );
print( "---" );

xxx.getScope( true )["zzz"] = function( a ){
    return 2 + a;
};
assert( 3 == xxx() );
print( "---" );

xxx.clearScope();
assert( 2 == xxx() );
print( "---" );

now = new Date();

function other(){
    while ( (new Date()).getTime() - now.getTime() < 200 ){
        xxx.getScope( true )["zzz"] = function( a ){
            return 2 + a;
        };
        assert( 3 == xxx() );
        
        xxx.clearScope();
        assert( 2 == xxx() );
    }
};

t = new Array();
for ( i=0; i<3; i++ )
    t[i] = fork( other );

t.forEach( function(z){ z.start(); } );
t.forEach( function(z){ z.join(); } );

print( "---" );

buf = "";
function foo(){
    print( "eliot" );
};

foo.getScope( true ).print = function( s ){
    buf += s;
};

foo();

assert( "eliot" == buf );

