
print( 5 );

var a = 3;
print( a );

a = 4;
print( a );

function foo(){
    return 5;
}

print( foo() );

print( function(){ return 7; }() );

function(){ print( "yay" ); return 5; }
function bar(){ return 5; }

bar();

var hehe = function(){ return 9; };
print( hehe() );
