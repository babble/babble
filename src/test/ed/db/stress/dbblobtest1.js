/*
 * Simple test that injects large blobs into the db, in batches of 10000,
 * performing a count after each batch as a check.
 * 
 * Don't let this run by itself - it will run forever
 */
function createStringBlob() { 
    var s = "a";
    for (i=0; i<18; i++) {
        s = s + s; 
    }

    return s;
}

function saveSome(coll, start, n, o) {
    for (i=start; i < start + n; i++) { 
	o.n = i;
	db[coll].save(o);
        o._id = null;
        if (i % 100 == 0) { print ("--> " + i);  }
    }

    return i;
}

db = connect("blob");
collection = "blobtest1";
db[collection].ensureIndex({n:1});

blob = createStringBlob();
print("using blob of " +  2 * blob.length  + " bytes.");
obj = { n: 0, blob : blob };

var count = db[collection].count();

print("Current count = " + count);

while(1) { 
    var next = saveSome(collection, count, 10000, obj);
    var count = db[collection].count();

    if (count != next) { 
	print("ERROR : count should be " + next + " : current count = " + count);
	break;
    }
}

