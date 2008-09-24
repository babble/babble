

all = [];

for ( var i=0; i<2; i++ ){
    var num = i;
    all.push( 
        function(){
            return num;
        }
    );
}

all.forEach(
    function( z ){
        print( z() );
    }
);
        
    
