// simple1.js
// basic replication test


__path__._repl( 
    function( master , slave ){
	mt = master.test;
	st = slave.test;

	mt.save( { a : 1 } );
	assert( mt.findOne() );
	assert( mt.findOne().a == 1 );
	assert.eq( mt.count() , 1 );
	
	sleep( 20000 );
	assert( st.findOne() , "nothing in slave table" );
	assert( st.findOne().a == 1 , "what's in slave table is wrong" );
	assert( st.count() == 1 , "why are there more than 1 thing in slave" );
    }
);


