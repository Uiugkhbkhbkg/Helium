val builder = StringBuilder()

for(n in 0..20){
  val typeArgs = StringBuilder()
  val typeArgsUse = StringBuilder()
  val types = StringBuilder()
  for (value in 1..n) {
    typeArgs.append("reified ").append("P").append(value).append(", ")
    typeArgsUse.append("P").append(value).append(", ")
    types.append("P").append(value).append("::class.java").append(", ")
    if (value%5 == 0) types.append("\n  ")
  }

  val res =
"""
inline fun <C: Any, ${typeArgs}reified R> KClass<C>.accessMethod$n(name: String) =
StaticInvoker$n<${typeArgsUse}R>(this.java.getDeclaredMethod(
  name,
  ${if (n > 0) types.substring(0, types.length - if (n % 5 == 0) 5 else 2) else types}
).also {
  checkReturnType(it, R::class.java)
  it.isAccessible = true
})"""

  builder.append(res)
}

System.out.print(builder.toString())