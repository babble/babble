// thread3.js

all = [];

num = 0;

bad = false;

func = function(){
    var before = ++num;
    sleep( 50 );
    if ( num != before )
        bad = true;
}

func();

assert( ! bad );

for ( var i=0; i<3; i++ ){
    all.add( fork( func ) );
}

all.forEach( function(z){ z.start(); } );
all.forEach( function(z){ z.join(); } );

assert( bad );

bad = false;
func = func.synchronizedVersion();

for ( var i=0; i<3; i++ ){
    all.add( fork( func ) );
}

assert( ! bad );
