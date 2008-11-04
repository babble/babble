db=connect("test");
t=db.embedded;
t.drop();

t.save({ x : [ {y:2}, {y:3} ] , q : 1});

t.save({ q : 2, x : [ {y:5}, {a:1, y:7}, {z:99} ] } );

for( pass = 0; pass < 2; pass++ ) { 
    print("pass:" + pass);
    assert( t.findOne({"x.y":3}).q == 1 );
    assert( t.findOne({"x.y":7}).q == 2 );
    t.ensureIndex({"x.y":1});
    /* problem here.  void IndexDetails::getKeysFromObject(BSONObj& obj, set<BSONObj>& keys) is not smart enough. */
    // remove this when fixed:
    break;
}

assert( t.validate().valid );
