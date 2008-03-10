
option = { a : "1" };
opt = 1;

function(t) {
    for( opt in option ) {
        print( opt );
    }
};

print( opt );


increm = function(options){
    n = options.n;
    return function(i){
        return i+n;
    };
};

f = increm({n: 5});
print(f(2));

g = increm({n: 8});
print(g(2));

print(f(2));

