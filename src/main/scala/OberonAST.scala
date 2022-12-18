package oberonAST

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer  

/**
 * Abstract Syntax Tree representation of an Oberon Module. 
 * The code here is almost an exact copy of 
 * https://github.com/UnBCIC-TP2/Oberon-Scala/blob/master/src/main/scala/br/unb/cic/oberon/ast/OberonModule.scala
 * This is done so that the AST generated by our parser is compatible with the existing one.
 */

case class OberonModule(name: String,
                        submodules: Set[String],
                        userTypes: List[UserDefinedType],
                        constants: List[Constant],
                        variables: List[VariableDeclaration],
                        procedures: List[Procedure],
                        stmt: Option[Statement]
                       ) {
}

// SequenceStatement(List[Stmt]) extends Stmt
// alternativa: SequenceStatement(stmt, stmt) extends Stmt

/* procedure declaration definition */
case class Procedure(name: String,
                     args: List[FormalArg],
                     returnType: Option[Type],
                     constants: List[Constant],
                     variables: List[VariableDeclaration],
                     stmt: Statement
                    ) {
}

/* formal argument definition */
trait FormalArg {
  def argumentType: Type
  def name: String
}
case class ParameterByValue(name: String, argumentType: Type) extends FormalArg
case class ParameterByReference(name: String, argumentType: Type) extends FormalArg


  /* Imports */
case class Import(name: String)

/* Constant definition */
case class Constant(name: String, exp: Expression)

/* Variable declaration definition */
case class VariableDeclaration(name: String, variableType: Type)

/* Expressions */
trait Expression

sealed abstract class Value extends Expression with Ordered[Value]{
  type T
  def value: T

  override def compare(that: Value): Int = (this, that) match {
    case (v1: IntValue, v2: IntValue) => v1.value.compareTo(v2.value)
    case (v1: IntValue, v2: RealValue) => ValueConversion.intValue2RealValue(v1).compareTo(v2)
    case (v1: RealValue, v2: IntValue) => ValueConversion.intValue2RealValue(v2).compareTo(v1)
    case (v1: RealValue, v2: RealValue) => v1.value.compareTo(v2.value)
    case (v1: CharValue, v2: CharValue) => v1.value.compareTo(v2.value)
    case (v1: StringValue, v2: StringValue) => v1.value.compareTo(v2.value)
    case _ => throw new RuntimeException("Comparison is not defined for " + this.getClass + " and " + that.getClass)
  }
}

sealed trait Number extends Expression {
  def +(that: Number): Number
  def -(that: Number): Number
  def *(that: Number): Number
  def /(that: Number): Number

}

sealed trait Modular extends Number {
  def mod(that: Modular): Modular
}

case class IntValue(value: Int) extends Value with Modular {
  type T = Int
  def +(that: Number): Number = that match {
    case other: IntValue => IntValue(value + other.value)
    case other: RealValue => RealValue(value + other.value)
  }

  def -(that: Number): Number = that match {
    case other: IntValue => IntValue(value - other.value)
    case other: RealValue => RealValue(value - other.value)
  }

  def *(that: Number): Number = that match {
    case other: IntValue => IntValue(value * other.value)
    case other: RealValue => RealValue(value * other.value)
  }

  def /(that: Number): Number = that match {
    case other: IntValue => IntValue(value / other.value)
    case other: RealValue => RealValue(value / other.value)
  }

  val positiveMod = (x: Int, y:Int) => {val res = x % y; if (x < 0) res + y else res}

  def mod(that: Modular): Modular = that match{
    case other: IntValue => IntValue(positiveMod(value, other.value))
  }
}

case class RealValue(value: Double) extends Value with Number {
  type T = Double

  def +(that: Number): Number = that match {
    case other: IntValue => RealValue(value + other.value)
    case other: RealValue => RealValue(value + other.value)
  }

  def -(that: Number): Number = that match {
    case other: IntValue => RealValue(value - other.value)
    case other: RealValue => RealValue(value - other.value)
  }

  def *(that: Number): Number = that match {
    case other: IntValue => RealValue(value * other.value)
    case other: RealValue => RealValue(value * other.value)
  }

  def /(that: Number): Number = that match {
    case other: IntValue => RealValue(value / other.value)
    case other: RealValue => RealValue(value / other.value)
  }
}

case class CharValue(value: Char) extends Value { type T = Char }
case class StringValue(value: String) extends Value { type T = String }
case class BoolValue(value: Boolean) extends Value { type T = Boolean }

sealed trait PointerAccess extends Expression
case class PointerAccessExpression(name: String) extends PointerAccess
case class ComplexPointerExpression(exp: Expression) extends PointerAccess

sealed trait FunctionCall extends Expression
case class FunctionCallExpression(name: String, args: List[Expression]) extends FunctionCall
case class ComplexFunctionCallExpression(exp: Expression, args: List[Expression]) extends FunctionCall


case object NullValue extends Expression
case class Location(loc: Int) extends Expression
case class Brackets(exp: Expression) extends Expression
case class ArrayValue(value: ListBuffer[Expression], arrayType: ArrayType) extends Value { type T = ListBuffer[Expression] }
case class ArraySubscript(arrayBase: Expression, index: Expression) extends Expression
case class Undef() extends Expression
case class FieldAccessExpression(exp: Expression, name: String) extends Expression
case class VarExpression(name: String) extends Expression
case class EQExpression(left:  Expression, right: Expression) extends Expression
case class NEQExpression(left:  Expression, right: Expression) extends Expression
case class GTExpression(left:  Expression, right: Expression) extends Expression
case class LTExpression(left:  Expression, right: Expression) extends Expression
case class GTEExpression(left:  Expression, right: Expression) extends Expression
case class LTEExpression(left:  Expression, right: Expression) extends Expression
case class AddExpression(left: Expression, right: Expression) extends Expression
case class SubExpression(left: Expression, right: Expression) extends Expression
case class MultExpression(left: Expression, right: Expression) extends Expression
case class DivExpression(left: Expression, right: Expression) extends Expression
case class OrExpression(left: Expression, right: Expression) extends Expression
case class AndExpression(left: Expression, right: Expression) extends Expression
case class ModExpression(left: Expression, right: Expression) extends Expression
case class NotExpression(exp: Expression) extends Expression

/* Algebraic data types for operators. */
sealed trait RelationOperator
case object EQOperator extends RelationOperator
case object NEQOperator extends RelationOperator
case object LTOperator extends RelationOperator
case object LTEOperator extends RelationOperator
case object GTOperator extends RelationOperator
case object GTEOperator extends RelationOperator

sealed trait AddOperator
case object PlusOperator extends AddOperator
case object MinusOperator extends AddOperator
case object OrOperator extends AddOperator

sealed trait MultOperator
case object TimesOperator extends MultOperator
case object SlashOperator extends MultOperator
case object AndOperator extends MultOperator
case object ModOperator extends MultOperator

sealed trait UnaryArithOperator
case object UnaryPlusOperator extends UnaryArithOperator
case object UnaryMinusOperator extends UnaryArithOperator

/* Statements */
trait Statement {
  val label = Statement.getLabel()
}

object Statement{
  var label = 0

  def getLabel() : Int = {
    label += 1
    label
  }

  def reset() : Unit = {
    label = 0
  }
}

case class AssignmentStmt(designator: Designator, exp: Expression) extends Statement
case class SequenceStmt(stmts: List[Statement]) extends Statement
case class ReadLongRealStmt(varName: String) extends Statement
case class ReadRealStmt(varName: String) extends Statement
case class ReadLongIntStmt(varName: String) extends Statement
case class ReadIntStmt(varName: String) extends Statement
case class ReadShortIntStmt(varName: String) extends Statement
case class ReadCharStmt(varName: String) extends Statement
case class WriteStmt(expression: Expression) extends Statement
case class ProcedureCallStmt(name: String, args: List[Expression]) extends Statement
case class IfElseStmt(condition: Expression, thenStmt: Statement, elseStmt: Option[Statement]) extends Statement
case class IfElseIfStmt(condition: Expression, thenStmt: Statement, elseifStmt: List[ElseIfStmt], elseStmt: Option[Statement]) extends Statement
case class ElseIfStmt(condition: Expression, thenStmt: Statement) extends Statement
case class WhileStmt(condition: Expression, stmt: Statement) extends Statement
case class RepeatUntilStmt(condition: Expression, stmt: Statement) extends Statement
case class ForStmt(init: Statement, condition: Expression, stmt: Statement) extends Statement
case class ForEachStmt(varName: String, exp: Expression, stmt: Statement) extends Statement
case class LoopStmt(stmt: Statement) extends Statement
case class ReturnStmt(exp: Expression) extends Statement
case class CaseStmt(exp: Expression, cases: List[CaseAlternative], elseStmt: Option[Statement]) extends Statement
case class ExitStmt() extends Statement
case class MetaStmt(f: () => Statement) extends Statement

trait CaseAlternative
case class SimpleCase(condition: Expression, stmt: Statement) extends CaseAlternative
case class RangeCase(min: Expression, max: Expression, stmt: Statement) extends CaseAlternative

sealed trait Selector
case object PointerSelector extends Selector
case class ArraySelector(exp: Expression) extends Selector
case class FieldSelector(field: String) extends Selector

sealed trait Designator

case class VarAssignment(varName: String) extends Designator
case class ArrayAssignment(array: Expression, index: Expression) extends Designator
case class RecordAssignment(record: Expression, field: String) extends Designator
case class PointerAssignment(pointerName: String) extends Designator
case class ComplexPointerAssignment(expression: Expression) extends Designator

case class DesignatorHelper(name: String, selectors: List[Selector])

/**
 * User defined types.
 *
 * Users can declare either records or
 * array types.
 */
case class UserDefinedType(name: String, baseType: Type)

/** The hierarchy for the Oberon supported types */
sealed trait Type

case object IntegerType extends Type
case object RealType extends Type
case object BooleanType extends Type
case object CharacterType extends Type
case object StringType extends Type
case object UndefinedType extends Type
case object NullType extends Type
case object LocationType extends Type

case class RecordType(variables: List[VariableDeclaration]) extends Type
case class ArrayType(length: Int, baseType: Type) extends Type
case class PointerType(variableType: Type) extends Type

case class ReferenceToUserDefinedType(name: String) extends Type

object ValueConversion {
  implicit def intValue2RealValue(intValue: IntValue): RealValue = RealValue(intValue.value)
  implicit def charValue2IntValue(charValue: CharValue): IntValue = IntValue(charValue.value)
}
