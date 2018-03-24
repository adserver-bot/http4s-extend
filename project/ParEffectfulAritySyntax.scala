package Templates

import sbt._
import ContentHelpers._

private[Templates] object ParEffectfulAritySyntax extends Template {
  def moduleFile: File => File =
    _ / "syntax" / "ParEffectfulAritySyntax.scala"

  def content: Int => String =
    arity => {

      val top =
        static"""package http4s.extend.syntax
          |
          |private[syntax] trait ParEffectfulAritySyntax {
          |"""

      val bottom = static"""}"""

      s"""$top
         |$bottom""".stripMargin
    }
}