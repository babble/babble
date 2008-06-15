
function good( num ){
    var x = 6;
    for ( var i=0; i<num ; i++ ){
        x += 2;
    }
    return x;
}

function bad( num ){
    var x = 6;
    var i = "abc"
    for ( var i=0; i<num; i++ ){
        x += 2;
    }
    return x;
}


assert( good(5) == bad(5) );


