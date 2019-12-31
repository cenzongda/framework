package net.unsun.infrastructure.aggr.core;

/**
 * base builder interface
 * @author tokey
 * @param <T>
 */
public interface AggrBuilder<T> {

    /**
     * build
     * @return
     */
    T build();

}