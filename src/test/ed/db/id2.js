db = connect("test");
t = db.id2;
t.drop();

t.save({name: "Jim"});
t.save({name: "Erica"});
t.save({name: "Nana", age: 102});

e = t.findOne({name: "Erica"});
eid = e._id;
eid_str = eid.toString();

e2 = t.findOne({_id: eid});
assert(e2.name == e.name);

e3 = t.findOne(eid_str);
assert(e3.name == e.name);
