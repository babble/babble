#--
#   finalizer.rb - 
#   	$Release Version: 0.3$
#   	$Revision: 6204 $
#   	$Date: 2008-03-15 18:37:00 -0500 (Sat, 15 Mar 2008) $
#   	by Keiju ISHITSUKA
#++
#
# Usage:
#
# add dependency R_method(obj, dependant)
#   add(obj, dependant, method = :finalize, *opt)
#   add_dependency(obj, dependant, method = :finalize, *opt)
#
# delete dependency R_method(obj, dependant)
#   delete(obj_or_id, dependant, method = :finalize)
#   delete_dependency(obj_or_id, dependant, method = :finalize)
#
# delete dependency R_*(obj, dependant)
#   delete_all_dependency(obj_or_id, dependant)
#
# delete dependency R_method(*, dependant)
#   delete_by_dependant(dependant, method = :finalize)
#
# delete dependency R_*(*, dependant)
#   delete_all_by_dependant(dependant)
#
# delete all dependency R_*(*, *)
#   delete_all
#
# finalize the dependant connected by dependency R_method(obj, dependtant).
#   finalize(obj_or_id, dependant, method = :finalize)
#   finalize_dependency(obj_or_id, dependant, method = :finalize)
#
# finalize all dependants connected by dependency R_*(obj, dependtant).
#   finalize_all_dependency(obj_or_id, dependant)
#
# finalize the dependant connected by dependency R_method(*, dependtant).
#   finalize_by_dependant(dependant, method = :finalize)
#
# finalize all dependants connected by dependency R_*(*, dependant).
#   finalize_all_by_dependant(dependant)
#
# finalize all dependency registered to the Finalizer.
#   finalize_all
#
# stop invoking Finalizer on GC.
#   safe{..}
#

require 'monitor'

module Finalizer
  RCS_ID='-$Id: finalize.rb 6204 2008-03-15 23:37:00Z mental $-'

  class <<self
    # @dependency: {id => [[dependant, method, *opt], ...], ...}

    # add dependency R_method(obj, dependant)
    def add_dependency(obj, dependant, method = :finalize, *opt)
      ObjectSpace.call_finalizer(obj)
      method = method.intern unless method.kind_of?(Integer)
      assoc = [dependant, method].concat(opt)
      if dep = @dependency[obj.object_id]
	dep.push assoc
      else
	@dependency[obj.object_id] = [assoc]
      end
    end
    alias add add_dependency

    # delete dependency R_method(obj, dependant)
    def delete_dependency(id, dependant, method = :finalize)
      id = id.object_id unless id.kind_of?(Integer)
      method = method.intern unless method.kind_of?(Integer)
      for assoc in @dependency[id]
	assoc.delete_if do
	  |d, m, *o|
	  d == dependant && m == method
	end
	@dependency.delete(id) if assoc.empty?
      end
    end
    alias delete delete_dependency

    # delete dependency R_*(obj, dependant)
    def delete_all_dependency(id, dependant)
      id = id.object_id unless id.kind_of?(Integer)
      method = method.intern unless method.kind_of?(Integer)
      for assoc in @dependency[id]
	assoc.delete_if do
	  |d, m, *o|
	  d == dependant
	end
	@dependency.delete(id) if assoc.empty?
      end
    end

    # delete dependency R_method(*, dependant)
    def delete_by_dependant(dependant, method = :finalize)
      method = method.intern unless method.kind_of?(Integer)
      for id in @dependency.keys
	delete(id, dependant, method)
      end
    end

    # delete dependency R_*(*, dependant)
    def delete_all_by_dependant(dependant)
      for id in @dependency.keys
	delete_all_dependency(id, dependant)
      end
    end

    # finalize the depandant connected by dependency R_method(obj, dependtant)
    def finalize_dependency(id, dependant, method = :finalize)
      id = id.object_id unless id.kind_of?(Integer)
      method = method.intern unless method.kind_of?(Integer)
      for assocs in @dependency[id]
	assocs.delete_if do
	  |d, m, *o|
	  d.send(m, id, *o) if ret = d == dependant && m == method
	  ret
	end
	@dependency.delete(id) if assoc.empty?
      end
    end
    alias finalize finalize_dependency

    # finalize all dependants connected by dependency R_*(obj, dependtant)
    def finalize_all_dependency(id, dependant)
      id = id.object_id unless id.kind_of?(Integer)
      method = method.intern unless method.kind_of?(Integer)
      for assoc in @dependency[id]
	assoc.delete_if do
	  |d, m, *o|
	  d.send(m, id, *o) if ret = d == dependant
	end
	@dependency.delete(id) if assoc.empty?
      end
    end

    # finalize the dependant connected by dependency R_method(*, dependtant)
    def finalize_by_dependant(dependant, method = :finalize)
      method = method.intern unless method.kind_of?(Integer)
      for id in @dependency.keys
	finalize(id, dependant, method)
      end
    end

    # finalize all dependants connected by dependency R_*(*, dependtant)
    def finalize_all_by_dependant(dependant)
      for id in @dependency.keys
	finalize_all_dependency(id, dependant)
      end
    end

    # finalize all dependants registered to the Finalizer.
    def finalize_all
      for id, assocs in @dependency
	for dependant, method, *opt in assocs
	  dependant.send(method, id, *opt)
	end
	assocs.clear
      end
    end

    # method to call finalize_* safely.
    def safe
      # Monitor, since this may need to be reentrant
      @monitor.synchronize do
        ObjectSpace.remove_finalizer(@proc)
        begin
	  yield
        ensure
	  ObjectSpace.add_finalizer(@proc)
        end
      end
    end

    private

    # registering function to ObjectSpace#add_finalizer
    def final_of(id)
      if assocs = @dependency.delete(id)
	for dependant, method, *opt in assocs
	  dependant.send(method, id, *opt)
	end
      end
    end

  end
  @monitor = Monitor.new
  @dependency = Hash.new
  @proc = proc{|id| final_of(id)}
  ObjectSpace.add_finalizer(@proc)
end
