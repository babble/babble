function validate() {
	bs = db.a.find().sort( { b: 1 } );
	assert.eq( 2, bs.count() );
	assert.eq( 1, bs[ 0 ].b );
	assert.eq( 2, bs[ 1 ].b );

	assert.eq( 1, db.system.indexes.find().count() );
	ys = db.x.find().sort( { y: 1 } );
	assert.eq( 2, ys.count() );
	assert.eq( 1, ys[ 0 ].y );
	assert.eq( 2, ys[ 1 ].y );
}

db = connect( "test.ed.db.repairDatabase.js" );
db.a.drop();
db.x.drop();

db.a.save( { b: 1 } );
db.a.save( { b: 2 } );
db.x.save( { y: 1 } );
db.x.save( { y: 2 } );
db.x.ensureIndex( { y: 1 } );

validate();

assert.eq( 1, db.repairDatabase().ok );
validate();

// Try special debug modes:

repairDatabaseBackup = function() {
	return db._dbCommand( { repairDatabase: 1, backupOriginalFiles: true } );
}
assert.eq( 1, repairDatabaseBackup().ok );
validate();

repairDatabasePreserve = function() {
	return db._dbCommand( { repairDatabase: 1, preserveClonedFilesOnFailure: true } );
}
assert.eq( 1, repairDatabasePreserve().ok );
validate();
