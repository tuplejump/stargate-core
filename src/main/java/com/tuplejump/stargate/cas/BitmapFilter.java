package com.tuplejump.stargate.cas;

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.wrappers.LongSet;
import org.apache.cassandra.utils.IFilter;
import org.apache.cassandra.utils.MurmurHash;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: satya
 */
public class BitmapFilter implements IFilter {

    LongSet positiveLongs = new LongSet(new ConciseSet());
    LongSet negativeLongs = new LongSet(new ConciseSet());

    @Override
    public void add(ByteBuffer key) {
        long hash = getHash(key);
        if (hash > 0)
            positiveLongs.add(hash);
        else
            negativeLongs.add(Math.abs(hash));
    }

    private long getHash(ByteBuffer key) {
        return MurmurHash.hash2_64(key, key.position(), key.remaining(), 0);
    }

    @Override
    public boolean isPresent(ByteBuffer key) {
        long hash = getHash(key);
        if (hash > 0)
            return positiveLongs.contains(hash);
        else
            return negativeLongs.contains(Math.abs(hash));
    }

    @Override
    public void clear() {
        positiveLongs.clear();
        negativeLongs.clear();
    }

    @Override
    public long serializedSize() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        clear();
        positiveLongs = null;
        negativeLongs = null;
    }
}
