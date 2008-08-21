var a = {
    uri: 2
};

with (a){
    uri = null;
};

print(a.uri);
