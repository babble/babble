
function good( num ){
    var x = 6;
    for ( var i=0; i<num ; i += 2 ){
        x += 2;
    }
    return x;
}

function bad( num ){
    var x = 6;
    var i = "abc"
    for ( var i=0; i<num; i += 2 ){
        x += 2;
    }
    return x;
}

var num = 60000;
var numCalls = 10;

assert( good( num ) == bad( num ) );

print( "inf2" );
for ( var i=0; i<5; i++ ){

    var a = Date.timeFunc( good , numCalls , num );
    var b = Date.timeFunc( bad , numCalls , num );

    b += Date.timeFunc( bad , numCalls , num );
    a += Date.timeFunc( good , numCalls , num );

    if ( i <= 1 )
        continue; // for jit
    
    print( "\t good: " + a  + " bad: " + b );
    //assert( a < b , "too slow");
}

