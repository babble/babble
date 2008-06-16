
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

var num = 40000;

assert( good( num ) == bad( num ) );

print( "inf2" );
for ( var i=0; i<5; i++ ){

    var a = Date.timeFunc( good , num );
    var b = Date.timeFunc( bad , num );

    b += Date.timeFunc( bad , num );
    a += Date.timeFunc( good , num );

    if ( i == 0 )
        continue; // for jit

    print( "\t good: " + a  + " bad: " + b );
    //assert( a * 1.2 < b );
}

