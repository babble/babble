
function foo(a, b, c){
    return arguments.length;
};

print(foo(1));
print(foo(1, 2));
print(foo(1, 2, 3));
print(foo(1, 2, 3 ,4));

