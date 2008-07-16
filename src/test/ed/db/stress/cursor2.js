
db = connect( "ed_db_stress_cursor2" );
t = db.cursor2;

t.clean();
t.drop();

var total = 20000;

for ( i=0; i<total; i++ ){
    t.save( 
        { 
            name : "asdhasjlkdhakshdflskadghflksdhfkljasdhfs" + i ,
            when : new Date() ,
            blah : "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" + 
                "lksjhasoh1298alshasoidiohaskjasiouashoasasiugoas" ,
            sub : []
        }
    );
}

t.ensureIndex( { name : 1 } );
t.ensureIndex( { sub : 1 } );
t.ensureIndex( { blah : 1 } );

t.findOne();

print( "READY" );

going = true;

function testCursor( cursor ){
    var num = 0;
    
    cursor.forEach(
        function(z){
            num++;
        }
    );
    
    if ( num != total ){
        print( "X : " + num + " should be (" + total + ") validated : " + t.validate().valid );
        if ( total - num < 10 )
            return;
        
        going = false;
        assert( 0 );
    }
    
}

function reader(){
    while ( going ){
        
        testCursor( t.find() );
        testCursor( t.find().sort( { name : 1 } ) );
        testCursor( t.find().sort( { sub : -1 } ) );
        testCursor( t.find().sort( { blah : 1 } ) );

        assert( t.validate().valid );
    }
}


function writer(){
    while( going ){
        var thing = t.find().skip( Math.random() * total ).limit(1);
        
        if ( ! thing.hasNext() ){
            print( "?" );
            continue;
        }
        
        thing = thing.next();

        thing.name = thing.name + "asdasdsD";
        thing.sub.push( "asdhasoid"  + Date()  + thing.name );
        
        t.save( thing );
    }
}

for ( i=0; i<3; i++ ){
    fork( reader ).start();
    fork( writer ).start();
}

sleep( 1000 * 60 * 15 );

going = false;

    
    
