
db = connect( "test" );
t = db.cursor1;

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

function reader(){
    while ( going ){
        
        var num = 0;
        
        t.find().forEach(
            function(z){
                num++;
            }
        );
        
        if ( num != total ){
            print( "X : " + num + tojson( t.validate() ) );
            if ( total - num < 10 ) 
                continue;
            
            going = false;
            assert( 0 );
        }

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

for ( i=0; i<2; i++ ){
    fork( reader ).start();
    fork( writer ).start();
}

sleep( 1000 * 60 * 15 );

going = false;

    
    
