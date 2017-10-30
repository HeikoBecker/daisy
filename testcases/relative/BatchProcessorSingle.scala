import daisy.lang._
import Real._


object BatchProcessorSingle {

  // s1, s2, s3, s4, y1, y2: <1, 16, 11>
//  def BatchProcessorSingle_out1(s1: Real) = { // , s2: Real, s3: Real, s4: Real
//    require(-10 <= s1 && s1 <= 10) // && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10)
//    (-0.0429) * s1 + (-0.9170) * -1.65 + (-0.3299) * 6.13 + (-0.8206) * -4.175
//  }
//
//  def BatchProcessorSingle_out2(s1: Real) = { // , s2: Real, s3: Real, s4: Real
//    require(-10 <= s1 && s1 <= 10) // && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10)
//    2.4908 * s1 + 0.0751 * -1.65 + 1.7481 * 6.13 + (-1.1433) * -4.175
//  }

  def BatchProcessorSingle_state1(y1: Real) = { // s1: Real, s2: Real, s3: Real, s4: Real,
    require(//-10 <= s1 && s1 <= 10 && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10 &&
      -10 <= y1 && y1 <= 10) // -9.99627735921253
    0.9670 * 0.378 + (-0.0019) * -1.65 + 0.0187 * 6.13 + (-0.0088) * -4.175 + 0.0447 * y1
//    0.520032 + (0.0447 * y1)
  }

  def BatchProcessorSingle_state1_reverse(y1: Real) = { // s1: Real, s2: Real, s3: Real, s4: Real,
    require(//-10 <= s1 && s1 <= 10 && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10 &&
      -10 <= y1 && y1 <= 10) // -9.99627735921253
    0.0447 * y1 + 0.9670 * 0.378 + (-0.0019) * -1.65 + 0.0187 * 6.13 + (-0.0088) * -4.175
//    (((((0.0447 * y1) + 0.365526) + 0.0031349999999999998) + 0.11463100000000001) + 0.03674)
  }

//  def BatchProcessorSingle_state2(y1: Real) = { // s1: Real, s2: Real, s3: Real, s4: Real, y2: Real
//    require(//-10 <= s1 && s1 <= 10 && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10 &&
//      -10 <= y1 && y1 <= 10) // && -10 <= y2 && y2 <= 10)
//    (-0.0078) * 0.378 + 0.9052 * -1.65 + (-0.0181) * 6.13 + (-0.0392) * -4.175 + (-0.0003) * y1 + 0.0020 * 8.0
//    //((0.9052 * s2) + (((s3 * -0.0181) + (-0.0078 * s1)) + (((-0.0392 * s4) + (-0.0003 * y1)) + (0.002 * y2))))
//  }
//
//  def BatchProcessorSingle_state3(y1: Real) = { // s1: Real, s2: Real, s3: Real, s4: Real, y1: Real, y2: Real
//    require(//-10 <= s1 && s1 <= 10 && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10 &&
//      -10 <= y1 && y1 <= 10) // && -10 <= y2 && y2 <= 10)
//    (-0.0830) * 0.378 + 0.0222 * -1.65 + 0.8620 * 6.13 + 0.0978 * -4.175 + 0.0170 * y1 + 0.0058 * 8.0
//  }
//
//  def BatchProcessorSingle_state4(y1: Real) = { // s1: Real, s2: Real, s3: Real, s4: Real, y1: Real, y2: Real
//    require(//-10 <= s1 && s1 <= 10 && -10 <= s2 && s2 <= 10 && -10 <= s3 && s3 <= 10 && -10 <= s4 && s4 <= 10 &&
//      -10 <= y1 && y1 <= 10) // && -10 <= y2 && y2 <= 10)
//    (-0.0133) * 0.378 + 0.0243 * -1.65 + (-0.0043) * 6.13 + 0.9824 * -4.175 + 0.0127 * y1 + 0.0059 * 8.0
//  }
}