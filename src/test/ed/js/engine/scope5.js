a = 2;
function foo(){
    var a;
    a = 5;
    return {};
}
print(a); 
foo();
print(a); 

a = 2;
function foo2(){
    var a;
    a = 5;
    return function(){};
}
print(a); 
foo2();
print(a); 


a = 2;
function foo3(){
    var a;
    print( a ? "y" : "n" );
    a = 5;
    print( a ? "y" : "n" );
    return function(){};
}
print(a); // output: 2
foo3();
print(a); // output: 5
