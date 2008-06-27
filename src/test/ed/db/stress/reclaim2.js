
db = connect( "test" )
t = db.reclaim1;
t.clean();
t.drop();

function randString( len ){
    var s = "";
    for ( var i=0; i<len; i++ )
        s += "z";
    return s;
}

var blobSize = 1024;

numTotal = 0;

length = 10000;

for ( i=0; i<length; i++ )
    insert();

t.ensureIndex( { name : 1 } );

correctSize = t.validate().lastExtentSize;
correctLength = t.count();

randGen = core.util.random().getRandom(1);

for ( i=0; i<100000; i++ ){
    
    var namea = "abc" + ( randGen.nextFloat() * correctLength ).toFixed(0);
    var nameb = "abc" + ( randGen.nextFloat() * correctLength ).toFixed(0);


    var a = t.findOne( { name : namea } );
    var b = t.findOne( { name : nameb } );
    
    assert( a , "no a  : " + namea );
    assert( b , "no b  : " + nameb );
    
    var ratio = randGen.nextFloat();
    
    a.blob = randString( blobSize * 2 * ratio );
    b.blob = randString( blobSize * 2 * ( 1 - ratio ) );

    t.save( a );
    t.save( b );
    
    if ( ! ( i % 100 ) ){
        var v = t.validate();
        assert( v.valid , "not valid" );
        assert( v.lastExtentSize < correctSize * 2 , "got too big" );
        assert( t.count() == correctLength , "wrong size" );
    }
}

function insert(){
    var o = { name : "abc" + numTotal++ };
    o.blob = randString( blobSize );
    t.save( o );
}


