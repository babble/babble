/*
 *   simple test that uses two collections, blob1 and blob2, and on each iteration,
 *   reads all the blobs out of one collection, deletes the id, and then saves the
 *   objects in the second collection.  It then repeats it in reverse so that things
 *   grow geometrically
 *
 *   Results follow for varying size of array
 *
 *           r0.0.4      v0.0.2
 *  64   ->  970/sec      950
 *  128  ->  384/sec      390
 *  256  ->  130/sec      134
 *  512  ->  40/sec       40
 *  1024 ->  10/sec       11
 *  2048 ->  3/sec         3
 */

core.db.db();

var blobSize = 2048;

function slop( coll1, coll2 ) {
	var c = db[coll1].find();   
	c.forEach(function(x) { delete x._id; db[coll2].save(x);});  
};

function go(x) {
	for(i = 0; i < x; i++) { 
                print(" =================================================================================== ");
		var startCount = count("blob1") + count("blob2");
		var startTime = new Date();
		print("Start : " + startTime + " : " + startCount + " blobs");
		slop("blob1", "blob2");
		slop("blob2", "blob1");
		var endTime = new Date();
		var endCount = count("blob1") + count("blob2");
		var timeDiff = endTime - startTime;
		print("End : " + endTime + " : " + endCount + " blobs");
		var countDiff = endCount - startCount;
		print("Total time (ms) : " + timeDiff + " num blobs : " + countDiff +  " : blobs/sec " + (countDiff / timeDiff * 1000));
	}
};

var db = connect("big_test");

db.blob1.remove({});
db.blob2.remove({});

var obj = new Array(blobSize);
obj.forEach( function(x, i) { obj[i] = i;});

db.blob1.save(obj);

go(20);
