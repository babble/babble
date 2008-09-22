
function foo(n){
    print( arguments[0] );
    print( arguments.callee );
}

function bar(){
    foo( 5 );
}

bar();
