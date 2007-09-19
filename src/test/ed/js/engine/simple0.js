
SYSOUT( 5 );

var a = 3;
SYSOUT( a );

a = 4;
SYSOUT( a );

function foo(){
    return 5;
}

SYSOUT( foo );
SYSOUT( foo() );

SYSOUT( function(){ return 6; } );
SYSOUT( function(){ return 7; }() );

function(){ SYSOUT( "yay" ); return 5; }
function bar(){ SYSOUT( "yay" ); return 5; }

bar();

var hehe = function(){ return 9; };
SYSOUT( hehe );
SYSOUT( hehe() );
