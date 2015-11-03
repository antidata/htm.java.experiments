package org.numenta.nupic.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <h3>MersenneTwister and MersenneTwisterFast</h3>
 * <p><b>Version 20</b>, based on version MT199937(99/10/29)
 * of the Mersenne Twister algorithm found at 
 * <a href="http://www.math.keio.ac.jp/matumoto/emt.html">
 * The Mersenne Twister Home Page</a>, with the initialization
 * improved using the new 2002/1/26 initialization algorithm
 * By Sean Luke, October 2004.
 *
 * <p><b>MersenneTwister</b> is a drop-in subclass replacement
 * for java.util.Random.  It is properly synchronized and
 * can be used in a multithreaded environment.  On modern VMs such
 * as HotSpot, it is approximately 1/3 slower than java.util.Random.
 *
 * <p><b>MersenneTwisterFast</b> is not a subclass of java.util.Random.  It has
 * the same public methods as Random does, however, and it is
 * algorithmically identical to MersenneTwister.  MersenneTwisterFast
 * has hard-code inlined all of its methods directly, and made all of them
 * final (well, the ones of consequence anyway).  Further, these
 * methods are <i>not</i> synchronized, so the same MersenneTwisterFast
 * instance cannot be shared by multiple threads.  But all this helps
 * MersenneTwisterFast achieve well over twice the speed of MersenneTwister.
 * java.util.Random is about 1/3 slower than MersenneTwisterFast.
 *
 * <h3>About the Mersenne Twister</h3>
 * <p>This is a Java version of the C-program for MT19937: Integer version.
 * The MT19937 algorithm was created by Makoto Matsumoto and Takuji Nishimura,
 * who ask: "When you use this, send an email to: matumoto@math.keio.ac.jp
 * with an appropriate reference to your work".  Indicate that this
 * is a translation of their algorithm into Java.
 *
 * <p><b>Reference. </b>
 * Makato Matsumoto and Takuji Nishimura,
 * "Mersenne Twister: A 623-Dimensionally Equidistributed Uniform
 * Pseudo-Random Number Generator",
 * <i>ACM Transactions on Modeling and. Computer Simulation,</i>
 * Vol. 8, No. 1, January 1998, pp 3--30.
 *
 * <h3>About this Version</h3>
 *
 * <p><b>Changes since V19:</b> nextFloat(boolean, boolean) now returns float,
 * not double.
 *
 * <p><b>Changes since V18:</b> Removed old final declarations, which used to
 * potentially speed up the code, but no longer.
 *
 * <p><b>Changes since V17:</b> Removed vestigial references to &amp;= 0xffffffff
 * which stemmed from the original C code.  The C code could not guarantee that
 * ints were 32 bit, hence the masks.  The vestigial references in the Java
 * code were likely optimized out anyway.
 *
 * <p><b>Changes since V16:</b> Added nextDouble(includeZero, includeOne) and
 * nextFloat(includeZero, includeOne) to allow for half-open, fully-closed, and
 * fully-open intervals.
 *
 * <p><b>Changes Since V15:</b> Added serialVersionUID to quiet compiler warnings
 * from Sun's overly verbose compilers as of JDK 1.5.
 *
 * <p><b>Changes Since V14:</b> made strictfp, with StrictMath.log and StrictMath.sqrt
 * in nextGaussian instead of Math.log and Math.sqrt.  This is largely just to be safe,
 * as it presently makes no difference in the speed, correctness, or results of the
 * algorithm.
 *
 * <p><b>Changes Since V13:</b> clone() method CloneNotSupportedException removed.  
 *
 * <p><b>Changes Since V12:</b> clone() method added.  
 *
 * <p><b>Changes Since V11:</b> stateEquals(...) method added.  MersenneTwisterFast
 * is equal to other MersenneTwisterFasts with identical state; likewise
 * MersenneTwister is equal to other MersenneTwister with identical state.
 * This isn't equals(...) because that requires a contract of immutability
 * to compare by value.
 *
 * <p><b>Changes Since V10:</b> A documentation error suggested that
 * setSeed(int[]) required an int[] array 624 long.  In fact, the array
 * can be any non-zero length.  The new version also checks for this fact.
 *
 * <p><b>Changes Since V9:</b> readState(stream) and writeState(stream)
 * provided.
 *
 * <p><b>Changes Since V8:</b> setSeed(int) was only using the first 28 bits
 * of the seed; it should have been 32 bits.  For small-number seeds the
 * behavior is identical.
 *
 * <p><b>Changes Since V7:</b> A documentation error in MersenneTwisterFast
 * (but not MersenneTwister) stated that nextDouble selects uniformly from
 * the full-open interval [0,1].  It does not.  nextDouble's contract is
 * identical across MersenneTwisterFast, MersenneTwister, and java.util.Random,
 * namely, selection in the half-open interval [0,1).  That is, 1.0 should
 * not be returned.  A similar contract exists in nextFloat.
 *
 * <p><b>Changes Since V6:</b> License has changed from LGPL to BSD.
 * New timing information to compare against
 * java.util.Random.  Recent versions of HotSpot have helped Random increase
 * in speed to the point where it is faster than MersenneTwister but slower
 * than MersenneTwisterFast (which should be the case, as it's a less complex
 * algorithm but is synchronized).
 *
 * <p><b>Changes Since V5:</b> New empty constructor made to work the same
 * as java.util.Random -- namely, it seeds based on the current time in
 * milliseconds.
 *
 * <p><b>Changes Since V4:</b> New initialization algorithms.  See
 * (see <a href="http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html">
 * http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html</a>)
 *
 * <p>The MersenneTwister code is based on standard MT19937 C/C++ 
 * code by Takuji Nishimura,
 * with suggestions from Topher Cooper and Marc Rieffel, July 1997.
 * The code was originally translated into Java by Michael Lecuyer,
 * January 1999, and the original code is Copyright (c) 1999 by Michael Lecuyer.
 *
 * <h3>Java notes</h3>
 *
 * <p>This implementation implements the bug fixes made
 * in Java 1.2's version of Random, which means it can be used with
 * earlier versions of Java.  See 
 * <a href="http://www.javasoft.com/products/jdk/1.2/docs/api/java/util/Random.html">
 * the JDK 1.2 java.util.Random documentation</a> for further documentation
 * on the random-number generation contracts made.  Additionally, there's
 * an undocumented bug in the JDK java.util.Random.nextBytes() method,
 * which this code fixes.
 *
 * <p> Just like java.util.Random, this
 * generator accepts a long seed but doesn't use all of it.  java.util.Random
 * uses 48 bits.  The Mersenne Twister instead uses 32 bits (int size).
 * So it's best if your seed does not exceed the int range.
 *
 * <p>MersenneTwister can be used reliably 
 * on JDK version 1.1.5 or above.  Earlier Java versions have serious bugs in
 * java.util.Random; only MersenneTwisterFast (and not MersenneTwister nor
 * java.util.Random) should be used with them.
 *
 * <h3>License</h3>
 *
 * Copyright (c) 2003 by Sean Luke. <br>
 * Portions copyright (c) 1993 by Michael Lecuyer. <br>
 * All rights reserved. <br>
 *
 * <p>Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li> Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * <li> Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * <li> Neither the name of the copyright owners, their employers, nor the 
 * names of its contributors may be used to endorse or promote products 
 * derived from this software without specific prior written permission.
 * </ul>
 * <p>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 @version 20
 */

public strictfp class MersenneTwister extends java.util.Random implements Serializable, Cloneable {
    private static final long serialVersionUID = 6208727693524452904L;
    private long seed;

    /**
     * Creates a new pseudo random number generator. The seed is initialized to
     * the current time, as if by
     * <code>setSeed(System.currentTimeMillis());</code>.
     */
    public MersenneTwister() {
        this(System.nanoTime());
    }

    /**
     * Creates a new pseudo random number generator, starting with the specified
     * seed, using <code>setSeed(seed);</code>.
     *
     * @param seed
     *            the initial seed
     */
    public MersenneTwister(long seed) {
        this.seed = seed;
    }
    public MersenneTwister(int[] seed) {
        this.seed = seed[0];
    }
    /**
     * Returns the current state of the seed, can be used to clone the object
     *
     * @returns the current seed
     */
    public synchronized long getSeed() {
        return seed;
    }

    /**
     * Sets the seed for this pseudo random number generator. As described
     * above, two instances of the same random class, starting with the same
     * seed, produce the same results, if the same methods are called.
     *
     * @param s
     *            the new seed
     */
    public synchronized void setSeed(long seed) {
        this.seed = seed;
        super.setSeed(seed);
    }

    /**
     * Returns an XSRandom object with the same state as the original
     */
    public MersenneTwister clone() {
        return new MersenneTwister(getSeed());
    }

    /**
     * Implementation of George Marsaglia's elegant Xorshift random generator
     * 30% faster and better quality than the built-in java.util.random see also
     * see http://www.javamex.com/tutorials/random_numbers/xorshift.shtml
     */
    protected int next(int nbits) {
        long x = seed;
        x ^= (x << 21);
        x ^= (x >>> 35);
        x ^= (x << 4);
        seed = x;
        x &= ((1L << nbits) - 1);
        return (int) x;
    }
    synchronized public void setSeed(int[] array) {
        if (array.length == 0)
            throw new IllegalArgumentException("Array length must be greater than zero");
        setSeed(array.hashCode());
    }
}