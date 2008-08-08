
assert.raises( 
    function(){
        scope.setToThrow( JSException( "hi" ) );
        var x = 5;
    }
);
