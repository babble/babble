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

