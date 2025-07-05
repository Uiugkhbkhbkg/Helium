package helium.util

import arc.func.Prov

class LazyWrap<T: Any>(val init: Prov<T>) {
  var raw: T? = null
    private set

  fun get() = raw?:init.get()!!.also { raw = it }
  fun initialized() = raw != null
}