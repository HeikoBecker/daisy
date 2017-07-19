
/*
  The contents of this file is heaviy influenced and/or partly taken from
  the Leon Project which is released under the BSD 2 clauses license.
  See file LEON_LICENSE or go to https://github.com/epfl-lara/leon
  for full license details.
 */

package daisy
package lang

import scala.collection.immutable.Seq

import Extractors._
import Trees._
import Constructors._

import lang.Identifiers._

/**
  Various useful functions for manipulating trees.
 */
object TreeOps {

  /** Does a right tree fold
    *
    * A right tree fold applies the input function to the subnodes first (from left
    * to right), and combine the results along with the current node value.
    *
    * @param f a function that takes the current node and the seq
    *        of results form the subtrees.
    * @param e The Expr on which to apply the fold.
    * @return The expression after applying `f` on all subtrees.
    * @note the computation is lazy, hence you should not rely on side-effects of `f`
    */
  def fold[T](f: (Expr, Seq[T]) => T)(e: Expr): T = {
    val rec = fold(f) _
    val Operator(es, _) = e

    //Usages of views makes the computation lazy. (which is useful for
    //contains-like operations)
    f(e, Seq(es.view.map(rec):_*))
    //f(e, es.map(rec))
  }

  /** Pre-traversal of the tree.
    *
    * Invokes the input function on every node '''before''' visiting
    * children. Traverse children from left to right subtrees.
    *
    * e.g.
    * {{{
    *   Add(a, Minus(b, c))
    * }}}
    * will yield, in order:
    * {{{
    *   f(Add(a, Minus(b, c))); f(a); f(Minus(b, c)); f(b); f(c)
    * }}}
    *
    * @param f a function to apply on each node of the expression
    * @param e the expression to traverse
    */
  def preTraversal(f: Expr => Unit)(e: Expr): Unit = {
    val rec = preTraversal(f) _
    val Operator(es, _) = e
    f(e)
    es.foreach(rec)
  }


  /** Post-traversal of the tree.
    *
    * Invokes the input function on every node '''after''' visiting
    * children.
    *
    * e.g.
    * {{{
    *   Add(a, Minus(b, c))
    * }}}
    * will yield, in order:
    * {{{
    *   f(a), f(b), f(c), f(Minus(b, c)), f(Add(a, Minus(b, c)))
    * }}}
    *
    * @param f a function to apply on each node of the expression
    * @param e the expression to traverse
    */
  def postTraversal(f: Expr => Unit)(e: Expr): Unit = {
    val rec = postTraversal(f) _
    val Operator(es, _) = e
    es.foreach(rec)
    f(e)
  }

  /** Post-transformation of the tree.
    *
    * Takes a partial function of replacements.
    * Substitutes '''after''' recursing down the trees.
    *
    * Supports two modes :
    *
    *   - If applyRec is false (default), will only substitute once on each level.
    *   e.g.
    *   {{{
    *     Add(a, Minus(b, c)) with replacements: Minus(b,c) -> z, Minus(e,c) -> d, b -> e
    *   }}}
    *   will yield:
    *   {{{
    *     Add(a, Minus(e, c))
    *   }}}
    *
    *   - If applyRec is true, it will substitute multiple times on each level:
    *   e.g.
    *   {{{
    *     Add(a, Minus(b, c)) with replacements: Minus(e,c) -> d, b -> e, d -> f
    *   }}}
    *   will yield:
    *   {{{
    *     Add(a, f)
    *   }}}
    *
    * @note The mode with applyRec true can diverge if f is not well formed (i.e. not convergent)
    */
  def postMap(f: Expr => Option[Expr], applyRec : Boolean = false)(e: Expr): Expr = {
    val rec = postMap(f, applyRec) _

    val Operator(es, builder) = e
    val newEs = es.map(rec)
    val newV = {
      if ((newEs zip es).exists { case (bef, aft) => aft ne bef }) {
        builder(newEs).copiedFrom(e)
      } else {
        e
      }
    }

    /*if (applyRec) {
      // Apply f as long as it returns Some()
      fixpoint { e : Expr => f(e) getOrElse e } (newV)
    } else {*/
      f(newV) getOrElse newV
    /*}*/

  }

  /** Checks if the predicate holds in some sub-expression */
  def exists(matcher: Expr => Boolean)(e: Expr): Boolean = {
    fold[Boolean]({ (e, subs) =>  matcher(e) || subs.contains(true) } )(e)
  }

  /** Computes the negation of a boolean formula, with some simplifications. */
  def negate(expr: Expr) : Expr = {
    //require(expr.getType == BooleanType)
    (expr match {
      case Let(i,b,e) => Let(i,b,negate(e))
      case Not(e) => e
      case Implies(e1,e2) => and(e1, negate(e2))
      case Or(exs) => and(exs map negate: _*)
      case And(exs) => or(exs map negate: _*)
      case LessThan(e1,e2) => GreaterEquals(e1,e2)
      case LessEquals(e1,e2) => GreaterThan(e1,e2)
      case GreaterThan(e1,e2) => LessEquals(e1,e2)
      case GreaterEquals(e1,e2) => LessThan(e1,e2)
      case IfExpr(c,e1,e2) => IfExpr(c, negate(e1), negate(e2))
      case BooleanLiteral(b) => BooleanLiteral(!b)
      case e => Not(e)
    }).setPos(expr)
  }

  /** Replaces bottom-up sub-expressions by looking up for them in a map */
  def replace(substs: Map[Expr,Expr], expr: Expr) : Expr = {
    postMap(substs.lift)(expr)
  }

  /** Returns the set of free variables in an expression */
  def variablesOf(expr: Expr): Set[Identifier] = {
    fold[Set[Identifier]] {
      case (e, subs) =>
        val subvs = subs.flatten.toSet
        e match {
          case Variable(i) => subvs + i
          case Let(i, _, _) => subvs - i
          case Lambda(args, _) => subvs -- args.map(_.id)
          case _ => subvs
        }
    }(expr)
  }

  /** Returns the set of Delta variables in an expression */
  def deltasOf(expr: Expr): Set[Delta] = {
    fold[Set[Delta]] {
      case (e, subs) =>
        val subvs = subs.flatten.toSet
        e match {
          case x @ Delta(i) => subvs + x
          case _ => subvs
        }
    }(expr)
  }

  /** Returns the set of Epsilon variables in an expression */
  def epsilonsOf(expr: Expr): Set[Epsilon] = {
    fold[Set[Epsilon]] {
      case (e, subs) =>
        val subvs = subs.flatten.toSet
        e match {
          case x @ Epsilon(i) => subvs + x
          case _ => subvs
        }
    }(expr)
  }

  /**
    A term is an expression which (for our purposes) does not contain
    propositional logic connectives.
   */
  def isBooleanTerm(e: Expr): Boolean = e match {
    case GreaterEquals(_, _) | GreaterThan(_,_) | LessThan(_, _) | LessEquals(_,_) => true
    case Equals(_, _) => true
    case AbsError(_, _) => true
    case BooleanLiteral(_) => true
    case _ => false
  }

  def size(e: Expr): Int = e match {
    case Operator(es, _) =>
      es.map(size).sum + 1
  }
}