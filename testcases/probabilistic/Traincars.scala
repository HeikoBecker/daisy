import daisy.lang._
import Real._

object Traincars {

  def outTraincar1(s0: Real, s1: Real, s2: Real) = {
    require(0 <= s0 && s0 <= 4.6 && 0 <= s1 && s1 <= 10 && 0 <= s2 && s2 <= 10)
    (-3.795323e2) * s0 + (-5.443608e2) * s1 + 92.729 * s2 + 4.5165916241610748e3
  }

  def outTraincar2(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real) = {
    require(-2 <= s0 && s0 <= 9 && -2 <= s1 && s1 <= 9 && 0 <= s2 && s2 <= 10 && 0 <= s3 && s3 <= 10 && 0 <= s4 && s4 <= 10)
    (-2.9829300077345002E+00) * s0 + 2.8399180104656203E+01 * s1 + (-1.5201325206209998E+02) * s2 +
     3.5623840930436779E+02 * s3 + (-2.0636383424191501E+02) * s4 + 2.1660394157545902E+01
  }

  def outTraincar3(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real) = {
    require(-3 <= s0 && s0 <= 5 && -3 <= s1 && s1 <= 5 && -3 <= s2 && s2 <= 5 && -6 <= s3 && s3 <= 11 && -6 <= s4 && s4 <= 11 &&
      -6 <= s5 && s5 <= 11 && -6 <= s6 && s6 <= 11)
    (-3.7377847506227999E+02) * s0  + (7.0862205220929002E+02) * s1  + (-3.7886044910192999E+02) * s2  + (-4.5413076942315001E+02) * s3  +
     (-8.7386976105502004E+02) * (1.0) * s4  + (3.5911290954487999E+03) * (1.0) * s5  + (-2.2921226288533999E+03) * (1.0) * s6  + 2.9021426298877441E+02
  }

  def outTraincar4(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12)
      (-1201.0) * s0  + (-4876.0) * s1  + 1.3415999999999998E+04 * s2  + (-10484.0) * s3  + (-774.0) * s4  + (-1.3620000000000002E+04) * s5  +
       10481.0 * s6  + 20425.0 * s7  + (-17815.0) * s8  + 5.2121094496644555E+03
  }
}
