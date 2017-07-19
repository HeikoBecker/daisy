package daisy.utils

import daisy.lang.Identifiers.{FreshIdentifier, Identifier}
import daisy._
import daisy.analysis.RelativeErrorPhase._
import daisy.lang.TreeOps._
import daisy.lang.Trees.{RealLiteral, _}
import daisy.lang.Types.RealType
import daisy.utils.FinitePrecision.Float64
import utils.Interval._
import scala.util.control.Breaks._

import scala.collection.immutable.Map

import scala.collection.parallel.{ParSeq, ParSet}

/**
  * Trait with collections of methods to perform Taylor simplifications
  */

trait Taylor{

  var reporter: Reporter
  var denormals: Boolean

  var listFailed: List[Map[Identifier, Interval]] = List.empty

  implicit val debugSection = DebugSectionAnalysis

  implicit val zero = RealLiteral(Rational.zero)
  implicit val one = RealLiteral(Rational.one)
  implicit val two = RealLiteral(Rational.two)

  implicit val optionAbsOrdering = new Ordering[Option[Rational]]{
    override def compare(x: Option[Rational], y: Option[Rational]): Int = {
      if (x.isDefined)
        if (y.isDefined)
          Rational.abs(x.get).compare(Rational.abs(y.get))
        else 1  // y undefined Some(x) > None
      else
        if (y.isDefined) -1 // x undef None < Some(y)
        else 0  // both undefined None = None
    }
  }
  /**
    * Collects elements o the expression into the sequence
    * @param ex expression to be split into multiplication terms
    * @return list of expressions that were multiplied
    */
  def listElements(ex: Expr): Seq[Expr] = ex match {
    case Times(lhs, UMinus(rhs)) =>
      listElements(lhs).++(listElements(rhs)) :+ UMinus(one)
    case Times(UMinus(lhs), rhs) =>
      listElements(lhs).++(listElements(rhs)) :+ UMinus(one)
    case Times(lhs, rhs) =>
      listElements(lhs).++(listElements(rhs))
    case UMinus(Times(lhs, rhs)) =>
      listElements(UMinus(lhs)).++(listElements(rhs))
    case Pow(x, RealLiteral(n)) =>
      var tmp:Seq[Expr] = Seq.empty
      for (i <-0 until n.intValue()){
        tmp = listElements(x).++(tmp)
      }
      tmp
    case UMinus(Pow(x, RealLiteral(n))) =>
      var tmp:Seq[Expr] = Seq.empty
      for (i <-0 until n.intValue()){
        tmp = listElements(x).++(tmp)
      }
//      reporter.warning(s"List elements $tmp")
      tmp:+ UMinus(one)
    case x =>
      List(x)
  }

  /**
    * Cancels repeating terms in nominator and denominator of Division expression
    * @param nom nominator expression
    * @param denom demominator expression
    * @return simplified expression Division
    */
  def cancelNominators(nom:Expr, denom: Expr): Expr = {
    // FIXME function not used. contains bugs?
//    reporter.warning("=== BEFOREEXPRESSION ==== list Nominators " + nom)
//    reporter.warning("=== BEFOREEXPRESSION ==== list Denominators " + denom)

    // get lists of the elements in nominator and denominator
    val noms = listElements(nom)
    val denoms = listElements(denom)

//    reporter.warning(s"===BEFORE==== list Nominators $noms")
//    reporter.warning(s"===BEFORE==== list Denominators $denoms")

    // get repeating terms in nominator and denominator
    var newNomList = noms
    var newDenomList = denoms
      for (n <- newNomList){
      breakable {
        var found = false
        for (d <- newDenomList) {
          found = n.equals(d)
          if (found) {
            // remove the element from denominator
            newDenomList = newDenomList.diff(List(d))
            newNomList = newNomList.diff(List(n))
            break } }
      }
    }

//    reporter.warning(s"===AFTER==== Nominator LIST $newNomList")
//    reporter.warning(s"===AFTER==== Denominator LIST $newDenomList")

    // multiply the terms that left
    val newNom = multiply(newNomList)
    val newDenom = multiply(newDenomList)
//    reporter.warning(s"===AFTER==== Nominator $newNom")
//    reporter.warning(s"===AFTER==== Denominator $newDenom")

    if (newDenom.equals(RealLiteral(Rational.one)))
      newNom
    else
      Division(newNom, newDenom)
  }

  /**
    * Multiplies terms in the Seq
    * @param lst - sequence of terms to be multiplied
    * @return expression Times(Times(..., term2), term1)
    */
  private def multiply(lst: Seq[Expr]):Expr = {
    if (lst.isEmpty) one
    else {
      if (lst.lengthCompare(1) == 0) lst.head
      else {
        var tmp: Expr = one
        lst.foreach(x => {
          tmp = if (x == UMinus(one)) UMinus(tmp) else Times(tmp, x)
        })
        // here Pow will come back
        easySimplify(tmp)
      }
    }
  }


  private def multiplyRealLiterals(ra: Rational,
                                   list: Seq[Expr]):Expr = {
//    reporter.warning(s"terms to multiply $ra and $list")
    var newMultiplier = ra
    list.foreach { case x @ RealLiteral(r) => newMultiplier = r.*(newMultiplier)
    case _ => newMultiplier = newMultiplier }
    var res: Expr = RealLiteral(newMultiplier)
    list.foreach {
      case x @ RealLiteral(r) => one
      case y => res = Times(res, y) }

//    reporter.warning(s"terms after multiplying $res")
    res
  }
  /**
    * Simplifies expression using simple arithmetic rules
    * +, -, *, / with 0 and 1
    * @param ex expression to be simplified
    * @return simplified expression
    */
  def easySimplify(ex:Expr): Expr = {
    var ready = false
    var resExpr = ex

    def simpleRound(e: Expr):Expr = e match {
      case x @ Delta(id) => x
      case x @ Epsilon(id) => x
      case x @ Variable(id) => x

      case x @ RealLiteral(r) =>
        if (r.<(Rational.zero)) {
          UMinus(RealLiteral(r.unary_-()))
        } else x

      // Plus with zeros
      case x @ Plus(lhs , rhs) if (lhs == zero) && (rhs == zero) =>
        zero
      case x @ Plus(lhs , rhs) if lhs == zero =>
        simpleRound(rhs)
      case x @ Plus(lhs , rhs) if rhs == zero =>
        simpleRound(lhs)
      case x @ Plus(UMinus(lhs), UMinus(rhs)) if (lhs == zero) && (rhs == zero) =>
        zero
      case x @ Plus(UMinus(lhs), UMinus(rhs)) if lhs == zero =>
        simpleRound(rhs)
      case x @ Plus(UMinus(lhs), UMinus(rhs)) if rhs == zero =>
        simpleRound(lhs)
      case x @ Plus(lhs, UMinus(rhs)) =>
        Minus(lhs, simpleRound(rhs))

      case x @ Plus(lhs , rhs) if expressionsEqual(lhs, rhs) =>
        Times(RealLiteral(Rational.fromReal(2)), simpleRound(lhs))

      case x @ Plus(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.+(rhsIn))
          case _ =>
            Plus(simpleRound(lhs), simpleRound(rhs))
        }

      // Minus with zeros
      case x @ Minus(lhs , rhs) if (lhs == zero) && (rhs == zero) =>
        zero
      case x @ Minus(lhs , rhs) if lhs == zero =>
        UMinus(simpleRound(rhs))
      case x @ Minus(lhs , rhs) if rhs == zero =>
        simpleRound(lhs)
      case x @ Minus(UMinus(lhs), UMinus(rhs)) if (lhs == zero) && (rhs == zero) =>
        zero
      case x @ Minus(UMinus(lhs), UMinus(rhs)) if lhs == zero =>
        UMinus(simpleRound(rhs))
      case x @ Minus(UMinus(lhs), UMinus(rhs)) if lhs == zero =>
        simpleRound(lhs)
      case x @ Minus(lhs , rhs) if expressionsEqual(lhs, rhs)=>
        zero

      case x @ Minus(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.-(rhsIn))
          case _ =>
            Minus(simpleRound(lhs), simpleRound(rhs))
        }
      // Times with zeros and ones
      case x @ Times(lhs , rhs) if lhs == zero  =>
        zero
      case x @ Times(lhs , rhs) if rhs == zero =>
        zero
      case x @ Times(lhs , rhs) if lhs == one =>
        simpleRound(rhs)
      case x @ Times(lhs , rhs) if rhs == one =>
        simpleRound(lhs)
      case x @ Times(UMinus(lhs), rhs) if lhs == one =>
        UMinus(simpleRound(rhs))
      case x @ Times(lhs, UMinus(rhs)) if rhs == one =>
        UMinus(simpleRound(lhs))

      case x @ Times(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.*(rhsIn))
          case _ =>
            Times(simpleRound(lhs), simpleRound(rhs))
        }

      //Division with zeros and ones
      case x @ Division(lhs , rhs) if lhs == zero =>
        zero
      case x @ Division(lhs , rhs) if rhs == zero =>
        throw DivisionByZeroException("Inside simplify function this should not happen")
      case x @ Division(lhs , rhs) if rhs == one =>
        simpleRound(lhs)
      case x @ Division(lhs , rhs) if lhs.equals(rhs) =>
        one
      case x @ Division(lhs , rhs) if lhs.equals(UMinus(rhs)) =>
        UMinus(one)
      case x @ Division(lhs , rhs) if UMinus(lhs).equals(rhs) =>
        UMinus(one)

      case x @ Division(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn./(rhsIn))
          case (_, Division(lhsIn, rhsIn)) if lhs == one =>
            Division(simpleRound(rhsIn), simpleRound(lhsIn))
          case _ =>
            Division(simpleRound(lhs), simpleRound(rhs))
        }

      case x @ Pow(lhs, n) =>
        Pow(simpleRound(lhs), n)

      case x @ UMinus(lhs) =>
        lhs match {
          case UMinus(lhs2) => simpleRound(lhs2)
          case RealLiteral(Rational.zero) => zero
          case Division(lhsIn, rhsIn) => Division(UMinus(simpleRound(lhsIn)), simpleRound(rhsIn))
          case _ => UMinus(simpleRound(lhs))
        }

      case x @ Sqrt(lhs) =>
        Sqrt(simpleRound(lhs))

      case x @ Let(id, value, body) =>
        simpleRound(body)

      case n =>
        throw new IllegalArgumentException("Unknown expression. Simplifying expression failed. " + n.toString)
    }

//    reporter.debug(s"COMPLETE EXPR is $ex")
    while (!ready){
      // simplify expression
      val tmpresExpr = simpleRound(resExpr)
      // check if expression has changed after simplifications
      ready = expressionsEqual(resExpr, tmpresExpr)
      resExpr = tmpresExpr
    }
//    reporter.debug(s"SIMPLIFIED EXPR is $resExpr")
    resExpr
  }

  /**
    * Simplifies expression using simple arithmetic rules
    * @param ex expression to be simplified
    * @return simplified expression
    */
  def moreSimplify(ex:Expr): Expr = {
    var ready = false
    var resExpr = easySimplify(ex)

    def simpleRound(e: Expr):Expr = e match {
      case x @ Delta(id) => x
      case x @ Epsilon(id) => x
      case x @ Variable(id) => x
      case x @ RealLiteral(r) => x

      case x @ Plus(lhs , rhs) if expressionsEqual(lhs, rhs) =>
        Times(RealLiteral(Rational.fromReal(2)), simpleRound(lhs))

      case x @ Plus(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.+(rhsIn))
          case (_, RealLiteral(rhsIn)) =>
            Plus(rhs, simpleRound(lhs))
//          // a/b + c/b = (a + c)/b
//          case (Division(nom1, denom1), Division(nom2, denom2))
//            if expressionsEqual(denom1, denom2)=>
//            Division(Plus(simpleRound(nom1),simpleRound(nom2)),
//              simpleRound(denom1))
//          // a/b + c/d = ad + bc / bd
//          case (Division(nom1, denom1), Division(nom2, denom2)) =>
//            Division(Plus(simpleRound(Times(nom1, denom2)),simpleRound(Times(nom2, denom1))),
//              simpleRound(Times(denom1, denom2)))
          // (a * b) + (a * c) = a * (b + c)
          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
            if expressionsEqual(lhs1, lhs2) =>
            Times(simpleRound(lhs1),
                Plus(simpleRound(rhs1), simpleRound(rhs2)))

          // (a * b) + (c * a) = a * (b + c)
          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
            if expressionsEqual(lhs1, rhs2)=>
            Times(simpleRound(lhs1),
              Plus(simpleRound(rhs1), simpleRound(lhs2)))
          // (a * b) + (c * b) = b * (a + c)
          case (Times(a, b), Times(c, bb))
            if expressionsEqual(b, bb)=>
            Times(Plus(simpleRound(a), simpleRound(c)),
              simpleRound(b))
          // (a * b) + (b * c) = b * (a + c)
          case (Times(a, b), Times(bb, c))
            if expressionsEqual(b, bb)=>
            Times(Plus(simpleRound(a), simpleRound(c)),
              simpleRound(b))
          // (a * b) + (a) = a * (b + 1)
          case (Times(a, b), _ )
            if expressionsEqual(a, rhs)=>
            Times(simpleRound(a),
              Plus(simpleRound(b), one))
          // (a * b) + (b) = b * (a + 1)
          case (Times(a, b), _ )
            if expressionsEqual(b, rhs)=>
            Times(simpleRound(b),
              Plus(simpleRound(a), one))

          // (a) + (a * b) = a * (b + 1)
          case (_ , Times(a, b))
            if expressionsEqual(lhs, a)=>
            Times(simpleRound(a),
              Plus(simpleRound(b), one))
          // (b) + (a * b) = b * (a + 1)
          case (_ , Times(a, b))
            if expressionsEqual(lhs, b)=>
            Times(simpleRound(b),
              Plus(simpleRound(a), one))
          case _ =>
            Plus(simpleRound(lhs), simpleRound(rhs))
        }

      case x @ Minus(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.-(rhsIn))
          case (_, RealLiteral(rhsIn)) =>
            Plus(UMinus(rhs), simpleRound(lhs))
            //TODO copy fro plus (ab)+a etc
//          // a - (-b) = a + b
//          case (_, UMinus(rhsIn)) =>
//            Plus(lhs, rhsIn)
//          // x - (x + y) = -y
//          case (_, Plus(lhsIn, rhsIn)) if expressionsEqual(lhs, lhsIn) =>
//            UMinus(simpleRound(rhsIn))
//          // x - (y + x) = -y
//          case (_, Plus(lhsIn, rhsIn)) if expressionsEqual(lhs, rhsIn) =>
//            UMinus(simpleRound(lhsIn))
//          // (x + y) - x = y
//          case (Plus(lhsIn, rhsIn), _) if expressionsEqual(rhs, lhsIn) =>
//            simpleRound(rhsIn)
//          // (y + x) - x = y
//          case (Plus(lhsIn, rhsIn), _) if expressionsEqual(rhs, rhsIn) =>
//            simpleRound(lhsIn)
//          // a/b - c/b = (a - c)/b
//          case (Division(nom1, denom1), Division(nom2, denom2))
//            if expressionsEqual(denom1, denom2)=>
//            Division(Minus(simpleRound(nom1),simpleRound(nom2)),
//              simpleRound(denom1))
//          // a/b - c/d = ad - bc / bd
//          case (Division(nom1, denom1), Division(nom2, denom2)) =>
//            Division(Minus(simpleRound(Times(nom1, denom2)),simpleRound(Times(nom2, denom1))),
//              simpleRound(Times(denom1, denom2)))
//          // (a * b) - (a * c) = a * (b - c)
//          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
//            if expressionsEqual(lhs1, lhs2)=>
//            Times(simpleRound(lhs1),
//              Minus(simpleRound(rhs1), simpleRound(rhs2)))
//          // (a * b) - (c * a) = a * (b - c)
//          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
//            if expressionsEqual(lhs1, rhs2)=>
//            Times(simpleRound(lhs1),
//              Minus(simpleRound(rhs1), simpleRound(lhs2)))
//          // (a * b) - (c * b) = b * (a - c)
//          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
//            if expressionsEqual(rhs1, rhs2)=>
//            Times(simpleRound(rhs1),
//              Minus(simpleRound(lhs1), simpleRound(lhs2)))
//          // (a * b) - (b * c) = b * (a - c)
//          case (Times(lhs1, rhs1), Times(lhs2, rhs2))
//            if expressionsEqual(rhs1, lhs2)=>
//            Times(simpleRound(rhs1),
//              Minus(simpleRound(lhs1), simpleRound(rhs2)))
          case _ =>
            Minus(simpleRound(lhs), simpleRound(rhs))
        }

      case x @ Times(lhs , rhs) if expressionsEqual(lhs, rhs) =>
        val tmp = Pow(simpleRound(lhs), two)
        tmp

      case x @ Times(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn.*(rhsIn))

          case (Times(_, _), RealLiteral(rhsIn)) =>
            val list = listElements(lhs)
            multiplyRealLiterals(rhsIn, list)

          case (RealLiteral(lhsIn), Times(_, _)) =>
            val list = listElements(rhs)
            multiplyRealLiterals(lhsIn, list)

//          case (UMinus(lhsIn), UMinus(rhsIn)) =>
//            Times(simpleRound(lhsIn), simpleRound(rhsIn))
//          case (_, UMinus(rhsIn)) =>
//            UMinus(Times(simpleRound(lhs), simpleRound(rhsIn)))
//          case (UMinus(lhsIn), _) =>
//            UMinus(Times(simpleRound(lhsIn), simpleRound(rhs)))
//          // a/b * c/a = c/b
//          case (Division(nom1, denom1), Division(nom2, denom2))
//            if expressionsEqual(nom1, denom2)=>
//            Division(simpleRound(nom2),
//              simpleRound(denom1))
//          // a/b * b/c = a/c
//          case (Division(nom1, denom1), Division(nom2, denom2))
//            if expressionsEqual(nom2, denom1)=>
//            Division(simpleRound(nom1),
//              simpleRound(denom2))
          // a/b * c/d = ac / bd
          case (Division(nom1, denom1), Division(nom2, denom2)) =>
            Division(Times(simpleRound(nom1), simpleRound(nom2)),
              Times(simpleRound(denom1), simpleRound(denom2)))
          // 1/b * c = c/b
          case (Division(nom, denom), _) if nom == one =>
            Division(simpleRound(rhs),
              simpleRound(denom))
          // a/b * c = ac/b
          case (Division(nom, denom), _) =>
            Division(Times(simpleRound(nom), simpleRound(rhs)),
              simpleRound(denom))
          // c * a/b= ca/b
          case (_, Division(nom, denom)) =>
            Division(Times(simpleRound(lhs), simpleRound(nom)),
              simpleRound(denom))

          case (Times(lhsIn, rhsIn), _) if expressionsEqual(rhsIn, rhs) =>
            Times(lhsIn, Pow(simpleRound(rhs), two))

          case (Times(lhsIn, Pow(rhsIn, RealLiteral(n))), _) if expressionsEqual(rhsIn, rhs) =>
            Times(lhsIn, Pow(simpleRound(rhs), RealLiteral(n + Rational.one)))

          case (Pow(lhsIn, RealLiteral(n1)), Pow(rhsIn, RealLiteral(n2)))
            if expressionsEqual(lhsIn, rhsIn) =>
            Pow(simpleRound(lhsIn), RealLiteral(n1 + n2))

          case (Pow(lhsIn, RealLiteral(n)), _ )
            if expressionsEqual(lhsIn, rhs) =>
            Pow(simpleRound(lhsIn), RealLiteral(n + Rational.one))

          case (_, Pow(rhsIn, RealLiteral(n)))
            if expressionsEqual(lhs, rhsIn) =>
            Pow(simpleRound(rhsIn), RealLiteral(n + Rational.one))

          case _ =>
            Times(simpleRound(lhs), simpleRound(rhs))
        }

       case x @ Division(lhs, rhs) =>
        (lhs, rhs) match {
          case (RealLiteral(lhsIn), RealLiteral(rhsIn)) =>
            RealLiteral(lhsIn./(rhsIn))
          case (_, Division(lhsIn, rhsIn)) if lhs == one =>
            Division(simpleRound(rhsIn), simpleRound(lhsIn))
          // a/b / c/d = ad / bc
          case (Division(a, b), Division(c, d)) =>
//            reporter.warning("swap divisions")
            Division(Times(simpleRound(a), simpleRound(d)),
              Times(simpleRound(b), simpleRound(c)))
//          case (Division(nom, denom), _) =>
//            Division(simpleRound(nom),
//              Times(simpleRound(denom), simpleRound(rhs)))
//          case (_, Division(nom, denom)) =>
//            Division(Times(simpleRound(lhs), simpleRound(denom)),
//              simpleRound(nom))

          case (Times(_, _), Times(_,_)) =>
            cancelNominators(simpleRound(lhs), simpleRound(rhs))
          case (Times(_, _), Pow(_,_)) =>
            cancelNominators(simpleRound(lhs), simpleRound(rhs))
          case (Pow(_, _), Times(_,_)) =>
            cancelNominators(simpleRound(lhs), simpleRound(rhs))
          case (Pow(_, _), Pow(_,_)) =>
            cancelNominators(simpleRound(lhs), simpleRound(rhs))
          case _ =>
            Division(simpleRound(lhs), simpleRound(rhs))
        }

      case x @ Pow(Pow(lhs, RealLiteral(n1)), RealLiteral(n)) =>
        Pow(simpleRound(lhs), RealLiteral(n * n1))

      case x @ Pow(lhs, n) =>
        Pow(simpleRound(lhs), n)

      case x @ UMinus(lhs) =>
        lhs match {
          case UMinus(lhs2) => simpleRound(lhs2)
          case RealLiteral(Rational.zero) => zero
          case Division(lhsIn, rhsIn) => Division(UMinus(simpleRound(lhsIn)), simpleRound(rhsIn))
          case _ => UMinus(simpleRound(lhs))
        }

      case x @ Sqrt(lhs) =>
        Sqrt(simpleRound(lhs))

      case x @ Let(id, value, body) =>
        simpleRound(body)

      case n =>
        throw new IllegalArgumentException("Unknown expression. Simplifying expression failed. " + n.toString)
    }

    reporter.debug(s"COMPLETE EXPR is $resExpr")
    while (!ready){
      // simplify expression
      val tmpresExpr = simpleRound(resExpr)
      // check if expression has changed after simplifications
      ready = expressionsEqual(resExpr, tmpresExpr)
      resExpr = tmpresExpr
    }
    reporter.debug(s"SIMPLIFIED EXPR is $resExpr")
    resExpr
  }

  /**
    * Computes partial derivative w.r.t. passed parameter
    * @param e expression for which derivative is computed
    * @param wrt Delta id w.r.t. which derivative is computed
    * @return expression
    */
  def getPartialDerivative(e: Expr, wrt: Identifier): Expr = e match {
    case x @ Delta(id) if wrt.equals(id) =>
      one
    case x @ Delta(id) => zero
      // for the remainder term we will also compute partial derivative wrt Epsilons
    case x @ Epsilon(id) if wrt.equals(id) =>
      one
    case x @ Epsilon(id) => zero

    case x @ Variable(id) => zero
    case x @ RealLiteral(r) => zero
    case x @ UMinus(in) => UMinus(getPartialDerivative(in, wrt))

    case z @ Plus(x, y) =>
      Plus(getPartialDerivative(x, wrt), getPartialDerivative(y, wrt))

    case z @ Minus(x, y) =>
      Minus(getPartialDerivative(x, wrt), getPartialDerivative(y, wrt))

    case z @ Times(x, y) if containsVariables(x, wrt) && containsVariables(y, wrt) =>
      Plus(Times(x, getPartialDerivative(y, wrt)), Times(getPartialDerivative(x, wrt), y))

    case z @ Times(x, y) if containsVariables(x, wrt) =>
      // y is constant
      Times(getPartialDerivative(x, wrt), y)

    case z @ Times(x, y) if containsVariables(y, wrt) =>
      // x is constant
      Times(x, getPartialDerivative(y, wrt))

    case z @ Times(x, y) =>
      // x, y are both constants
      zero

    case z @ Division(x, y) if containsVariables(x, wrt) && containsVariables(y, wrt) =>
      Division(Minus(Times(getPartialDerivative(x, wrt), y), Times(getPartialDerivative(y, wrt), x)),
        Times(y, y))

    case z @ Division(x, y) if containsVariables(x, wrt) =>
      // y is constant
      Times(Division(one, y), getPartialDerivative(x, wrt))

    case z @ Division(x, y) if containsVariables(y, wrt) =>
      // x is constant
      // (1/y)' = -y' / y^2
      Times(x, Division(UMinus(getPartialDerivative(y, wrt)), Times(y, y)))

    case z @ Division(x, y) => zero

      //TODO fix the Pow if we allow expressions in power
//    case z @ Pow(x, n) if containsVariables(x, wrt) && containsVariables(n, wrt)=>
//      ???
//    case z @ Pow(x, n) if containsVariables(n, wrt) =>
//      ???

    case z @ Pow(x, RealLiteral(n)) if containsVariables(x, wrt) =>
      assert(n > Rational.one)
//      assert(n.isValidInt)
      if (n == Rational.two)
        getPartialDerivative(x, wrt)
      else
        Times(RealLiteral(n),
          Pow(getPartialDerivative(x, wrt), RealLiteral(n-Rational.one)))

//    case z @ Pow(x, n) => zero

    case z @ Sqrt(x) if containsVariables(x, wrt) =>
      Division(getPartialDerivative(x, wrt), Times(two, Sqrt(x)))
    case z @ Sqrt(x) => zero

    case z @ Let(x, value, body) if containsVariables(body, wrt) =>
      getPartialDerivative(body, wrt)

    case z @ Let(x, value, body) => zero

    case z => throw new IllegalArgumentException(s"Unknown expression $z. Computing derivative failed")
  }


  /**
    * Computes partial derivatives w.r.t. each delta in the relative error expression
    * @param e relative error expression
    * @return set of tuples :
    *         (partial derivative,
    *         id of Delta w.r.t. which this derivative has been computed)
    */
  def getDerivative(e: Expr): Seq[(Expr, Identifier)]  = {
    // we only compute partial derivatives with respect to deltas
    var taylorSeries: Seq[(Expr, Identifier)] = Seq.empty
    reporter.debug(s"rel error expression $e")
//    val simple = simplify(e)
    deltasOf(e).foreach(wrt => {
      val tmp = getPartialDerivative(e, wrt.id) //recurseDerivative(e, wrt.id)
      taylorSeries = taylorSeries :+ (easySimplify(tmp), wrt.id)
    })
    taylorSeries
  }

  /**
    * Computes second derivative and evaluates the range with intervals
    * @param e relative error expression
    * @param intervals map with with intervals for input vars
    * @return max estimated value for the
    */
  def getTaylorRemainder(e:Expr, intervals: Seq[Map[Identifier, Interval]]): Option[Rational] = {

    val simple = easySimplify(e)
    reporter.debug("WHAT is INITIALLY in the listFailed " + listFailed.map(removeDeltasFromMap).map(_.keySet.map(_.globalId)))

    // both deltas and epsilons
    val deltas:Set[Expr with Identifiable] = deltasOf(simple) ++ epsilonsOf(simple)
    //todo fix fold or map
    reporter.warning("Amount of runs for the first remainder term "+ Math.pow(deltas.size, 2)*intervals.size)
    val firstTerm = deltas.map(wrt => {
//    val wrtSt = System.currentTimeMillis()
      // first derivative
      val tmp = easySimplify(
        getPartialDerivative(simple, wrt.getId))
      val tmpValOut = deltas.par.map(wrtIn => {
//        val start = System.currentTimeMillis()
        // second derivative
        if (containsVariables(tmp, wrtIn.getId)) {
          val tmpIn = easySimplify(Times(wrt, Times(wrtIn, moreSimplify(
            getPartialDerivative(tmp, wrtIn.getId)))))

//          reporter.warning(s"=============WRT $wrt * $wrtIn===============")
//          reporter.warning(s"Second derivative $tmpIn")
          val tmpVal = tmpIn match {
            case x @ Delta(id) => Seq(Some(maxAbs(x.interval)))
            case x @ Epsilon(id) => Seq(Some(maxAbs(x.interval)))
            case _ =>
              intervals.par.map(x => {
                val interval = evaluateInterval (tmpIn, x)
                if (interval.isDefined)
                  Some (maxAbs (interval.get) )
                else {
                  if (! listFailed.contains (x) ) {
                    listFailed = listFailed :+ x
                  }
                  None
                }
              })
            }
//          reporter.warning(s"finished iteration for $wrt, $wrtIn; time:" + (System.currentTimeMillis() - start))
          tmpVal.max(optionAbsOrdering)
        }
        else None
      })
      // remove a dummy entry from the list
      reporter.debug(s"before "+ listFailed.map(removeDeltasFromMap))
      listFailed = listFailed.filter(x => removeDeltasFromMap(x).keySet == removeDeltasFromMap(intervals.head).keySet)
      reporter.debug(s"after "+ listFailed.map(removeDeltasFromMap))
//      reporter.warning(s"done for $wrt in " + (System.currentTimeMillis()-wrtSt))
      tmpValOut.fold(None)(sumOption)
    })

    val epsilons:Set[Epsilon] = epsilonsOf(simple)
    val secondTerm = epsilons.par.map(wrt => {
      val tmp = Times(wrt, Times(wrt, //simplify(
        getPartialDerivative(simple, wrt.getId)))//)
      val tmpVal = intervals.par.map(x => {
        val interval = evaluateInterval(replaceDeltasWithZeros(tmp), x)
        if (interval.isDefined)
          Some(maxAbs(interval.get))
        else None
      })
      tmpVal.max(optionAbsOrdering)
    })

    val first = timesOption(Some(Rational(1, 2)), firstTerm.fold(None)(sumOption))
    val second = secondTerm.fold(None)(sumOption)
    sumOption(first, second)
  }

  /**
    * This function checks whether the expression contains a Delta variable,
    * w.r.t. which we compute partial derivative
    *
    * @param e - expression
    * @param wrt - Delta or Epsilon variable
    * @return boolean
    */
  private def containsVariables(e: Expr, wrt: Identifier):Boolean = {

    def recurse(ex:Expr, wrt: Identifier): Boolean = ex match {
      // if we compute w.r.t. this delta, it's a var
      case x @ Delta(id) if wrt.equals(id) =>
        true
      // otherwise a constant
      case x @ Delta(id) =>
        false
      // if we compute w.r.t. this epsilon, it's a var
      case x @ Epsilon(id) if wrt.equals(id) =>
        true
      // otherwise a constant
      case x @ Epsilon(id) =>
        false

      // all variables are constants
      case x @ Variable(id) =>
        false

      case x @ RealLiteral(r) =>
        false

      case x @ Plus(lhs, rhs) =>
        recurse(lhs, wrt) || recurse(rhs, wrt)

      case x @ Minus(lhs, rhs) =>
        recurse(lhs, wrt) || recurse(rhs, wrt)

      case x @ Times(lhs, rhs) =>
        recurse(lhs, wrt) || recurse(rhs, wrt)

      case x @ Division(lhs, rhs) =>
        recurse(lhs, wrt) || recurse(rhs, wrt)

      case x @ Pow(lhs, rhs) =>
        recurse(lhs, wrt) || recurse(rhs, wrt)

      case x @ UMinus(lhs) =>
        recurse(lhs, wrt)

      case x @ Sqrt(lhs) =>
        recurse(lhs, wrt)

      case x @ Let(id, value, body) =>
        recurse(body, wrt)

      case n =>
        throw new IllegalArgumentException("Unknown expression. Check expression for variables failed" + n.toString)
    }

    recurse(e, wrt)
  }

  /**
    * Compares two expressions
    * @param e1 - first expression for comparison
    * @param e2 - second expression for comparison
    * @return true - if expressions are equal, false - otherwise
    */
  private def expressionsEqual(e1:Expr, e2: Expr):Boolean = (e1, e2) match{
    case (RealLiteral(r1), RealLiteral(r2)) => r1.equals(r2)
    case (Delta(r1), Delta(r2)) => r1.equals(r2)
    case (Epsilon(r1), Epsilon(r2)) => r1.equals(r2)
    case (Variable(r1), Variable(r2)) => r1.equals(r2)
    case (Sqrt(r1), Sqrt(r2)) => expressionsEqual(r1, r2)
    case (UMinus(r1), UMinus(r2)) => expressionsEqual(r1, r2)
    case (Let(id1, val1, body1), Let(id2, val2, body2)) =>
      id1.equals(id2) && val1.equals(val2) && expressionsEqual(body1, body2)
    //FIXME handle different order of terms inside Plus()
//    case (Plus(lhs1, Plus(lhsIn1, rhsIn1)), Plus(Plus(lhsIn2, rhsIn2), rhs2)) =>
//      expressionsEqual(lhs1, lhsIn2) && expressionsEqual(lhsIn1, lhsIn2) && expressionsEqual(rhsIn1, rhs2)
    case (Plus(lhs1, rhs1), Plus(lhs2, rhs2)) =>
      (expressionsEqual(lhs1, lhs2) && expressionsEqual(rhs1, rhs2)) ||
        (expressionsEqual(lhs1, rhs2) && expressionsEqual(rhs1, lhs2))
    // FIXME handle different order of terms inside Times()
//    case (Times(lhs1, Times(lhsIn1, rhsIn1)), Times(Times(lhsIn2, rhsIn2), rhs2)) =>
//      expressionsEqual(lhs1, lhsIn2) && expressionsEqual(lhsIn1, lhsIn2) && expressionsEqual(rhsIn1, rhs2)
    case (Times(lhs1, rhs1), Times(lhs2, rhs2)) =>
      (expressionsEqual(lhs1, lhs2) && expressionsEqual(rhs1, rhs2)) ||
        (expressionsEqual(lhs1, rhs2) && expressionsEqual(rhs1, lhs2))
    case (Minus(lhs1, rhs1), Minus(lhs2, rhs2)) =>
      // FIXME handle reverse order
      expressionsEqual(lhs1, lhs2) && expressionsEqual(rhs1, rhs2)
    case (Division(lhs1, rhs1), Division(lhs2, rhs2)) =>
      expressionsEqual(lhs1, lhs2) && expressionsEqual(rhs1, rhs2)
    case (Pow(x1, n1), Pow(x2, n2)) =>
      expressionsEqual(x1, x2) && expressionsEqual(n1, n2)
    case _ => false
  }

  private def evaluateInterval(expr: Expr, _intMap: collection.immutable.Map[Identifier, Interval] = Map.empty): Option[Interval] = {
    try {
      //TODO remove it! only for Doppler evaluation!
//      Some(Evaluators.evalSMT(expr,_intMap.map({ case (id, int) => (id -> SMTRange(Variable(id), int)) })).toInterval)
      Some(Evaluators.evalInterval(expr,_intMap))
    }
    catch {
      case e: DivisionByZeroException => None
    }

  }

  def getADelta: Delta = {
    val tmp = FreshIdentifier(deltaName, RealType, alwaysShowUniqueID = true).toDeltaVariable
    tmp.interval = Interval( -Float64.machineEpsilon, Float64.machineEpsilon)
    tmp
  }

  def getAnEpsilon: Epsilon = {
    val tmp = FreshIdentifier(epsilonName, RealType, alwaysShowUniqueID = true).toEpsilonVariable
    tmp.interval = Interval( -Float64.denormalsError, Float64.denormalsError)
    tmp
  }

  def mapDeltasToVars(e: Expr):Map[Variable, Delta] = {
    variablesOf(e).map(x =>{
            (Variable(x) -> getADelta)
    }).toMap
  }
  def mapEpsilonsToVars(e: Expr):Map[Variable, Epsilon] = {
    variablesOf(e).map(x =>{
            (Variable(x) -> getAnEpsilon)
    }).toMap
  }

  def deltaAbstract(e: Expr, deltaToVarMap: Map[Variable, Delta],
                   epsToVarMap: Map[Variable,Epsilon]): Expr = (e: @unchecked) match {

    // x -> x * (1 + delta)
    case x: Variable if trackRoundoffErrs && denormals =>
      // initial error
      if (deltaToVarMap.contains(x))
        if (epsToVarMap.contains(x))
          Plus(Times(x, Plus(one, deltaToVarMap(x))), epsToVarMap(x))
        else
          Plus(Times(x, Plus(one, deltaToVarMap(x))), getAnEpsilon)
      else
        if (epsToVarMap.contains(x))
          Plus(Times(x, Plus(one, getADelta)), epsToVarMap(x))
        else
          Plus(Times(x, Plus(one, getADelta)), getAnEpsilon)

    // x -> x * (1 + delta)
    case x: Variable if trackRoundoffErrs =>
      // initial error
      if (deltaToVarMap.contains(x))
        Times(x, Plus(one, deltaToVarMap(x)))
      else
        Times(x, Plus(one, getADelta))

    case x: Variable =>
      // no initial error
      x

    // c -> c * (1 + delta)   ## except if representable directly
    case x: RealLiteral =>
      // No initial error
      //Times(x, Plus(RealLiteral(one), getADelta))
      x

    // x - > x   ## negation does not incur any error
    case UMinus(x) =>
      UMinus(deltaAbstract(x, deltaToVarMap, epsToVarMap))


    // (x + y) -> (x + y)(1 + delta)
    case z @ Plus(x, y) =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
      Times(Plus(xDelta, yDelta),
        Plus(one,
          getADelta))

    // (x - y) -> (x - y)(1 + delta)
    case z @ Minus(x, y) =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
      Times(Minus(xDelta, yDelta),
        Plus(one,
          getADelta))

    // (x * y) -> (x * y)(1 + delta) + eps
    case z @ Times(x, y) if denormals =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
      Plus(
        Times(Times(xDelta, yDelta),
        Plus(one,
          getADelta)), getAnEpsilon)

    // (x * y) -> (x * y)(1 + delta)
    case z @ Times(x, y) =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
        Times(Times(xDelta, yDelta),
        Plus(one,
          getADelta))

    // (x / y) -> ( x / y)(1 + delta) + eps
    case z @ Division(x, y) if denormals =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
      Plus(
        Times(Division(xDelta, yDelta),
        Plus(one,
          getADelta)), getAnEpsilon)

    // (x / y) -> ( x / y)(1 + delta)
    case z @ Division(x, y) =>
      val xDelta = deltaAbstract(x, deltaToVarMap, epsToVarMap)
      val yDelta = deltaAbstract(y, deltaToVarMap, epsToVarMap)
        Times(Division(xDelta, yDelta),
        Plus(one,
          getADelta))

    // sqrt(x) = sqrt(x)(1 + delta) + eps
    case z @ Sqrt(x) if denormals =>
      Plus(
        Times(Sqrt(deltaAbstract(x, deltaToVarMap, epsToVarMap)),
        Plus(one, getADelta))
        ,getAnEpsilon)

    // sqrt(x) = sqrt(x)(1 + delta)
    case z @ Sqrt(x) =>
        Times(Sqrt(deltaAbstract(x, deltaToVarMap, epsToVarMap)),
        Plus(one, getADelta))

    //todo add Pow ?

    case z @ Let(x, value, body) =>
      Let(x, deltaAbstract(value, deltaToVarMap, epsToVarMap),
        deltaAbstract(body, deltaToVarMap, epsToVarMap))

    case z =>
      throw new IllegalArgumentException(s"Unknown expression $z. Parsing failed")
  }

  /**
    * Removes deltas from input maps to avoid not necessary
    * sub-division and reduce map dimension
    * @param in map for variables and deltas
    * @return map for variables
    */
  def removeDeltasFromMap(in: Map[Identifier, Interval]): Map[Identifier, Interval] ={
    in.filterKeys(x => !x.isDeltaId && !x.isEpsilonId)
  }

  /**
    * replaces all deltas in the expression with zeros
    * @param expr - expression to replace deltas and epsilons in
    * @return expr - expression with RealLiteral(0.) instead of deltas and epsilons
    */
  def replaceDeltasWithZeros(expr: Expr): Expr = expr match {
    case x @ Delta(id) => zero
    case x @ Epsilon(id) => zero
    case x @ Variable(id) => x
    case x @ RealLiteral(r) => x
    case UMinus(x) =>
      UMinus(replaceDeltasWithZeros(x))

    case z @ Plus(x, y) =>
      Plus(replaceDeltasWithZeros(x), replaceDeltasWithZeros(y))

    case z @ Minus(x, y) =>
      Minus(replaceDeltasWithZeros(x), replaceDeltasWithZeros(y))

    case z @ Times(x, y) =>
      Times(replaceDeltasWithZeros(x), replaceDeltasWithZeros(y))

    case z @ Division(x, y) =>
      Division(replaceDeltasWithZeros(x), replaceDeltasWithZeros(y))

    case z @ Pow(x, n) =>
      Pow(replaceDeltasWithZeros(x), replaceDeltasWithZeros(n))

    case z @ Sqrt(x) =>
      Sqrt(replaceDeltasWithZeros(x))

    case z @ Let(x, value, body) =>
      Let(x, value, replaceDeltasWithZeros(body))

    case z => throw new IllegalArgumentException(s"Unknown expression $z. Replacing deltas failed")
  }

  def sumOption(a: Option[Rational],
                b: Option[Rational]
//  TODO fix to make more generic            ,op: (Rational, Rational) => Rational
               ): Option[Rational] = {
// fixme is this better?   a.getOrElse(Rational.zero) + b.getOrElse(Rational.zero)
    if (a.isDefined)
      if (b.isDefined)
        Some(a.get + b.get)
      else
        a
    else
      if (b.isDefined)
        b
      else None
  }
  def timesOption(a: Option[Rational],
                 b: Option[Rational]): Option[Rational] = {
    if (a.isDefined && b.isDefined)
        Some(a.get * b.get)
    else
      None
  }
}


