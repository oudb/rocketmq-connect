/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.connect.runtime.serialization;


import java.nio.ByteBuffer;

/**
 * Factory for creating serializers / deserializers.
 */
public class Serdes {

    static public final class LongSerde extends WrapperSerde<Long> {
        public LongSerde() {
            super(new LongSerializer(), new LongDeserializer());
        }
    }

    static public final class IntegerSerde extends WrapperSerde<Integer> {
        public IntegerSerde() {
            super(new IntegerSerializer(), new IntegerDeserializer());
        }
    }

    static public final class ShortSerde extends WrapperSerde<Short> {
        public ShortSerde() {
            super(new ShortSerializer(), new ShortDeserializer());
        }
    }

    static public final class FloatSerde extends WrapperSerde<Float> {
        public FloatSerde() {
            super(new FloatSerializer(), new FloatDeserializer());
        }
    }

    static public final class DoubleSerde extends WrapperSerde<Double> {
        public DoubleSerde() {
            super(new DoubleSerializer(), new DoubleDeserializer());
        }
    }

    static public final class StringSerde extends WrapperSerde<String> {
        public StringSerde() {
            super(new StringSerializer(), new StringDeserializer());
        }
    }

    static public final class ByteBufferSerde extends WrapperSerde<ByteBuffer> {
        public ByteBufferSerde() {
            super(new ByteBufferSerializer(), new ByteBufferDeserializer());
        }
    }


    static public final class ByteArraySerde extends WrapperSerde<byte[]> {
        public ByteArraySerde() {
            super(new ByteArraySerializer(), new ByteArrayDeserializer());
        }
    }


    /**
     * serde from by type
     *
     * @param type
     * @param <T>
     * @return
     */
    static public <T> Serde<T> serdeFrom(Class<T> type) {
        if (String.class.isAssignableFrom(type)) {
            return (Serde<T>) String();
        }

        if (Short.class.isAssignableFrom(type)) {
            return (Serde<T>) Short();
        }

        if (Integer.class.isAssignableFrom(type)) {
            return (Serde<T>) Integer();
        }

        if (Long.class.isAssignableFrom(type)) {
            return (Serde<T>) Long();
        }

        if (Float.class.isAssignableFrom(type)) {
            return (Serde<T>) Float();
        }

        if (Double.class.isAssignableFrom(type)) {
            return (Serde<T>) Double();
        }

        if (byte[].class.isAssignableFrom(type)) {
            return (Serde<T>) ByteArray();
        }

        if (ByteBuffer.class.isAssignableFrom(type)) {
            return (Serde<T>) ByteBuffer();
        }

        // TODO: we can also serializes objects of type T using generic Java serialization by default
        throw new IllegalArgumentException("Unknown class for built-in serializer. Supported types are: " +
                "String, Short, Integer, Long, Float, Double, ByteArray, ByteBuffer, Bytes, UUID");
    }

    /**
     * Construct a serde object from separate serializer and deserializer
     *
     * @param serializer   must not be null.
     * @param deserializer must not be null.
     */
    static public <T> Serde<T> serdeFrom(final Serializer<T> serializer, final Deserializer<T> deserializer) {
        if (serializer == null) {
            throw new IllegalArgumentException("serializer must not be null");
        }
        if (deserializer == null) {
            throw new IllegalArgumentException("deserializer must not be null");
        }

        return new WrapperSerde<>(serializer, deserializer);
    }

    /**
     * A serde for nullable {@code Long} type.
     */
    static public Serde<Long> Long() {
        return new LongSerde();
    }

    /**
     * A serde for nullable {@code Integer} type.
     */
    static public Serde<Integer> Integer() {
        return new IntegerSerde();
    }

    /**
     * A serde for nullable {@code Short} type.
     */
    static public Serde<Short> Short() {
        return new ShortSerde();
    }

    /**
     * A serde for nullable {@code Float} type.
     */
    static public Serde<Float> Float() {
        return new FloatSerde();
    }

    /**
     * A serde for nullable {@code Double} type.
     */
    static public Serde<Double> Double() {
        return new DoubleSerde();
    }

    /**
     * A serde for nullable {@code String} type.
     */
    static public Serde<String> String() {
        return new StringSerde();
    }

    /**
     * A serde for nullable {@code ByteBuffer} type.
     */
    static public Serde<ByteBuffer> ByteBuffer() {
        return new ByteBufferSerde();
    }


    /**
     * A serde for nullable {@code byte[]} type.
     */
    static public Serde<byte[]> ByteArray() {
        return new ByteArraySerde();
    }

}
