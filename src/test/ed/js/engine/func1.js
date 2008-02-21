var a = 3;
var b = 4;

function foo( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();   
}
print( "4=" + foo( 1 , 2 ) );

function bar( a , b ){
    var v = function(){
        return a + 5 + b;
    }
    a = 100;
    return v();   
}
print( "108=" + bar( 2 , 3 ) );

function bar2( a , b ){
    var c = a + b;
    c = c * a;
    return c;
}

print( bar2( 4 , 2 ) );

function bar3( a ){
    var silly = Array( 7 , 1 ) , silly2 = Array(2);
    for ( var i = 0; i<a ; i = i + 1 ){
        print( i );
        silly[0] = i;
    }
    print( silly );
}

bar3( 4 );
bar3( 1 );


function f(){
    
    function g(){
        print("Hi");
    };

    g();
};


f();

try {
    g();
    print( "y" );
}
catch( e ){
    print( "n" );
}


Function.prototype.d = function(){ return 2*this(); };

function f(){ 
    return 1; 
};

print(f.d());

