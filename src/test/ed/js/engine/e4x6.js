// MANIPULATING CHILDREN

xml = <actors>
  <actor>
    <name>Jeff</name>
    <born>1930</born>
    <film>Old Yeller</film>
    <film>Zorro</film>
  </actor>
</actors>

//appendChild
xml.appendChild(<singer>Joni Mitchell</singer>);
print( xml.actor.name.appendChild(" York") );

//insertChildAfter
//xml.actor.insertChildAfter(xml.singer, <artist>Renoir</artist>);

//insertChildBefore
//xml.actor.born.insertChildBefore(xml.singer, "lose");
//xml.actor.film[0].insertChildBefore(xml.singer, <some><really><nested>thing</nested></really></some>);

//prependChild
xml.actor.prependChild(<name>me</name>);

//setChildren
xml.actor.film[1].setChildren(<awesome>Zorro</awesome>);

print( xml );

//childIndex
print( xml.singer.childIndex() );
print( xml.actor.born.childIndex() );

//child
print( xml.child("singer") );

//children
print( xml.actor.film[1].children() );
print( xml.actor.children() );