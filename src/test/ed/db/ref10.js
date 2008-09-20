
db = connect("test");
ta = db.ref10a;
tb = db.ref10b;
ta.drop();
tb.drop();

var o1 = { attr: 'thing' };
ta.save(o1);

var o2 = { dbref: o1 };
tb.save(o2);

ta.remove( {} );

assert( tb.findOne().dbref == null );

