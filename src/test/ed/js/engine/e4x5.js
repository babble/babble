// set tests

var french = <france><food>fromage</food><food>haricots verts</food><language>francais</language></france>

french.capital = "paris";

french.food[-1] = "mal";
french.food[0] = "pain";
french.food[20] = "pomme";

french.language.accent = "parisian";

print(french);

french.food = "fromage"

french.places = <books><usr>livre</usr><bin>elles</bin></books>

print(french);
