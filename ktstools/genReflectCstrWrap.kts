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
inline fun <reified O: Any, ${if (n > 0) typeArgs.substring(0, typeArgs.length - 2) else typeArgs}> accessConstructor$n() =
ConstructorInvoker$n<${typeArgsUse}O>(O::class.java.getConstructor(
  ${if (n > 0) types.substring(0, types.length - if (n % 5 == 0) 5 else 2) else types}
).also {
  it.isAccessible = true
})"""

  builder.append(res)
}

System.out.print(builder.toString())