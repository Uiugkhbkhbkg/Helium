package helium.util

import arc.util.Strings

private val storeList = arrayOf(
  "B", "KB", "MB", "GB",
  "TB", "PB", "YB", "ZB"
)

fun Float.toStoreSize(): String {
  var v = this
  var n = 0

  while (v > 1024) {
    v /= 1024
    n++
  }

  return "${Strings.fixed(v, 2)}[lightgray]${storeList[n]}"
}