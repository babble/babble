/**
*    Copyright (C) 2008 10gen Inc.
*
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ed.lang.ruby;

import java.util.Set;
import java.util.HashSet;

import org.jruby.*;
import org.jruby.common.IRubyWarnings.ID;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

import ed.db.DBCursor;
import ed.js.engine.Scope;

/**
 * RubyDBCursorWrapper is a RubyArray.
 */
public class RubyDBCursorWrapper extends RubyArray {

    static RubyClass dbCursorClass = null;

    protected Scope _scope;
    protected DBCursor _cursor;

    public static synchronized RubyClass getDBCursorClass(Ruby runtime) {
	if (dbCursorClass == null) {
	    dbCursorClass = runtime.defineClass("DBCursor", runtime.getArray(), ObjectAllocator.NOT_ALLOCATABLE_ALLOCATOR);
	    dbCursorClass.kindOf = new RubyModule.KindOf() {
		    public boolean isKindOf(IRubyObject obj, RubyModule type) {
			return obj instanceof RubyDBCursorWrapper;
		    }
		};
	}
	return dbCursorClass;
    }

    RubyDBCursorWrapper(Scope s, Ruby runtime, DBCursor cursor) {
	super(runtime, getDBCursorClass(runtime));
	if (RubyObjectWrapper.DEBUG)
	    System.err.println("creating RubyDBCursorWrapper");
	_scope = s;
	_cursor = cursor;
	_createMethods();
    }

    private void _createMethods() {
	RubyClass eigenclass = getSingletonClass();
	eigenclass.alias_method(getRuntime().getCurrentContext(), RubyString.newString(getRuntime(), "forEach"), RubyString.newString(getRuntime(), "each"));
	Set<String> alreadyDefined = new HashSet<String>();
	alreadyDefined.add("forEach");
	RubyObjectWrapper.addJavaPublicMethodWrappers(_scope, eigenclass, _cursor, alreadyDefined);
    }

    protected RubyArray notImplemented(String what) {
	throw getRuntime().newNotImplementedError("Can not call " + what + " on database cursors");
    }

    protected IRubyObject _at(long i) {
	return toRuby(_cursor.getInt((int)i));
    }

    // Superclass implementation is OK
//     public IRubyObject initialize(ThreadContext context, IRubyObject[] args, Block block) {
//     }

    // Superclass implementation is OK
//     public IRubyObject initialize_copy(IRubyObject orig) {
//     }

    public IRubyObject replace(IRubyObject orig) {
	return notImplemented("replace");
    }

    public IRubyObject to_s() {
	return RubyString.newString(getRuntime(), _cursor.toString());
    }

    public RubyFixnum hash(ThreadContext context) {
	return getRuntime().newFixnum(_cursor.hashCode());
    }

    private final void concurrentModification() {
        throw getRuntime().newConcurrencyError("Detected invalid array contents due to unsynchronized modifications with concurrent users");
    }

    public IRubyObject fetch(ThreadContext context, IRubyObject arg0, Block block) {
        long index = RubyNumeric.num2long(arg0);

	long realLength = _cursor.length();
        if (index < 0) index += realLength;
        if (index < 0 || index >= realLength) {
            if (block.isGiven()) return block.yield(context, arg0);
            throw getRuntime().newIndexError("index " + index + " out of array");
        }

        try {
            return _at(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            concurrentModification();
            return getRuntime().getNil();
        }
    }

    public IRubyObject fetch(ThreadContext context, IRubyObject arg0, IRubyObject arg1, Block block) {
       if (block.isGiven()) getRuntime().getWarnings().warn(ID.BLOCK_BEATS_DEFAULT_VALUE, "block supersedes default value argument");

       long index = RubyNumeric.num2long(arg0);

	long realLength = _cursor.length();
       if (index < 0) index += realLength;
       if (index < 0 || index >= realLength) {
           if (block.isGiven()) return block.yield(context, arg0);
           return arg1;
       }

       try {
           return _at(index);
       } catch (ArrayIndexOutOfBoundsException e) {
           concurrentModification();
           return getRuntime().getNil();
       }
    }

    public IRubyObject insert(IRubyObject arg) {
	return notImplemented("insert");
    }

    public IRubyObject insert(IRubyObject arg1, IRubyObject arg2) {
	return notImplemented("insert");
    }

    public IRubyObject insert(IRubyObject[] args) {
	return notImplemented("insert");
    }

    public RubyArray transpose() {
	return notImplemented("transpose");
    }

    // Copied from RubyRange and modified
    final long[] begLen(RubyRange range, long len, int err){
        long beg = RubyNumeric.num2long(range.first());
        long end = RubyNumeric.num2long(range.last());

        if (beg < 0) {
            beg += len;
            if (beg < 0) {
                if (err != 0) throw getRuntime().newRangeError("out of range");
                return null;
            }
        }

        if (err == 0 || err == 2) {
            if (beg > len) {
                if (err != 0) throw getRuntime().newRangeError("out of range");
                return null;
            }
            if (end > len) end = len;
        }

        if (end < 0) end += len;
	// HACK ALERT! The only way to figure out if the range is exclusive is
	// to look at the range's hash value. If the hash has bit 24 set, then
	// the range is exclusive.
	boolean isExclusive = ((range.hash().getLongValue() >> 24) & 1) == 1;
        if (!isExclusive) end++;
        len = end - beg;
        if (len < 0) len = 0;

        return new long[]{beg, len};
    }

    public IRubyObject values_at(IRubyObject[] args) {
        RubyArray result = RubyArray.newArray(getRuntime(), args.length);
	int realLength = _cursor.length();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof RubyFixnum) {
                result.append(_at(((RubyFixnum)args[i]).getLongValue()));
                continue;
            }

            long beglen[];
            if (!(args[i] instanceof RubyRange)) {
            } else if ((beglen = begLen((RubyRange)args[i], realLength, 0)) == null) {
                continue;
            } else {
                int beg = (int) beglen[0];
                int len = (int) beglen[1];
                for (int j = 0; j < len; j++)
                    result.append(_at(j + beg));
                continue;
            }
            result.append(_at(RubyNumeric.num2long(args[i])));
        }

        return result;
    }

    public RubyFixnum length() {
	return getRuntime().newFixnum(_cursor.length());
    }

    public RubyArray append(IRubyObject item) {
	return notImplemented("replace");
    }

    public RubyArray push_m(IRubyObject[] items) {
	return notImplemented("replace");
    }

    public IRubyObject pop() {
	return notImplemented("pop");
    }

    public IRubyObject shift() {
	return notImplemented("shift");
    }

    public RubyArray unshift_m(IRubyObject[] items) {
	return notImplemented("unshift");
    }

    public RubyBoolean include_p(ThreadContext context, IRubyObject item) {
	while (_cursor.hasNext())
	    if (equalInternal(context, toRuby(_cursor.next()), item))
		return getRuntime().getTrue();
	return getRuntime().getFalse();
    }

    // Superclass implementation is OK
//     public RubyBoolean frozen_p(ThreadContext context) {
//     }

    public IRubyObject aref(IRubyObject arg0) {
        if (arg0 instanceof RubyFixnum) return _at(((RubyFixnum)arg0).getLongValue());
        if (arg0 instanceof RubySymbol) throw getRuntime().newTypeError("Symbol as array index");

        long[] beglen;
        if (!(arg0 instanceof RubyRange)) {
        } else if ((beglen = begLen((RubyRange) arg0, _cursor.length(), 0)) == null) {
            return getRuntime().getNil();
        } else {
            return subseq(beglen[0], beglen[1]);
        }
        return _at(RubyNumeric.num2long(arg0));
    }

    public IRubyObject aref(IRubyObject arg0, IRubyObject arg1) {
        if (arg0 instanceof RubySymbol) throw getRuntime().newTypeError("Symbol as array index");

        long beg = RubyNumeric.num2long(arg0);
        if (beg < 0) beg += _cursor.length();

        return subseq(beg, RubyNumeric.num2long(arg1));
    }

    public IRubyObject aset(IRubyObject arg0, IRubyObject arg1) {
	return notImplemented("aset");
    }

    public IRubyObject aset(IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
	return notImplemented("aset");
    }

    public IRubyObject at(IRubyObject pos) {
	return _at(RubyNumeric.num2long(pos));
    }

    public RubyArray concat(IRubyObject obj) {
	return notImplemented("concat");
    }

    private IRubyObject inspectAry(ThreadContext context) {
        ByteList buffer = new ByteList();
        buffer.append('[');
        boolean tainted = isTaint();

	int realLength = _cursor.length();
        for (int i = 0; i < realLength; i++) {
            if (i > 0) buffer.append(',').append(' ');

            RubyString str = RubyString.newString(getRuntime(), _at(i).toString());
            if (str.isTaint()) tainted = true;
            buffer.append(str.getByteList());
        }
        buffer.append(']');

        RubyString str = getRuntime().newString(buffer);
        if (tainted) str.setTaint(true);

        return str;
    }

    public IRubyObject inspect() {
	int realLength = _cursor.length();
        if (realLength == 0) return getRuntime().newString("[]");
        if (getRuntime().isInspecting(this)) return  getRuntime().newString("[...]");

        try {
            getRuntime().registerInspecting(this);
            return inspectAry(getRuntime().getCurrentContext());
        } finally {
            getRuntime().unregisterInspecting(this);
        }
    }

    public IRubyObject first() {
	if (_cursor.length() == 0) return getRuntime().getNil();
	return _at(0);
    }

    public IRubyObject first(IRubyObject arg0) {
        long n = RubyNumeric.num2long(arg0);
	long realLength = _cursor.length();
        if (n > realLength) {
            n = realLength;
        } else if (n < 0) {
            throw getRuntime().newArgumentError("negative array size (or size too big)");
        }
	return subseq(0, n);
    }

    public IRubyObject last() {
	long realLength = _cursor.length();
        if (realLength == 0) return getRuntime().getNil();
        return _at(realLength - 1);
    }

    public IRubyObject last(IRubyObject arg0) {
        long n = RubyNumeric.num2long(arg0);
	int realLength = _cursor.length();
        if (n > realLength) {
            n = realLength;
        } else if (n < 0) {
            throw getRuntime().newArgumentError("negative array size (or size too big)");
        }

        return subseq(realLength - n, n);
    }

    public IRubyObject each(ThreadContext context, Block block) {
	while (_cursor.hasNext())
	    block.yield(context, toRuby(_cursor.next()));
	return this;
    }

    public IRubyObject each_index(ThreadContext context, Block block) {
        Ruby runtime = getRuntime();
	int realLength = _cursor.length();
        for (int i = 0; i < realLength; i++)
            block.yield(context, runtime.newFixnum(i));
	return this;
    }

    public IRubyObject reverse_each(ThreadContext context, Block block) {
	int realLength = _cursor.length();
        int len = realLength;
        while (len-- > 0) {
            block.yield(context, _at(len));
            if (realLength < len) len = realLength;
        }
        return this;
    }

    private IRubyObject inspectJoin(ThreadContext context, RubyArray tmp, IRubyObject sep) {
        Ruby runtime = getRuntime();

        // If already inspecting, there is no need to register/unregister again.
        if (runtime.isInspecting(this)) {
            return tmp.join(context, sep);
        }

        try {
            runtime.registerInspecting(this);
            return tmp.join(context, sep);
        } finally {
            runtime.unregisterInspecting(this);
        }
    }

    public RubyString join(ThreadContext context, IRubyObject sep) {
        final Ruby runtime = getRuntime();

	int realLength = _cursor.length();
        if (realLength == 0) return RubyString.newEmptyString(getRuntime());

        boolean taint = isTaint() || sep.isTaint();

        RubyString strSep = null;
        if (!sep.isNil())
            sep = strSep = sep.convertToString();

        ByteList buf = new ByteList();
        for (int i = 0; i < realLength; i++) {
            IRubyObject tmp;
            try {
                tmp = _at(i);
            } catch (ArrayIndexOutOfBoundsException e) {
                concurrentModification();
                return runtime.newString("");
            }
            if (tmp instanceof RubyString) {
                // do nothing
            } else if (tmp instanceof RubyArray) {
                if (runtime.isInspecting(tmp)) {
                    tmp = runtime.newString("[...]");
                } else {
                    tmp = inspectJoin(context, (RubyArray)tmp, sep);
                }
            } else {
                tmp = RubyString.objAsString(context, tmp);
            }

            if (i > 0 && !sep.isNil()) buf.append(strSep.getByteList());

            buf.append(tmp.asString().getByteList());
            if (tmp.isTaint()) taint = true;
        }

        RubyString result = runtime.newString(buf);

        if (taint) result.setTaint(true);

        return result;
    }

    public RubyString join_m(ThreadContext context, IRubyObject[] args) {
        int argc = args.length;
        IRubyObject sep = (argc == 1) ? args[0] : getRuntime().getGlobalVariables().get("$,");
        return join(context, sep);
    }

    // Superclass implementation is OK
//     public RubyArray to_a() {
//     }

    // Superclass implementation is OK
//     public IRubyObject to_ary() {
//     }

    public IRubyObject op_equal(ThreadContext context, IRubyObject obj) {
        if (this == obj) return getRuntime().getTrue();

        if (!(obj instanceof RubyArray)) {
            if (!obj.respondsTo("to_ary")) {
                return getRuntime().getFalse();
	    } else {
                if (equalInternal(context, obj.callMethod(context, "to_ary"), this)) return getRuntime().getTrue();
                return getRuntime().getFalse();
            }
        }

        RubyArray ary = (RubyArray) obj;
	long realLength = _cursor.length();
        if (realLength != ary.size()) return getRuntime().getFalse();

        Ruby runtime = getRuntime();
        for (long i = 0; i < realLength; i++) {
            if (!equalInternal(context, _at(i), ary.at(getRuntime().newFixnum(i)))) return runtime.getFalse();
        }
        return runtime.getTrue();
    }

    public RubyBoolean eql_p(ThreadContext context, IRubyObject obj) {
        if (this == obj) return getRuntime().getTrue();
        if (!(obj instanceof RubyArray)) return getRuntime().getFalse();

        RubyArray ary = (RubyArray) obj;

	long realLength = _cursor.length();
        if (realLength != ary.size()) return getRuntime().getFalse();

        Ruby runtime = getRuntime();
        for (int i = 0; i < realLength; i++)
            if (!eqlInternal(context, _at(i), ary.at(getRuntime().newFixnum(i)))) return runtime.getFalse();
        return runtime.getTrue();
    }

    public IRubyObject compact_bang() {
	return getRuntime().getNil();
    }

    public IRubyObject compact() {
	return getRuntime().getNil();
    }

    public IRubyObject empty_p() {
        return _cursor.length() == 0 ? getRuntime().getTrue() : getRuntime().getFalse();
    }

    public IRubyObject rb_clear() {
	return notImplemented("rb_clear");
    }

    public IRubyObject fill(ThreadContext context, IRubyObject[] args, Block block) {
	return notImplemented("fill");
    }

    public IRubyObject index(ThreadContext context, IRubyObject obj) {
        Ruby runtime = getRuntime();
	int realLength = _cursor.length();
        for (int i = 0; i < realLength; i++) {
            if (equalInternal(context, _at(i), obj)) return runtime.newFixnum(i);
        }

        return runtime.getNil();
    }

    public IRubyObject rindex(ThreadContext context, IRubyObject obj) {
        Ruby runtime = getRuntime();
	int realLength = _cursor.length();
        int i = realLength;

        while (i-- > 0) {
            if (i > realLength) {
                i = realLength;
                continue;
            }
            if (equalInternal(context, _at(i), obj)) return getRuntime().newFixnum(i);
        }

        return runtime.getNil();
    }

    // Superclass implementation is OK
//     public IRubyObject indexes(IRubyObject[] args) {
//     }

    public IRubyObject reverse_bang() {
	return notImplemented("reverse!");
    }

    public IRubyObject reverse() {
	return notImplemented("reverse");
    }

    public RubyArray collect(ThreadContext context, Block block) {
        Ruby runtime = getRuntime();
	int realLength = _cursor.length();

        if (!block.isGiven()) {
	    RubyArray a = RubyArray.newArray(runtime);
	    for (int i = 0; i < realLength; ++i)
		a.append(_at(i));
	    return a;
	}

        RubyArray collect = RubyArray.newArray(runtime);
        for (int i = 0; i < realLength; i++)
            collect.append(block.yield(context, _at(i)));
        return collect;
    }

    protected RubyArray toRubyArray() {
	return collect(getRuntime().getCurrentContext(), Block.NULL_BLOCK);
    }

    public RubyArray collect_bang(ThreadContext context, Block block) {
	return notImplemented("collect!");
    }

    public RubyArray select(ThreadContext context, Block block) {
        RubyArray result = RubyArray.newArray(getRuntime());
	int realLength = _cursor.length();
	for (int i = 0; i < realLength; i++) {
	    IRubyObject o = _at(i);
	    if (block.yield(context, o).isTrue()) result.append(o);
	}
        return result;
    }

    public IRubyObject delete(ThreadContext context, IRubyObject item, Block block) {
	return notImplemented("delete");
    }

    public IRubyObject delete_at(IRubyObject obj) {
	return notImplemented("delete_at");
    }

    public IRubyObject reject(ThreadContext context, Block block) {
        RubyArray result = RubyArray.newArray(getRuntime());
	int realLength = _cursor.length();
	for (int i = 0; i < realLength; i++) {
	    IRubyObject o = _at(i);
	    if (!block.yield(context, o).isTrue()) result.append(o);
	}
        return result;
    }

    public IRubyObject reject_bang(ThreadContext context, Block block) {
	return notImplemented("reject!");
    }

    public IRubyObject delete_if(ThreadContext context, Block block) {
	return notImplemented("delete_if");
    }

    public IRubyObject zip(ThreadContext context, IRubyObject[] args, Block block) {
        for (int i = 0; i < args.length; i++)
            args[i] = args[i].convertToArray();

        Ruby runtime = getRuntime();
        int len = _cursor.length();
        if (block.isGiven()) {
	    long realLength = _cursor.length();
            for (int i = 0; i < realLength; i++) {
                RubyArray tmp = RubyArray.newArray(runtime);
                tmp.append(_at(i));
                for (int j = 0; j < args.length; j++)
                    tmp.append(((RubyArray) args[j]).aref(getRuntime().newFixnum(i)));
                block.yield(context, tmp);
            }
            return runtime.getNil();
        }

        RubyArray result = RubyArray.newArray(runtime);
        for (int i = 0; i < len; i++) {
            RubyArray tmp = RubyArray.newArray(runtime);
            tmp.append(_at(i));
            for (int j = 0; j < args.length; j++)
                tmp.append(((RubyArray) args[j]).aref(getRuntime().newFixnum(i)));
            result.append(tmp);
        }
        return result;
    }

    public IRubyObject op_cmp(ThreadContext context, IRubyObject obj) {
        RubyArray ary2 = obj.convertToArray();

	int realLength = _cursor.length();
        long len = realLength;

	long ary2RealLength = ary2.size();
        if (len > ary2RealLength) len = ary2RealLength;

        Ruby runtime = getRuntime();
        for (int i = 0; i < len; i++) {
            IRubyObject v = _at(i).callMethod(context, MethodIndex.OP_SPACESHIP, "<=>", ary2.aref(getRuntime().newFixnum(i)));
            if (!(v instanceof RubyFixnum) || ((RubyFixnum) v).getLongValue() != 0) return v;
        }
        len = realLength - ary2RealLength;

        if (len == 0) return RubyFixnum.zero(runtime);
        if (len > 0) return RubyFixnum.one(runtime);

        return RubyFixnum.minus_one(runtime);
    }

    public IRubyObject slice_bang(IRubyObject arg0) {
	return notImplemented("slice!");
    }

    public IRubyObject slice_bang(IRubyObject arg0, IRubyObject arg1) {
	return notImplemented("slice!");
    }

    public IRubyObject assoc(ThreadContext context, IRubyObject key) {
	return notImplemented("assoc");
    }

    public IRubyObject rassoc(ThreadContext context, IRubyObject value) {
	return notImplemented("rassoc");
    }

    public IRubyObject flatten_bang(ThreadContext context) {
	return notImplemented("flatten!");
    }

    public IRubyObject flatten(ThreadContext context) {
	return this;
    }

    public IRubyObject nitems() {
	return length();	// db cursor rows are never nil
    }

    public IRubyObject op_plus(IRubyObject obj) {
        RubyArray y = obj.convertToArray();
        RubyArray z = toRubyArray();
        try {
	    long yRealLength = y.size();
	    for (int i = 0; i < yRealLength; ++i)
		z.append(y.at(getRuntime().newFixnum(i)));
        } catch (ArrayIndexOutOfBoundsException e) {
            concurrentModification();
        }
        return z;
    }

    public IRubyObject op_times(ThreadContext context, IRubyObject times) {
	RubyArray me = toRubyArray();
	return me.op_times(context, times);
    }

    public IRubyObject uniq_bang() {
	return this;
    }

    public IRubyObject uniq() {
	return this;
    }

    public IRubyObject op_diff(IRubyObject other) {
	return toRubyArray().op_diff(other);
    }

    public IRubyObject op_and(IRubyObject other) {
	return toRubyArray().op_and(other);
    }

    public IRubyObject op_or(IRubyObject other) {
	return toRubyArray().op_or(other);
    }

    public RubyArray sort(Block block) {
	return toRubyArray().sort(block);
    }

    public RubyArray sort_bang(Block block) {
	return notImplemented("sort!");
    }

    public RubyString pack(ThreadContext context, IRubyObject obj) {
	return toRubyArray().pack(context, obj);
    }

    public IRubyObject subseq(long beg, long len) {
	int realLength = _cursor.length();
        if (beg > realLength || beg < 0 || len < 0) return getRuntime().getNil();

        if (beg + len > realLength) {
            len = realLength - beg;

            if (len < 0) len = 0;
        }

        if (len == 0) return RubyArray.newArray(getRuntime());

	RubyArray a = RubyArray.newArray(getRuntime());
	for (long i = beg; i < beg + len; ++i)
	    a.append(_at(i));
	return a;
    }

    public IRubyObject subseqLight(long beg, long len) {
	return subseq(beg, len);
    }

    protected IRubyObject toRuby(Object o) { return RubyObjectWrapper.toRuby(_scope, getRuntime(), o); }

    protected Object toJS(IRubyObject o) { return RubyObjectWrapper.toJS(_scope, o); }
}
