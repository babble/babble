all = [];

r = /(\d)/g

for ( var i=0; i<100; i++ )
    all[i] = fork( 
        function(){
            for ( var j=0; j<100; j++ ){
                var num = Math.floor( Math.random() * 100000 ) + 100;
                var s = "_" + num;
                var z = r.exec( s );

                var howMany = 0;
                
                while ( z ){
                    howMany++;
                    assert.eq( z[1] , s[howMany] );
                    z = r.exec( s );
                }
                
                assert.eq( s.length , howMany + 1 );
            }
        }
    );

for each ( f in all ){
    f.start();
}

for each ( f in all ){
    f.join();
}

