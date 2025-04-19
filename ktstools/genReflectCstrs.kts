
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
class ConstructorInvoker$n<${typeArgs}O>(private val constructor: Constructor<O>)
  : (${if (n > 0) typeArgs.substring(0, typeArgs.length - 2) else typeArgs}) -> O {
  override fun invoke(${if (n > 0) argsDecl.substring(0, argsDecl.length - 2) else ""})
      = constructor.newInstance(${if (n > 0) args.substring(0, args.length - 2) else ""})
}"""

  builder.append(res)
}

System.out.print(builder.toString())