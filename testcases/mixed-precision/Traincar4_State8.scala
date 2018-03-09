

import daisy.lang._
import Real._



object Traincar4_State8 {
  // f32
  def train4_state8_32(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 5.5e-6)

  // 0.5 f32
  def train4_state8_32_05(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 2.75e-6)

  // 0.1 f32
  def train4_state8_32_01(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 5.5e-7)

  // 0.01 f32
  def train4_state8_32_001(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 5.5e-8)

  // f64
  def train4_state8_64(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 1e-14)

  // 0.5 f64
  def train4_state8_64_05(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 5e-15)

  // 0.1 f64
  def train4_state8_64_01(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 1e-15)

  // 0.01 f64
  def train4_state8_64_001(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 1e-16)

  // dbldbl
  def train4_state8_dbldbl(s0: Real, s1: Real, s2: Real, s3: Real, s4: Real, s5: Real, s6: Real, s7: Real, s8: Real, y0: Real, y1: Real, y2: Real, y3: Real, y4: Real) = {
    require(-2.5 <= s0 && s0 <= 6.5 && -2.5 <= s1 && s1 <= 6.5 && -2.5 <= s2 && s2 <= 6.5 && -2.5 <= s3 && s3 <= 6.5 && -2 <= s4 && s4 <= 12 &&
      -2 <= s5 && s5 <= 12 && -2 <= s6 && s6 <= 12 && -2 <= s7 && s7 <= 12 && -2 <= s8 && s8 <= 12 &&
      -2 <= y0 && y0 <= 12 && -2 <= y1 && y1 <= 12 && -2 <= y2 && y2 <= 12 && -2 <= y3 && y3 <= 12 && -2 <= y4 && y4 <= 12)
    (2.5093E-10)*s0 +(9.15884E-10)*s1+ (7.81656E-06)*s2+ (-7.81701E-06)*s3+ (-6.54335E-07)*s4+ (6.87341E-06)*s5+ (1.00368E-05)*s6+ (0.999907)*s7+
     (3.32876E-05)*s8+ (6.5448232E-07)*y0 + (-6.8708837E-06)*y1+ (-8.9460042E-06)*y2+ (9.0317123E-05)*y3+ (-3.2191562E-05)*y4+
      (-1.8530512E-13)*5.2121094496644555E+03
  } ensuring(res => res +/- 3.5e-30)
}