
option = { a : "1" };
opt = 1;

function(t) {
    for( opt in option ) {
        print( opt );
    }
};

print( opt );
