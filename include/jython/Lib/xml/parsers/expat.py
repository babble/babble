# coding: utf-8

#------------------------------------------------------------------------------
# Copyright (c) 2008 Sébastien Boisgérault
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
# -----------------------------------------------------------------------------

# Jython check
import sys
if not sys.platform.startswith('java'):
    raise ImportError("this version of expat requires the jython interpreter")

# Java Standard Edition
import java.io
from java.lang import String, StringBuilder
import org.xml.sax
import org.xml.sax.helpers

def ParserCreate(encoding=None, namespace_separator=None):
    return XMLParser(encoding, namespace_separator)

class XMLParser(object):
    def __init__(self, encoding, namespace_separator):
        self.encoding = encoding
        self.namespace_separator = namespace_separator

        self._buffer_text = True
        self._returns_unicode = True

        self._data = StringBuilder()

        self.DefaultHandlerExpand = None
        self.StartElementHandler = None
        self.EndElementHandler = None
        self.CharacterDataHandler = None
        self.StartNamespaceDeclHandler = None
        self.EndNamespaceDeclHandler = None

        XMLReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader
        self._reader = XMLReader()
        if not namespace_separator:
            try:
                namespace_support = "http://xml.org/sax/features/namespaces"
                self._reader.setFeature(namespace_support, False)
            except org.xml.sax.SAXNotRecognizedException:
                error  = "namespace support cannot be disabled; "
                error += "set namespace_separator to a string of length 1."
                raise ValueError(error)

        self._handler = XMLEventHandler(self)
        self._reader.setContentHandler(self._handler)
        self._reader.setErrorHandler(self._handler)

    def _error(self, value=None):
        raise AttributeError("'XMLParser' has no such attribute")

    def _get_buffer_text(self):
        return self._buffer_text

    def _set_buffer_text(self, value):
        self._buffer_text = bool(value)

    def _get_returns_unicode(self):
        return bool(self._returns_unicode)

    def _set_returns_unicode(self, value):
        self._returns_unicode = value

    # 'ordered' and 'specified' attributes are not supported
    ordered_attributes = property(_error, _error)
    specified_attributes = property(_error, _error)
    # any setting is allowed, but it won't make a difference
    buffer_text = property(_get_buffer_text, _set_buffer_text)
    # non-significant read-only values
    buffer_used = property(lambda self: None)
    buffer_size = property(lambda self: None)
    # 'returns_unicode' attribute is properly supported
    returns_unicode = property(_get_returns_unicode, _set_returns_unicode)

    def Parse(self, data, isfinal=False):
        # The 'data' argument should be an encoded text: a str instance that
        # represents an array of bytes. If instead it is a unicode string,
        # only the us-ascii range is considered safe enough to be silently
        # converted.
        if isinstance(data, unicode):
            data = data.encode("US-ASCII")

        self._data.append(data)

        if isfinal:
            # converts a Python string used as a sequence of bytes to a Java
            # byte array ; similar to PyString toBytes method implementation.
            data = String(self._data.toString())
            bytes = data.getBytes("ISO-8859-1")

            byte_stream = java.io.ByteArrayInputStream(bytes)
            source = org.xml.sax.InputSource(byte_stream)
            if self.encoding is not None:
                source.setEncoding(self.encoding)
            try:
                self._reader.parse(source)
            except org.xml.sax.SAXParseException, sax_error:
                error = ExpatError(sax_error.getMessage())
                error.lineno = sax_error.lineNumber
                error.offset = sax_error.columnNumber-1
                error.code = None
                raise error

XMLParserType = XMLParser

def _encode(arg, encoding):
    try:
        return arg.encode(encoding)
    except AttributeError:
        _type = type(arg)
        if issubclass(_type, dict):
            iterator = arg.iteritems()
        else:
            iterator = iter(arg)
        return _type(_encode(_arg, encoding) for _arg in iterator)

def expat(callback):
    def _expat(method):
        def new_method(*args):
            self = args[0]
            parser = self.parser
            _callback = getattr(parser, callback)
            if _callback is not None:
                results = method(*args)
                if not isinstance(results, tuple):
                    results = (results,)
                if not parser.returns_unicode:
                    results = _encode(results, "utf-8")
                _callback(*results)
        new_method.__name__ = method.__name__
        new_method.__doc__ = method.__doc__
        return new_method
    return _expat

class XMLEventHandler(org.xml.sax.helpers.DefaultHandler):
    def __init__(self, parser):
        self.parser = parser

    def _qualify(self, local_name, namespace=None):
        namespace_separator = self.parser.namespace_separator
        if not namespace_separator or not namespace:
            return local_name
        else:
            return namespace + namespace_separator + local_name

    # TODO: what about the expat 'DefaultHandlerExpand' callback that is
    #       currently never called ? It it used in ElementTree for undefined
    #       entities and doctype processing ... is it tested ? Yes, two errors
    #       in ElementTree 1.3 test suite are directly related to it.
    #       UPDATE: check this, I am not sure anymore ...

    @expat("StartElementHandler")
    def startElement(self, namespace, local_name, qname, attributes):
        tag = self._qualify(local_name, namespace)
        attribs = {}
        length = attributes.getLength()
        for index in range(length):
            name = attributes.getLocalName(index)
            namespace = attributes.getURI(index)
            name = self._qualify(name, namespace)
            value = attributes.getValue(index)
            attribs[name] = value
        return tag, attribs

    @expat("EndElementHandler")
    def endElement(self, namespace, local_name, qualified_name):
        return self._qualify(local_name, namespace)

    @expat("CharacterDataHandler")
    def characters(self, characters, start, length):
        # converts a char[] slice to a PyUnicode instance
        text = String(characters[start:start+length]).substring(0)
        return text

    @expat("StartNamespaceDeclHandler")
    def startPrefixMapping(self, prefix, uri):
        return prefix, uri

    @expat("EndNamespaceDeclHandler")
    def endPrefixMapping(self, prefix):
        return prefix

class ExpatError(Exception):
    pass

error = ExpatError
