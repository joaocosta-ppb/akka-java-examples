package io.github.jlmc;

/**
 * # Exercise 1
 * 1. Source - emit 10 elements.
 * 2. Convert element to a random BigInteger `new BigInteger(2000, new Random())`.
 * 3. Find the next probable prime + print it
 * 4. Group the primes into a List
 *
 * <pre>
 * --------            -----------------------              -------------------------                ----------------              --------------
 * \ Source \ => () => | Flow (to BigInteger) | => (BI) => | Flow (to prime + print) | => (prime) => \ Flow to List | => (list) => \ Flow (sort) | => (list) => Sink
 * ----------          -----------------------             ---------------------------               ----------------               --------------
 * </pre>
 *
 */
public class Exercise1BigPrimes {
}
