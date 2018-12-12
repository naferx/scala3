import scala.tasty.Reflection

class JVMReflection[R <: Reflection & Singleton](val reflect: R) {
  import reflect._
  import java.lang.reflect.{InvocationTargetException, Method}
  private val classLoader: ClassLoader = getClass.getClassLoader

  // taken from StdNames
  final val MODULE_INSTANCE_FIELD      = "MODULE$"

  def loadModule(sym: Symbol): Object = {
    if (sym.owner.flags.isPackage) {
      // is top level object
      val moduleClass = loadClass(sym.fullName)
      moduleClass.getField(MODULE_INSTANCE_FIELD).get(null)
    }
    else {
      // nested object in an object
      // val clazz = loadClass(sym.fullNameSeparated(FlatName))
      // clazz.getConstructor().newInstance().asInstanceOf[Object]
      ???
    }
  }

 def loadClass(name: String): Class[_] = {
    try classLoader.loadClass(name)
    catch {
      case _: ClassNotFoundException =>
        val msg = s"Could not find class $name in classpath$extraMsg"
        throw new Exception(msg)
    }
  }

  def interpretStaticVal(moduleClass: Symbol, fn: Symbol): Object = {
    val instance = loadModule(moduleClass)
    val name = fn.name

    val method = getMethod(instance.getClass, name, Nil)
    method.invoke(instance)
  }

  def interpretStaticMethodCall(moduleClass: Symbol, fn: Symbol, args: => List[Object]): Object = {
    val instance = loadModule(moduleClass)
    val name = fn.name

    val method = getMethod(instance.getClass, name, paramsSig(fn))
    method.invoke(instance, args: _*)
  }

  def getMethod(clazz: Class[_], name: String, paramClasses: List[Class[_]]): Method = {
    try clazz.getMethod(name, paramClasses: _*)
    catch {
      case _: NoSuchMethodException =>
        val msg = s"Could not find method ${clazz.getCanonicalName}.$name with parameters ($paramClasses%, %)$extraMsg"
        throw new Exception(msg)
    }
  }

  private def paramsSig(sym: Symbol): List[Class[_]] = {
    sym.asDef.signature.paramSigs.map { param =>
      println(param)
      ???
      // defn.valueTypeNameToJavaType(param) match {
      //   case Some(clazz) => clazz
        // case None =>
        //   def javaArraySig(name: String): String = {
        //     if (name.endsWith("[]")) "[" + javaArraySig(name.dropRight(2))
        //     else name match {
        //       case "scala.Boolean" => "Z"
        //       case "scala.Byte" => "B"
        //       case "scala.Short" => "S"
        //       case "scala.Int" => "I"
        //       case "scala.Long" => "J"
        //       case "scala.Float" => "F"
        //       case "scala.Double" => "D"
        //       case "scala.Char" => "C"
        //       case paramName => "L" + paramName + ";"
        //     }
        //   }
        //   def javaSig(name: String): String =
        //     if (name.endsWith("[]")) javaArraySig(name) else name
        //   java.lang.Class.forName(javaSig(param.toString), false, classLoader)
      //}
    }
  }

  private def extraMsg = ". The most common reason for that is that you apply macros in the compilation run that defines them"
}