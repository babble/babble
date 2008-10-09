/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end, using $lt
 */

db = connect( "ed_perf_find_w_range_sort1" );
var t = db.sort2;
t.drop();

var size = 500000;

for (var i=0; i<size; i++){
    t.save({ num : i });
    if (i == 0 )
        t.ensureIndex( { num : 1 } );
}

var calls = 100;

var results = {};
var rnd = core.util.random();
var myRandom = rnd.getRandom(seed);


results.ltAndLimit = Date.timeFunc(
    function(){
        assert(t.find({num: {$gte : myRandom.nextInt(size-11)}}).sort({num: 1}).limit(10).toArray().length == 10);
    } , calls );

results.useRange = Date.timeFunc(
    function(){
        var n = myRandom.nextInt(size-11);
        assert(t.find({num: {$gte : n, $lt : n+10}}).sort({num: 1}).toArray().length == 10);
    } , calls );


//  Uncomment once 927 is fixed

//assert(   0.75 < (results.ltAndLimit / results.useRange) > 1.25,
//        "first / last (" + results.ltAndLimit + " / " + results.useRange + " ) = " +
//        results.ltAndLimit /  results.useRange + " not in [0.75, 1.25]" );
