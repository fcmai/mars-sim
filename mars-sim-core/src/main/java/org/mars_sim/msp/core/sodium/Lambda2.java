package org.mars_sim.msp.core.sodium;

/**
 * An interface for 2-argument lambda functions.
 */
public interface Lambda2<A,B,C> {
    C apply(A a, B b);
}

