package com.tuplejump.stargate;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface Serializer<T> {

    void serialize(DataOutput appender, T value) throws IOException;

    T deserialize(DataInput in, int available) throws IOException;

    int fixedSize();
}
