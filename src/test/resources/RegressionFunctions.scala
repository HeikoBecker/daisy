import daisy.lang._
import Real._

object RegressionFunctions {

  def doppler(u: Real, v: Real, T: Real): Real = {
    require(-100.0 <= u && u <= 100 && 20 <= v && v <= 20000 && -30 <= T && T <= 50)

    val t1 = 331.4 + 0.6 * T
    (- (t1) *v) / ((t1 + u)*(t1 + u))

  }

  def dopplerInlined(u: Real, v: Real, T: Real): Real = {
    require(-100.0 <= u && u <= 100 && 20 <= v && v <= 20000 && -30 <= T && T <= 50)

    (- (331.4 + 0.6 * T) *v) / (((331.4 + 0.6 * T) + u)*((331.4 + 0.6 * T) + u))

  }

  def sine(x: Real): Real = {
    require(x > -1.57079632679 && x < 1.57079632679)
    x - (x*x*x)/6.0 + (x*x*x*x*x)/120.0 - (x*x*x*x*x*x*x)/5040.0
  }

  def sineOrder3(x: Real): Real = {
    require(-2.0 < x && x < 2.0)
    0.954929658551372 * x -  0.12900613773279798*(x*x*x)
  }


  def sqroot(x: Real): Real = {
    require(x >= 0.0 && x < 10.0)
    1.0 + 0.5*x - 0.125*x*x + 0.0625*x*x*x - 0.0390625*x*x*x*x
  }


  def bspline0(u: Real): Real = {
    require(0 <= u && u <= 1)
    (1 - u) * (1 - u) * (1 - u) / 6.0
  }

  def bspline1(u: Real): Real = {
    require(0 <= u && u <= 1)
    (3 * u*u*u - 6 * u*u + 4) / 6.0
  }

  def bspline2(u: Real): Real = {
    require(0 <= u && u <= 1)
    (-3 * u*u*u  + 3*u*u + 3*u + 1) / 6.0
  }

  def bspline3(u: Real): Real = {
    require(0 <= u && u <= 1)
    -u*u*u / 6.0
  }

  def rigidBody1(x1: Real, x2: Real, x3: Real): Real = {
    require(-15.0 <= x1 && x1 <= 15 && -15.0 <= x2 && x2 <= 15.0 && -15.0 <= x3 && x3 <= 15)

    -x1*x2 - 2*x2*x3 - x1 - x3
  }

  def rigidBody2(x1: Real, x2: Real, x3: Real): Real = {
    require(-15.0 <= x1 && x1 <= 15 && -15.0 <= x2 && x2 <= 15.0 &&
      -15.0 <= x3 && x3 <= 15)

    2*(x1*x2*x3) + (3*x3*x3) - x2*(x1*x2*x3) + (3*x3*x3) - x2
  }


  def verhulst(r: Real, K: Real, x: Real): Real = {
    require(r >= 4.0 && r <= 4.0 && K >= 1.11 && K <= 1.11 && 0.1 <= x && x <= 0.3)

    (r*x) / (1 + (x/K))

  }

  def predatorPrey(r: Real, K: Real, x: Real): Real = {
    require(r >= 4.0 && r <= 4.0 && K >= 1.11 && K <= 1.11 && 0.1 <= x && x <= 0.3)

    (r*x*x) / (1 + (x/K)*(x/K))

  }

  def carbonGas(T: Real, a: Real, b: Real, N: Real, p: Real, V: Real): Real = {
    require(T >= 300 && T <= 300 && a >= 0.401 && a <= 0.401 && b >= 42.7e-6 && b <= 42.7e-6 && N >= 1000 && N <= 1000 &&
    p >= 3.5e7 && p <= 3.5e7 && 0.1 < V && V < 0.5)

    val k:Real = 1.3806503e-23
    (p + a * (N / V) * (N / V)) * (V - N * b) - k * N * T

  }

  def turbine1(v: Real, w: Real, r: Real): Real = {
    require(-4.5 <= v && v <= -0.3 && 0.4 <= w && w <= 0.9 && 3.8 <= r && r <= 7.8)

    3 + 2/(r*r) - 0.125*(3-2*v)*(w*w*r*r)/(1-v) - 4.5

  }

  def turbine2(v: Real, w: Real, r: Real): Real = {
    require(-4.5 <= v && v <= -0.3 && 0.4 <= w && w <= 0.9 && 3.8 <= r && r <= 7.8)

    6*v - 0.5 * v * (w*w*r*r) / (1-v) - 2.5

  }

  def turbine3(v: Real, w: Real, r: Real): Real = {
    require(-4.5 <= v && v <= -0.3 && 0.4 <= w && w <= 0.9 && 3.8 <= r && r <= 7.8)

    3 - 2/(r*r) - 0.125 * (1+2*v) * (w*w*r*r) / (1-v) - 0.5

  }

  def kepler0(x1: Real, x2: Real, x3: Real, x4: Real, x5: Real, x6: Real): Real = {
    require(4 <= x1 && x1 <= 6.36 && 4 <= x2 && x2 <= 6.36 && 4 <= x3 && x3 <= 6.36 &&
      4 <= x4 && x4 <= 6.36 && 4 <= x5 && x5 <= 6.36 && 4 <= x6 && x6 <= 6.36)

    x2 * x5 + x3 * x6 - x2 * x3 - x5 * x6 + x1 * (-x1 + x2 + x3 - x4 + x5 + x6)

  } // 1.15e-15


  def kepler1(x1: Real, x2: Real, x3: Real, x4: Real): Real = {
    require(4 <= x1 && x1 <= 6.36 && 4 <= x2 && x2 <= 6.36 && 4 <= x3 && x3 <= 6.36 &&
      4 <= x4 && x4 <= 6.36)

    x1 * x4 * (-x1 + x2 + x3 - x4) + x2 * (x1 - x2 + x3 + x4) + x3 * (x1 + x2 - x3 + x4) -
      x2 * x3 * x4 - x1 * x3 - x1 * x2 - x4

  } // 4.50e–13

  def kepler2(x1: Real, x2: Real, x3: Real, x4: Real, x5: Real, x6: Real): Real = {
    require(4 <= x1 && x1 <= 6.36 && 4 <= x2 && x2 <= 6.36 && 4 <= x3 && x3 <= 6.36 &&
      4 <= x4 && x4 <= 6.36 && 4 <= x5 && x5 <= 6.36 && 4 <= x6 && x6 <= 6.36)

      x1 * x4 * (-x1 + x2 + x3 - x4 + x5 + x6) + x2 * x5 * (x1 - x2 + x3 + x4 - x5 + x6) +
        x3* x6 * (x1 + x2 - x3 + x4 + x5 - x6) - x2 * x3 * x4 -
          x1* x3* x5 - x1 * x2 * x6 - x4 * x5 * x6

  } //2.08e–12

  def himmilbeau(x1: Real, x2: Real) = {
    require(-5 <= x1 && x1 <= 5 && -5 <= x2 && x2 <= 5)

    (x1*x1 + x2 - 11)*(x1 * x1 + x2 - 11) + (x1 + x2*x2 - 7)*(x1 + x2*x2 - 7)

  } //1.43e–12

  def pendulum1(t: Real, w: Real): Real = {
    require(-2 <= t && t <= 2 && -5 <= w && w <= 5)

    t + 0.01*(w + 0.01/2*(-9.80665/2.0 * sin(t)))

  }


  def pendulum2(t: Real, w: Real): Real = {
    require(-2 <= t && t <= 2 && -5 <= w && w <= 5)

    w + 0.01*(-9.80665/2.0 * sin(t + 0.01/2*w))

  }

  def invertedPendulum(s1: Real, s2: Real, s3: Real, s4: Real) = {
    require(-50 <= s1 && s1 <= 50 && -10 <= s2 && s2 <= 10 && -0.785 <= s3 && s3 <= 0.785 && -0.785 <= s4 && s4 <= 0.785)

    1.0000 * s1 + 1.6567 * s2 + (-18.6854) * s3 + (-3.4594) * s4
  }

  def train4_out1(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12)
      (-1201.0) * s0  + (-4876.0) * s1  + 1.3415999999999998E+04 * s2  + (-10484.0) * s3  + (-774.0) * s4  + (-1.3620000000000002E+04) * s5  +
       10481.0 * s6  + 20425.0 * s7  + (-17815.0) * s8  + 5.2121094496644555E+03
  }

  def train4_state1(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    s0 + (-9.6239E-08)*s1+ (2.65102E-07)*s2+ (-2.07133E-07)*s3+ (6.97639E-05)*s4+ (-4.78152E-05)*s5+ (-2.63725E-05)*s6+ (-1.05202E-05)*s7+ (-1.57626E-05)*s8 +
      (3.0220751E-05)*y0 + (-5.2453841E-05)*y1+ (2.6579541E-05)*y2+ (1.0923688E-05)*y3+ (1.5410654E-05)*y4+ (1.9755163E-11)*5.2121094496644555E+03
  }

  def train4_state2(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-1.13598E-08)*s0 + (1.0)*s1+ (1.2786E-07)*s2+ (-9.96874E-08)*s3+ (3.36288E-05)*s4+ (3.94595E-05)*s5+ (-6.09589E-05)*s6+
    (-3.51073E-05)*s7+ (2.7313E-06)*s8+ (-3.3636111E-05)*y0 + (6.0410747E-05)*y1+ (-3.8941208E-05)*y2+ (3.5301806E-05)*y3+
    (-2.9009623E-06)*y4+ (9.522692E-12)*5.2121094496644555E+03
  }

  def train4_state3(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-3.85494E-09)*s0 + (-1.55578E-08)*s1+ (1.0)*s2+ (-3.33852E-08)*s3+ (1.13515E-05)*s4+ (8.09597E-06)*s5+ (3.75166E-05)*s6+
    (-7.28888E-05)*s7+ (7.51347E-06)*s8 + (-1.1353997E-05)*y0 + (-8.1397399E-06)*y1+ (6.2517037E-05)*y2+
    (-2.7045432E-05)*y3+ (-7.570773E-06)*y4+ (3.2144001E-12)*5.2121094496644555E+03
  }

  def train4_state4(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-4.88831E-09)*s0 + (-1.94328E-08)*s1+ (5.40591E-08)*s2+ (1.0)*s3+ (1.41246E-05)*s4+ (-1.33425E-05)*s5+ (4.35487E-06)*s6+
    (1.06102E-04)*s7+ (-9.66574E-05)*s8+ (-1.4127737E-05)*y0 + (1.328804E-05)*y1+ (-4.3128989E-06)*y2+
    (-6.0208156E-06)*y3+ (-3.4137075E-06)*y4+ (3.9997189E-12)*5.2121094496644555E+03
  }

  def train4_state5(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-6.84591E-04)*s0 + (-0.00276041)*s1+ (0.00759572)*s2+ (-0.00593566)*s3+ (0.999506)*s4+ (-0.00770228)*s5+ (0.00593952)*s6+
    (0.0115632)*s7+ (-0.0100812)*s8+ (5.4816343E-05)*y0 + (-8.1959323E-06)*y1+(-5.0852166E-06)*y2+ (-1.5117184E-07)*y3+
    (-4.5614134E-06)*y4+ (5.6615597E-07)*5.2121094496644555E+03

  }

  def train4_state6(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (7.81569E-06)*s0 +(-7.83331E-06)*s1+ (3.894E-08)*s2+ (-3.04854E-08)*s3+ (1.02522E-05)*s4+ (0.999937)*s5+ (9.27415E-06)*s6+
     (6.88119E-06)*s7+ (-5.63471E-06)*s8 + (-9.161729E-06)*y0 + (6.1088274E-05)*y1+ (-8.1509892E-06)*y2+ (-6.8219061E-06)*y3+ (5.5829936E-06)*y4+
      (2.9031464E-12)*5.2121094496644555E+03

  }

  def train4_state7(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-2.3463E-09)*s0 +(7.80955E-06)*s1+ (-7.79273E-06)*s2+ (-2.06835E-08)*s3+ (6.95227E-06)*s4+ (8.71732E-06)*s5+ (0.999932)*s6+
     (1.01306E-05)*s7+ (-1.09525E-06)*s8+ (-6.953786E-06)*y0 + (-7.6514151E-06)*y1+ (6.5402276E-05)*y2+ (-8.997669E-06)*y3+ (1.0601845E-06)*y4+
      (1.9686912E-12)*5.2121094496644555E+03

  }

  def train4_state8(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  }

  def train4_state9(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (-1.73572E-09)*s0 +(-6.90441E-09)*s1+ (1.91831E-08)*s2+ (7.80416E-06)*s3+ (5.01527E-06)*s4+ (-4.73947E-06)*s5+ (4.30545E-07)*s6+
     (3.35281E-05)*s7+ (0.999934)*s8 + (-5.0163739E-06)*y0 + (4.7201386E-06)*y1+ (-4.156438E-07)*y2+ (-3.2406398E-05)*y3+ (6.4987306E-05)*y4+
      (1.4201936E-12)*5.2121094496644555E+03
  }

  def jetEngineInlined(x1: Real, x2: Real): Real = {
    require(-5 <= x1 && x1 <= 5 && -20 <= x2 && x2 <= 5)

    x1 + ((2*x1*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1))*
    ((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1) - 3) + x1*x1*(4*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1))-6))*
    (x1*x1 + 1) + 3*x1*x1*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1)) + x1*x1*x1 + x1 +
    3*((3*x1*x1 + 2*x2 -x1)/(x1*x1 + 1)))
  }

  def jetEngine(x1: Real, x2: Real): Real = {
    require(-5 <= x1 && x1 <= 5 && -20 <= x2 && x2 <= 5)

    val t = (3*x1*x1 + 2*x2 - x1)

    x1 + ((2*x1*(t/(x1*x1 + 1))*
    (t/(x1*x1 + 1) - 3) + x1*x1*(4*(t/(x1*x1 + 1))-6))*
    (x1*x1 + 1) + 3*x1*x1*(t/(x1*x1 + 1)) + x1*x1*x1 + x1 +
    3*((3*x1*x1 + 2*x2 -x1)/(x1*x1 + 1)))
  }

  def analysis1(x: Real): Real = {
    require(x > -1.57079632679 && x < 1.57079632679)
    sin(x) * sin(x) * cos(x) * cos(x)
  }

  def analysis2(x: Real): Real = {
    require(-1.1 <= x && x < 0.9)

    ((1 / cos(x)) * (1 / cos(x))) / (4 + (tan(x)) * (tan(x)))
  }

  // from FPTaylor
  def logExp(x: Real): Real = {
    require(-8 <= x && x <= 8)

    log(1 + exp(x))
  }

  // from FPTaylor
  def sphere(r: Real, x: Real, lat: Real, lon: Real): Real = {
    require(0 <= r && r <= 10 && -10 <= x && x <= 10 &&
      -1.570796 <= lat && lat <= 1.570796 && -3.14159265 <= lon && lon <= 3.14159265)

    x + r * sin(lat) * cos(lon)
  }

  /*def jetEngine(x1: Real, x2: Real) = {
    require(-5 <= x1 && x1 <= 5 && -20 <= x2 && x2 <= 5)
    x1 + (
      (2*x1*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1))*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1) - 3) +
     x1*x1*(4*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1))-6))
      *(x1*x1 + 1) +
    3*x1*x1*((3*x1*x1 + 2*x2 - x1)/(x1*x1 + 1)) + x1*x1*x1 + x1 + 3*((3*x1*x1 + 2*x2 -x1)/(x1*x1 + 1)))

  }*/
  
  // For Powers unrolling and rolling

  def evenPowers(x: Real): Real = {
    require(-14.594 <= x && x <= 23.8539)
    x * x * x * x * x * x
  }

  def inverseSquare(x: Real): Real = {
    require(1e-20 <= x && x <= 1e-10)
    1 / x * x
  }

  def euclideanDistance(x1: Real, x2: Real, y1: Real, y2: Real): Real = {
    require(-5.23 <= x1 && x1 <= -4.67 &&
      5.293 <= x2 && x2 <= 7.59 &&
      -0.91294 <= y1 && y1 <= 0.9999 &&
      -14.594 <= y2 && y2 <= 23.8539)
    sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
  }

}