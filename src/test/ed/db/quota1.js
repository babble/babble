db = connect( "test" );
t = db.quota1;

assert.raises( 
    function(z){
        db.eval(
            function(){
                var a = 5;
                while ( true ){
                    a += 2;
                    scope.toString();
                }
            }
        )
    }
);


assert.raises( 
    function(z){
        db.eval(
            function(){
                var a = 5;
                sleep( 150000 );
            }
        )
    }
);



assert.raises( 
    function(z){
        db.eval(
            function(){
                var a = 1;
                while ( true ){
                    a += 1;
                    sleep( 1000 );
                }
            }
        )
    }
);
