
/*
  The contents of this file is heaviy influenced and/or partly taken from
  the Leon Project which is released under the BSD 2 clauses license.
  See file LEON_LICENSE or go to https://github.com/epfl-lara/leon
  for full license details.
 */

package daisy
package solvers

import scala.collection.immutable.Seq

import _root_.smtlib.parser.Terms.{Let => SMTLet,Identifier => SMTIdentifier,_}
import _root_.smtlib.parser.Commands.{FunDef => _, Assert => SMTAssert, _ }
import _root_.smtlib.parser.CommandsResponses.{Error => ErrorResponse, _}
import _root_.smtlib.interpreters.ProcessInterpreter
import _root_.smtlib.printer.{RecursivePrinter => SMTPrinter}
import _root_.smtlib.theories._

import lang.Types._
import lang.Trees._
import lang.Identifiers._
import utils.Bijection
import lang.TreeOps.variablesOf
import lang.TreeOps.deltasOf
import lang.TreeOps.epsilonsOf
import utils.Rational
import Rational._
import lang.Constructors._

import scala.collection.mutable.StringBuilder


object Solver {

  private val defaultVal = "z3"

  private var context: Context = null

  private var solver = defaultVal

  def setContext(ctx: Context) = {
    context = ctx
    solver = context.findOption(Main.optionSolver) match {
      case Some("dreal") | Some("dReal") => context.reporter.info("Using dReal")
            "dreal"
      case Some("z3") => "z3"
      case None => defaultVal
      case Some(s) =>
        context.reporter.warning("Unknown solver: $s, choosing default (" + defaultVal + ")!")
        defaultVal
    }

    Z3Solver.context = ctx
    DRealSolver.context = ctx
  }

  def hasContext(): Boolean = context != null

  def checkSat(query: Expr): Option[Boolean] = {
    solver match {
      case "z3" => Z3Solver.checkSat(query)
      case "dreal" => DRealSolver.checkSat(query)
      case s =>
        context.reporter.internalError(s"Unknown solver: $s!")
    }
  }

  def getModel(query: Expr): Option[Model] = {
    solver match {
      case "z3" => Z3Solver.checkAndGetModel(query)
        // todo: dreal getmodel
      case "dreal" => context.reporter.internalError(s"Not yet implemented!")
      case s =>
        context.reporter.internalError(s"Unknown solver: $s!")
    }
  }

  var unknownCounter = 0
}

//TODO place somewhere else?
object Power extends scala.AnyRef{
    def apply(t1: Term, t2: Term): Term =
            FunctionApplication(
                QualifiedIdentifier(smtlib.parser.Terms.Identifier(SSymbol("^"))),
                Seq(t1, t2)
                )

          def unapply(term: Term): Option[(Term, Term)] = term match {
          case FunctionApplication(
              QualifiedIdentifier(
                  smtlib.parser.Terms.Identifier(SSymbol("^"), Seq()),
                  None
                  ), Seq(t1, t2)) => Some((t1, t2))
          case _ => None
        }
  }

// general interface for a solver
abstract class SMTLibSolver(val context: Context) {

  implicit val debugSection = DebugSectionSolver
  // debugging prints ALL SMT calls, but we don't necessarily want that,
  // so we have a separate flag
  val printToughSMTCalls = context.findOption(Main.optionPrintToughSMTCalls).getOrElse(false)

  /* Reporter */
  protected val reporter = context.reporter

  /* Solver name */
  def targetName: String
  def name: String = "smt-"+targetName

  val interpreterOpts: Seq[String]
  protected def getNewInterpreter(): ProcessInterpreter

  protected lazy val interpreter = getNewInterpreter

  val output = new java.io.StringWriter
  val commandBuffer = new java.io.BufferedWriter(output)

  /* Printing VCs */
  protected lazy val debugOut: Option[java.io.FileWriter] = {
//    if (reporter.isDebugEnabled) {
//      val file = context.files.headOption.getOrElse("NA")
//      val n = DebugFileNumbers.next(targetName + file)
//
//      val fileName = s"smt-sessions/$targetName-$file-$n.smt2"
//
//      val javaFile = new java.io.File(fileName)
//      javaFile.getParentFile.mkdirs()
//
//      reporter.debug(s"Outputting smt session into $fileName")
//
//      val fw = new java.io.FileWriter(javaFile, false)
//
//      fw.write("; Options: " + interpreterOpts.mkString(" ") + "\n")
//
//      Some(fw)
//    } else {
      None
//    }
  }

  /* Send a command to the solver */
  protected def emit(cmd: SExpr, rawOut: Boolean = false): SExpr = {
    debugOut foreach { o =>
      SMTPrinter.printSExpr(cmd, o)
      o.write("\n")
      o.flush()
    }

    SMTPrinter.printSExpr(cmd, commandBuffer)
    commandBuffer.write("\n")
    commandBuffer.flush()

//    System.out.println("CMD: "+cmd)
    interpreter.eval(cmd) match {
      case err @ ErrorResponse(msg) if !rawOut =>
        reporter.warning(s"Unexpected error from $targetName solver: $msg")
        // Store that there was an error. Now all following check()
        // invocations will return None
        addError()
        err
      case res =>
//        System.out.println(res)
        res
    }
  }

  /* Add a constraint */
  def assertConstraint(expr: Expr): Unit = {
    try {
      variablesOf(expr).foreach(declareVariable)
      deltasOf(expr).foreach {
        case Delta(id) => declareVariable(id)
      }
      epsilonsOf(expr).foreach {
        case Epsilon(id) => declareVariable(id)
      }
      val term = toSMT(expr)(Map())
      //reporter.warning(s"solver query SMT:" + SMTAssert(term))
      emit(SMTAssert(term))
    } catch {
      case _ : SMTLIBUnsupportedError =>
        // Store that there was an error. Now all following check()
        // invocations will return None
        addError()
    }
  }

  /* Check the satisfiability of the currently asserted constraints.
    @return Some(true) == SAT, Some(false) == UNSAT, None == error or unknown
  */
  def checkSat: Option[Boolean] = {

    if (hasError) None
    else {
      val start = System.currentTimeMillis
      val res = emit(CheckSat())
      val diff = System.currentTimeMillis - start
      //reporter.info(s"SMT call took $diff ms")

      if (reporter.isDebugEnabled || printToughSMTCalls) {
        if (diff > 20) {  // 20ms is a random large number
          if (diff > 1000) {
            reporter.warning(s"solver took $diff ms!")


          reporter.info("printing tough smt call")
          // slashes in the file name cause file not found errors
          val file = context.files.headOption.getOrElse("NA").replace("/", "-")
          val n = DebugFileNumbers.next(targetName + file)
          val fileName = s"smt-sessions/$targetName-$file-$n.smt2"

          val out = new java.io.BufferedWriter(new java.io.FileWriter(fileName))
//          out.write(s"; time taken: $diff ms\n")
          out.write(output.toString)
          out.close
          }
        }
      }

      res match {
        case CheckSatStatus(SatStatus)     => Some(true)
        case CheckSatStatus(UnsatStatus)   => Some(false)
        case CheckSatStatus(UnknownStatus) => reporter.debug(s"solver says $res")
                                                None
        case e                             => reporter.debug(s"solver CRIES $res")
          None
      }

    }
  }

  protected def getModel(filter: Identifier => Boolean): Model = {
    val syms = variables.aSet.filter(filter).toList.map(variables.aToB)
    if (syms.isEmpty) {
      Model.empty
    } else {
      try {
        val cmd: Command = GetValue(
          syms.head,
          syms.tail.map(s => QualifiedIdentifier(SMTIdentifier(s)))
        )
//        reporter.warning(cmd)
        emit(cmd) match {
          case GetValueResponseSuccess(valuationPairs) =>
            System.out.println("Val Pairs: "+valuationPairs)
            new Model(valuationPairs.collect {
              case (SimpleSymbol(sym), value) if variables.containsB(sym) =>
                val id = variables.toA(sym)

                (id, fromSMT(value, id.getType)(Map(), Map()))
            }.toMap)
          case _ =>
            Model.empty //FIXME improve this
        }
      } catch {
        case e : SMTLIBUnsupportedError =>
          throw new SolverUnsupportedError(e.t, this, e.reason)
      }
    }
  }

  def getModel: Model = getModel( _ => true)

  /*def reset() = {
    emit(Reset(), rawOut = true) match {
      case ErrorResponse(msg) =>
        reporter.warning(s"Failed to reset $name: $msg")
        throw new CantResetException(this)
      case _ =>
    }
  }*/

  /* Should be called after the solver is no longer used. */
  def free() = {
    interpreter.free()
    debugOut foreach { _.close }

    commandBuffer.close()
    //reporter.info(s"# calls: $n, avrg time per SMT call: $runningAvrg,"+
    //  " max time per call: $maxTime")

  }

  //protected val errors = new IncrementalBijection[Unit, Boolean]()
  private var errors: Boolean = false
  protected def hasError: Boolean = errors //errors.getB(()) contains true
  protected def addError() = errors = true

  /*
    -------- Daisy to SMT --------
   */

  protected val variables = new Bijection[Identifier, SSymbol]()
  protected val deltas = new Bijection[Identifier, SSymbol]()
  // in Leon, this is FunDef -> SSymbol
  // we may have to do extra work, when mapping back to get the full fnc
  protected val functions = new Bijection[Identifier, SSymbol]()

  import scala.language.implicitConversions
  protected implicit def symbolToQualifiedId(s: SSymbol): QualifiedIdentifier = {
    QualifiedIdentifier(SMTIdentifier(s))
  }

  protected def id2sym(id: Identifier): SSymbol = {
    SSymbol(id.uniqueNameDelimited("!").replace("|", "$pipe").replace("\\", "$backslash"))
  }

  /*
    Transforms a Daisy type to an SMTLib type
    This will get more complicated, once we have parametric and more complex types
   */
  protected def toSMTSort(tpe: TypeTree): Sort = {
    val sort = tpe match {
      // TODO: cache the types?
      case BooleanType => Core.BoolSort()
      case IntegerType => Ints.IntSort()
      case RealType    => Reals.RealSort()
    }
    sort
  }

  protected def toSMT(tpe: TypeTree): SExpr = {
    toSMTSort(tpe).id.symbol
  }


  protected def declareVariable(id: Identifier): SSymbol = {
    variables.getOrElseAddB(id) {
      val s = id2sym(id)
      val cmd = DeclareFun(s, List(), toSMTSort(id.getType))
//      reporter.warning(cmd)
      emit(cmd)
      s
    }
  }

  protected def declareFunction(fncInv: FunctionInvocation): SSymbol = {
    functions.getOrElseAddB(fncInv.fdId) {
      val s = id2sym(fncInv.fdId)
      emit(DeclareFun(
        s,
        fncInv.params.map((p: ValDef) => toSMTSort(p.getType)),
        toSMTSort(fncInv.returnType)))
      s
    }
  }

  /*
   Transforms a Daisy expression to a SMTLib expression.
   */
  protected def toSMT(e: Expr)(implicit bindings: Map[Identifier, Term]): Term = e match {
    /**
     * ===== Literals =====
     */

    case Delta(id) =>
      toSMT(e.getType)
      // either bindings has the mapping, or else we look in variables.
      val name = id.uniqueNameDelimited("!")
//      reporter.debug(s"delta $name")
      bindings.getOrElse(id, SSymbol(name))

    case Epsilon(id) =>
      toSMT(e.getType)
      // either bindings has the mapping, or else we look in variables.
      val name = id.uniqueNameDelimited("!")
//      reporter.debug(s"delta $name")
      bindings.getOrElse(id, SSymbol(name))

    case Variable(id) =>
      toSMT(e.getType)
      // either bindings has the mapping, or else we look in variables.
      bindings.getOrElse(id, variables.toB(id))


    case IntegerLiteral(i)
      => if (i >= 0) Ints.NumeralLit(i) else Ints.Neg(Ints.NumeralLit(-i))

      //case Int32Literal(i)             => FixedSizeBitVectors.BitVectorLit(Hexadecimal.fromInt(i))

    case RealLiteral(r) =>
      Reals.Div(Reals.NumeralLit(r.n), Reals.NumeralLit(r.d))

    case BooleanLiteral(v)         => Core.BoolConst(v)
    case Let(b, d, e) =>
      val id = id2sym(b)
      val value = toSMT(d)
      val newBody = toSMT(e)(bindings + (b -> id))

      SMTLet(
        VarBinding(id, value),
        Seq(),
        newBody)

      //case er @ Error(tpe, _) =>
      //  declareVariable(FreshIdentifier("error_value", tpe))

    /**
     * ===== Arithmetic =====
     */

    case UMinus(u)       => Ints.Neg(toSMT(u))
    case Plus(a, b) if (e.getType == IntegerType)   => Ints.Add(toSMT(a), toSMT(b))
    case Minus(a, b) if (e.getType == IntegerType)  => Ints.Sub(toSMT(a), toSMT(b))
    case Times(a, b) if (e.getType == IntegerType)  => Ints.Mul(toSMT(a), toSMT(b))
    case Division(a, b) if (e.getType == IntegerType) => {
      val ar = toSMT(a)
      val br = toSMT(b)

      Core.ITE(
        Ints.GreaterEquals(ar, Ints.NumeralLit(0)),
        Ints.Div(ar, br),
        Ints.Neg(Ints.Div(Ints.Neg(ar), br)))
    }

    case Plus(a, b)     if (e.getType == RealType) => Reals.Add(toSMT(a), toSMT(b))
    case Minus(a, b)    if (e.getType == RealType) => Reals.Sub(toSMT(a), toSMT(b))
    case Times(a, b)    if (e.getType == RealType) => Reals.Mul(toSMT(a), toSMT(b))
    case Division(a, b) if (e.getType == RealType) => Reals.Div(toSMT(a), toSMT(b))

    case Pow(x, n) => Power(toSMT(x), toSMT(n))
    /**
     * ===== Logic =====
     */
    case Not(u)          => Core.Not(toSMT(u))
    case And(sub)                  => Core.And(sub.map(toSMT): _*)
    case Or(sub)                   => Core.Or(sub.map(toSMT): _*)

    case Equals(a, b)    => Core.Equals(toSMT(a), toSMT(b))
    case Implies(a, b)   => Core.Implies(toSMT(a), toSMT(b))
    case LessThan(a, b) => a.getType match {
      //case Int32Type   => FixedSizeBitVectors.SLessThan(toSMT(a), toSMT(b))
      case IntegerType => Ints.LessThan(toSMT(a), toSMT(b))
      case RealType    => Reals.LessThan(toSMT(a), toSMT(b))
    }
    case LessEquals(a, b) => a.getType match {
      //case Int32Type   => FixedSizeBitVectors.SLessEquals(toSMT(a), toSMT(b))
      case IntegerType => Ints.LessEquals(toSMT(a), toSMT(b))
      case RealType    => Reals.LessEquals(toSMT(a), toSMT(b))
    }
    case GreaterThan(a, b) => a.getType match {
      //case Int32Type   => FixedSizeBitVectors.SGreaterThan(toSMT(a), toSMT(b))
      case IntegerType => Ints.GreaterThan(toSMT(a), toSMT(b))
      case RealType    => Reals.GreaterThan(toSMT(a), toSMT(b))
    }
    case GreaterEquals(a, b) => a.getType match {
      //case Int32Type   => FixedSizeBitVectors.SGreaterEquals(toSMT(a), toSMT(b))
      case IntegerType => Ints.GreaterEquals(toSMT(a), toSMT(b))
      case RealType    => Reals.GreaterEquals(toSMT(a), toSMT(b))
    }

    case IfExpr(cond, thenn, elze) => Core.ITE(toSMT(cond), toSMT(thenn), toSMT(elze))

    case f @ FunctionInvocation(_, _, args, _) =>
      if (args.isEmpty) {
        declareFunction(f)
      } else {
        FunctionApplication(
          declareFunction(f),
          args.map(toSMT))
      }
    case o =>
      reporter.warning(s"$o is unsupported by solver $targetName")
      throw new SolverUnsupportedError(o, this, None)

  }

  /* --------- Model extraction --------- */

  protected object SimpleSymbol {
    def unapply(term: Term): Option[SSymbol] = term match {
      case QualifiedIdentifier(SMTIdentifier(sym, Seq()), None) => Some(sym)
      case _ => None
    }
  }

  /* Translate an SMTLIB term back to a Leon Expr */
  protected def fromSMT(t: Term, otpe: Option[TypeTree] = None)(implicit lets: Map[SSymbol, Term], letDefs: Map[SSymbol, DefineFun]): Expr = {

    // Use as much information as there is, if there is an expected type, great, but it might not always be there
    (t, otpe) match {

      case (FixedSizeBitVectors.BitVectorConstant(n, b), Some(Int32Type)) if b == BigInt(32) =>
        Int32Literal(n.toInt)

      case (SHexadecimal(hexa), Some(Int32Type)) =>
        Int32Literal(hexa.toInt)

      case (SDecimal(d), Some(RealType)) =>
        // converting bigdecimal to a fraction
        if (d == BigDecimal(0))
          RealLiteral(Rational.zero)
        else {
          d.toBigIntExact() match {
            case Some(num) =>
              RealLiteral(Rational(num, 1))
            case _ =>
              val scale = d.scale
              val num = BigInt(d.bigDecimal.scaleByPowerOfTen(scale).toBigInteger())
              val denom = BigInt(new java.math.BigDecimal(1).scaleByPowerOfTen(scale).toBigInteger())
              RealLiteral(Rational(num, denom))
          }
        }

      case (SNumeral(n), Some(RealType)) =>
        RealLiteral(Rational(n, 1))

      case (FunctionApplication(SimpleSymbol(SSymbol("ite")), Seq(cond, thenn, elze)), t) =>
        IfExpr(
          fromSMT(cond, Some(BooleanType)),
          fromSMT(thenn, t),
          fromSMT(elze, t))

      // Best-effort case
      case (SNumeral(n), _) =>
        IntegerLiteral(n)

      // EK: Since we have no type information, we cannot do type-directed
      // extraction of defs, instead, we expand them in smt-world
      case (SMTLet(binding, bindings, body), tpe) =>
        val defsMap: Map[SSymbol, Term] = (binding +: bindings).map {
          case VarBinding(s, value) => (s, value)
        }.toMap

        fromSMT(body, tpe)(lets ++ defsMap, letDefs)

      case (FunctionApplication(SimpleSymbol(s @ SSymbol(app)), args), _) =>
        (app, args) match {
          case (">=", List(a, b)) =>
            GreaterEquals(fromSMT(a, IntegerType), fromSMT(b, IntegerType))

          case ("<=", List(a, b)) =>
            LessEquals(fromSMT(a, IntegerType), fromSMT(b, IntegerType))

          case (">", List(a, b)) =>
            GreaterThan(fromSMT(a, IntegerType), fromSMT(b, IntegerType))

          case (">", List(a, b)) =>
            LessThan(fromSMT(a, IntegerType), fromSMT(b, IntegerType))

          case ("+", args) =>
            args.map(fromSMT(_, otpe)).reduceLeft(plus _)

          case ("-", List(a)) if otpe == Some(RealType) =>
            val aexpr = fromSMT(a, otpe)
            aexpr match {
              case RealLiteral(r) =>
                RealLiteral(Rational(-r.n, r.d))
              case _ =>
                UMinus(aexpr)
            }

          case ("-", List(a)) =>
            val aexpr = fromSMT(a, otpe)
            aexpr match {
              case IntegerLiteral(v) =>
                IntegerLiteral(-v)
              case _ =>
                UMinus(aexpr)
            }

          case ("-", List(a, b)) =>
            Minus(fromSMT(a, otpe), fromSMT(b, otpe))

          case ("*", args) =>
            args.map(fromSMT(_, otpe)).reduceLeft(times _)

          case ("/", List(a, b)) if otpe == Some(RealType) =>
            val aexpr = fromSMT(a, otpe)
            val bexpr = fromSMT(b, otpe)
            (aexpr, bexpr) match {
              case (RealLiteral(r1), RealLiteral(r2)) if r1.d == 1 && r2.d == 1 =>
                RealLiteral(Rational(r1.n, r2.n))
              case _ =>
                Division(aexpr, bexpr)
            }

          case ("/", List(a, b)) =>
            Division(fromSMT(a, otpe), fromSMT(b, otpe))

          case ("^", List(a, b)) =>
            Pow(fromSMT(a, otpe), fromSMT(b, otpe))

          case ("div", List(a, b)) =>
            Division(fromSMT(a, otpe), fromSMT(b, otpe))

          case ("not", List(a)) =>
            Not(fromSMT(a, BooleanType))

          case ("or", args) =>
            or(args.map(fromSMT(_, BooleanType)):_*)

          case ("and", args) =>
            and(args.map(fromSMT(_, BooleanType)):_*)

          case ("=", List(a, b)) =>
            val ra = fromSMT(a, None)
            Equals(ra, fromSMT(b, ra.getType))

          case _ =>
            reporter.fatalError("Function " + app + " not handled in fromSMT: " + s)
        }

      case (Core.True(), Some(BooleanType))  => BooleanLiteral(true)
      case (Core.False(), Some(BooleanType)) => BooleanLiteral(false)

      //case (SimpleSymbol(s), otpe) if lets contains s =>
      //  fromSMT(lets(s), otpe)

      case (SimpleSymbol(s), otpe) if s.equals(SSymbol("?"))=>
        reporter.debug(s"Detected ? here")
        RealLiteral(Rational.zero)

      case (SimpleSymbol(s), otpe) =>
        variables.getA(s).map(_.toVariable).getOrElse {
          throw new Exception(s"Unknown symbol $s")
        }

      case _ =>
        reporter.fatalError(s"Unhandled case in fromSMT: $t : ${otpe.map(_.toString).getOrElse("?")} (${t.getClass})")

    }
  }

  final protected def fromSMT(pair: (Term, TypeTree))(implicit lets: Map[SSymbol, Term], letDefs: Map[SSymbol, DefineFun]): Expr = {
    fromSMT(pair._1, Some(pair._2))
  }

  final protected def fromSMT(s: Term, tpe: TypeTree)(implicit lets: Map[SSymbol, Term], letDefs: Map[SSymbol, DefineFun]): Expr = {
    fromSMT(s, Some(tpe))
  }

}

// Unique numbers
private[solvers] object DebugFileNumbers extends UniqueCounter[String]