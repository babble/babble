
db = connect( "test" )
t = db.reclaim1;
t.clean();
t.drop();

blob = (new Array(1000)).map( function(z){ return "abc" } ).join( "-" );
print( "blob.length: " + blob.length );

numTotal = 0;

length = 10000;

for ( i=0; i<length; i++ )
    insert();

t.ensureIndex( { name : 1 } );

var correctSize = t.validate().lastExtentSize;

for ( i=0; i<100000; i++ ){
    insert();
    t.remove( { _id : t.findOne()._id } );

    if ( ! ( i % 100 ) )
        assert( t.validate().lastExtentSize - ( blob.length * 3 ) < correctSize );
}

function insert(){
    var o = { name : "abc" + Math.random() };
    o.blob = blob;
    if ( numTotal % 2  == 0 )
        o.asd = blob;
    t.save( o );
    numTotal++;
}


