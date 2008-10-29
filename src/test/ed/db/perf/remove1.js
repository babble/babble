/**
 *  Performance tests for removing ojects
 */

var removals = 100;
var rnd = core.util.random();
var myRandom = rnd.getRandom(seed);
var size = 50000;
var collection_name = "remove_test";
var msg = "Hello from remove test";

function testSetup(dbConn) {
    var t = dbConn[collection_name];
    t.drop();
    t.ensureIndex( { num : 1 } );

    for (var i=0; i<size; i++){
        t.save({ num : i, msg : msg });
    }
}

/**
 *   Compares difference of removing objects from a collection if only includes
 *   field that's indexed, vs w/ additional other fields
 *
 * @param dbConn
 */
function testRemoveWithMultiField(dbConn) {

    // remove once XXX fixed
    print("ed/db/remove1.js:testRemoveWithMultiField() - not run - BUG XXX");
    return;

    var results = {};
    var t = dbConn[collection_name];

    testSetup(dbConn);
    
    results.indexOnly = Date.timeFunc(
        function(){
            for (var i = 0; i < removals; i++) {
                t.remove({num : i});
            }

            t.findOne();
        }
    );

    testSetup(dbConn);
    
    results.withAnother = Date.timeFunc(
        function(){
            for (var i = 0; i < removals; i++) {
                t.remove({num : i, msg : msg});
            }

            t.findOne();
        }
    );


     assert(   0.90 < (results.indexOnly / results.withAnother) > 1.10,
            "indexOnly / withAnother (" + results.indexOnly + " / " + results.withAnother + " ) = " +
            results.indexOnly /  results.withAnother + " not in [0.90, 1.10]" );
}

var db = connect( "ed_perf_remove1" );

testRemoveWithMultiField(db);
