val builder = StringBuilder()

for(n in 0..20){
  val typeArgs = StringBuilder()
  val typeArgsUse = StringBuilder()
  val types = StringBuilder()
  for (value in 1..n) {
    typeArgs.append("reified ").append("P").append(value).append(", ")
    typeArgsUse.append("P").append(value).append(", ")
    types.append("P").append(value).append("::class.java").append(", \n  ")
  }

  val res =
    """
inline fun <reified O, ${typeArgs}reified R> Class<O>.accessMethod(name: String) =
MethodInvoker$n<O, ${typeArgsUse}R>(this.getDeclaredMethod(
  name,
  ${if (n > 0) types.substring(0, types.length - 5) else types}
).also {
  checkReturnType(it, R::class.java)
  it.isAccessible = true
})"""

  builder.append(res)
}

System.out.print(builder.toString())