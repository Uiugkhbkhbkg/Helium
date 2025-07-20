
val builder = StringBuilder()

for(n in 0..22){
  val typeArgs = StringBuilder()
  val argsDecl = StringBuilder()
  val args = StringBuilder()
  for (value in 1..n) {
    typeArgs.append("P").append(value).append(", ")
    argsDecl.append("p").append(value).append(": ").append("P").append(value).append(", ")
    args.append("p").append(value).append(", ")
  }

  val res =
"""
class StaticInvoker$n<${typeArgs}R>(private val method: Method)
  : (${if (n > 0) typeArgs.substring(0, typeArgs.length - 2) else typeArgs}) -> R {
  override fun invoke(${if (n > 0) argsDecl.substring(0, argsDecl.length - 2) else ""})
    = method.invoke(null, ${if (n > 0) args.substring(0, args.length - 2) else ""}) as R
}"""

  builder.append(res)
}

System.out.print(builder.toString())