
a1 = 1;
a2 = 2;

print( a1 + " " + a2 );

function foo (){
    var a1 = a2 = 3;
    print( a1 + " " + a2 );
}

print( a1 + " " + a2 );
foo();
print( a1 + " " + a2 );

