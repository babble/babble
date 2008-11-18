adapterSelector = function(uri) {

    if (uri == "/cgi.py") {
        return "CGI";
    }

    if (uri == "/wsgi.py") {
        return "WSGI";
    }

    if (uri == "/direct.js") {
        return "DIRECT_10GEN";
    }

    return "DIRECT_10GEN";
}