
function zzz( a ){
    return 1 + a;
};

function xxx(){
    return zzz( 1 );
};

assert( 2 == xxx() );

xxx.getScope( true )["zzz"] = function( a ){
    return 2 + a;
};
assert( 3 == xxx() );

xxx.clearScope();
assert( 2 == xxx() );

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


buf = "";
function foo(){
    print( "eliot" );
};

foo.getScope( true ).print = function( s ){
    buf += s;
};

foo();

assert( "eliot" == buf );

