package com.tuplejump.stargate.cas;

import org.apache.cassandra.utils.IFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: satya
 */
public class FilterChain {
    protected static final Logger logger = LoggerFactory.getLogger(SearchSupport.class);
    List<IFilter> andChain;
    List<IFilter> orChain;

    public FilterChain(int size) {
        this.andChain = new ArrayList<>(size);
        this.orChain = new ArrayList<>(size);
    }

    public void add(IFilter filter, boolean and) {
        if (and)
            andChain.add(filter);
        else
            orChain.add(filter);
    }

    public boolean accepts(ByteBuffer key) {
        if (logger.isDebugEnabled())
            logger.debug("Testing orChain -");
        for (IFilter filter : orChain) {
            if (filter.isPresent(key))
                return true;
        }

        logger.debug("Testing andChain -");
        for (IFilter filter : andChain) {
            if (!filter.isPresent(key))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filter Chain - OrChain -" + orChain.toString() + " - AndChain - " + andChain.toString();
    }
}
