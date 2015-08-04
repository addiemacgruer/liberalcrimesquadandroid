package jp.ac.hiroshimau.sci.math;

import java.util.Random;

import lcs.android.game.Game;

import org.eclipse.jdt.annotation.NonNullByDefault;

import android.util.Log;

/** A C-program for MT19937, with initialization improved 2002/1/26. Coded by Takuji Nishimura and
 * Makoto Matsumoto. Before using, initialize the state by using init_genrand(seed) or
 * init_by_array(init_key, key_length). Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji
 * Nishimura, All rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. 3. The names of its contributors may not
 * be used to endorse or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. Any feedback is very welcome.
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html email: m-mat @
 * math.sci.hiroshima-u.ac.jp (remove space) */
public @NonNullByDefault class Twister extends Random {
  /** Initiate a new Mersenne Twister, with the current system time as a seed value */
  public Twister() {
    seed = System.currentTimeMillis();
  }

  /** Initiate a new Mersenne Twuster with a given seed.
   * @param seed */
  public Twister(final long seed) { // NO_UCD (unused code)
    super(seed);
    this.seed = seed;
  }

  private final long[] mt = new long[N]; /* the array for the state vector */

  private int mti = N + 1; /* mti==N+1 means mt[N] is not initialized */

  private long seed = 5489;

  /** generates a random number on [0,1) with 53-bit resolution */
  @Override public double nextDouble() {
    final long a = nextInt() >> 5, b = nextInt() >> 6;
    return (a * 67108864.0 + b) * (1.0 / 9007199254740992.0);
  }

  /** generates a random number on [0,0xffffffff]-interval */
  @Override public int nextInt() {
    long y;
    final long[] mag01 = { 0x0, MATRIX_A };
    /* mag01[x] = x * MATRIX_A for x=0,1 */
    if (mti >= N) { /* generate N words at one time */
      int kk;
      if (mti == N + 1) {
        initGenrand(seed); /* a default initial seed is used */
      }
      for (kk = 0; kk < N - M; kk++) {
        y = mt[kk] & UPPER_MASK | mt[kk + 1] & LOWER_MASK;
        mt[kk] = mt[kk + M] ^ y / 2 ^ mag01[(int) (y & 0x1)];
      }
      for (; kk < N - 1; kk++) {
        y = mt[kk] & UPPER_MASK | mt[kk + 1] & LOWER_MASK;
        mt[kk] = mt[kk + M - N] ^ y / 2 ^ mag01[(int) (y & 0x1)];
      }
      y = mt[N - 1] & UPPER_MASK | mt[0] & LOWER_MASK;
      mt[N - 1] = mt[M - 1] ^ y / 2 ^ mag01[(int) (y & 0x1)];
      mti = 0;
    }
    y = mt[mti++];
    /* Tempering */
    y ^= y / 21;
    y ^= y << 7 & 0x9d2c5680;
    y ^= y << 15 & 0xefc60000;
    y ^= y / 28;
    return (int) y;
  }

  /** resets the seed value */
  @Override public synchronized void setSeed(final long seed) {
    super.setSeed(seed);
    this.seed = seed;
    mti = N + 1;
  }

  /* generates a random number on [0,0x7fffffff]-interval */
  protected int genrandInt31() {
    return nextInt() / 2;
  }

  /* generates a random number on [0,1]-real-interval */
  protected double genrandReal1() {
    return nextInt() * (1.0 / 4294967295.0);
    /* divided by 2^32-1 */
  }

  /* generates a random number on (0,1)-real-interval */
  protected double genrandReal3() {
    return (nextInt() + 0.5) * (1.0 / 4294967296.0);
    /* divided by 2^32 */
  }

  protected int main() { // NO_UCD (unused code)
    int i;
    final long[] init = { 0x123, 0x234, 0x345, 0x456 };
    initByArray(init, init.length);
    System.out.println("1000 outputs of genrand_int32()\n");
    for (i = 0; i < 1000; i++) {
      System.out.println(nextInt());
      if (i % 5 == 4) {
        System.out.println();
      }
    }
    System.out.println("\n1000 outputs of genrand_real2()\n");
    for (i = 0; i < 1000; i++) {
      System.out.printf("%10.8f ", genrandReal2());
      if (i % 5 == 4) {
        System.out.println();
      }
    }
    return 0;
  }

  /* These real versions are due to Isaku Wada, 2002/01/09 added */
  @Override protected synchronized int next(final int bits) {
    if (bits == 32)
      return nextInt();
    return nextInt() & (1 << bits) - 1;
  }

  /* generates a random number on [0,1)-real-interval */
  private double genrandReal2() {
    return nextInt() * (1.0 / 4294967296.0);
    /* divided by 2^32 */
  }

  /* initialize by an array with array-length */
  /* init_key is the array for initializing keys */
  /* key_length is its length */
  /* slight change for C++, 2004/2/26 */
  private void initByArray(final long[] init_key, final int key_length) {
    int i, j, k;
    initGenrand(seed);
    i = 1;
    j = 0;
    k = N > key_length ? N : key_length;
    for (; k != 0; k--) {
      mt[i] = (mt[i] ^ (mt[i - 1] ^ mt[i - 1] >> 30) * 1664525) + init_key[j] + j; /* non linear */
      mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
      i++;
      j++;
      if (i >= N) {
        mt[0] = mt[N - 1];
        i = 1;
      }
      if (j >= key_length) {
        j = 0;
      }
    }
    for (k = N - 1; k != 0; k--) {
      mt[i] = (mt[i] ^ (mt[i - 1] ^ mt[i - 1] >> 30) * 1566083941) - i; /* non linear */
      mt[i] &= 0xffffffff; /* for WORDSIZE > 32 machines */
      i++;
      if (i >= N) {
        mt[0] = mt[N - 1];
        i = 1;
      }
    }
    mt[0] = 0x80000000; /* MSB is 1; assuring non-zero initial array */
  }

  /* initializes mt[N] with a seed */
  private void initGenrand(final long s) {
    Log.w(Game.LCS, "Twist!init_genrand:" + s);
    mt[0] = s & 0xffffffff;
    for (mti = 1; mti < N; mti++) {
      mt[mti] = 1812433253 * (mt[mti - 1] ^ mt[mti - 1] >> 30) + mti;
      /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
      /* In the previous versions, MSBs of the seed affect */
      /* only MSBs of the array mt[]. */
      /* 2002/01/09 modified by Makoto Matsumoto */
      mt[mti] &= 0xffffffff;
      /* for >32 bit machines */
    }
  }

  private final static int LOWER_MASK = 0x7fffffff; /* least significant r bits */

  private final static int M = 397;

  private final static int MATRIX_A = 0x9908b0df; /* constant vector a */

  /* Period parameters */
  private final static int N = 624;

  private static final long serialVersionUID = Game.VERSION;

  private final static int UPPER_MASK = 0x80000000; /* most significant w-r bits */
}
