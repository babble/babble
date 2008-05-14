
function foo(){
    print("a");
}

if ( 5 == 4 ){
    print( "1" );
    function foo(){
        print( "b" );
    }
}

foo();



function bar(){
    print("1");
}    

bar();

function bar(){
    print( "2" );
}



function good(){
    print( "g1" );
}

good();

if ( 5 == 5 ){
    function good(){
        print( "g2" );
    }
}

good();

function good(){
    print( "g3" );
}

good();
