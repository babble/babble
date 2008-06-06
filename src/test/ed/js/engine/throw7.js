
function go(z){
    try {
        throw z;
    }
    catch ( e if z == 6 ){
        print( "six" );
    }
    catch ( e if z == 7 ){
        print( "seven" );
    }
    finally {
        print( "ff" );
    }
}

go( 6 );
go( 7 );
try {
    go( 8 );
}
catch ( e ){
    print( e );
}


print(
    function( z ){
        try {
            throw z;
        }
        catch( e if e == 5 ){
            print("a");
            return "asd";
        }
        finally {
            print( "basdlkajsd")
            
        }
    }( 5 ) 
);

