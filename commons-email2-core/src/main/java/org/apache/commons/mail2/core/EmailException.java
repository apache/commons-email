/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail2.core;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Exception thrown when a checked error occurs in commons-email.
 * <p>
 * Adapted from FunctorException in Commons Collections.
 * </p>
 * <p>
 * Emulation support for nested exceptions has been removed in {@code Email 1.3}, supported by JDK 1.4 and above.
 * </p>
 *
 * @since 1.0
 */
public class EmailException extends Exception {
    /** Serializable version identifier. */
    private static final long serialVersionUID = 5550674499282474616L;

    /**
     * Throws an EmailException if the supplier evaluates to true.
     *
     * @param <T>     the subject type to return if we don't throw.
     * @param test    test condition.
     * @param subject the subject to return if we don't throw.
     * @param message the exception message.
     * @return the given subject.
     * @throws EmailException if the supplier evaluates to true.
     */
    public static <T> T check(final Supplier<Boolean> test, final T subject, final Supplier<String> message) throws EmailException {
        if (test.get()) {
            throw new EmailException(message.get());
        }
        return subject;
    }

    /**
     * Throws an EmailException if the collection is empty.
     *
     * @param <T>     the type of elements in the collection.
     * @param value   the value to test.
     * @param message the exception message.
     * @return the given subject.
     * @throws EmailException if the collection is empty.
     */
    public static <T> Collection<T> checkNonEmpty(final Collection<T> value, final Supplier<String> message) throws EmailException {
        return check(() -> EmailUtils.isEmpty(value), value, message);
    }

    /**
     * Throws an EmailException if the string is empty.
     *
     * @param message the exception message.
     * @param value   the value to test.
     * @return the given subject.
     * @throws EmailException if the string is empty.
     */
    public static String checkNonEmpty(final String value, final Supplier<String> message) throws EmailException {
        return check(() -> EmailUtils.isEmpty(value), value, message);
    }

    /**
     * Throws an EmailException if the array is empty.
     *
     * @param <T>     the array type.
     * @param message the exception message.
     * @param value   the value to test.
     * @return the given subject.
     * @throws EmailException if the array is empty.
     */
    public static <T> T[] checkNonEmpty(final T[] value, final Supplier<String> message) throws EmailException {
        return check(() -> EmailUtils.isEmpty(value), value, message);
    }

    /**
     * Throws an EmailException if the value is null.
     *
     * @param <T>     the value type.
     * @param message the exception message.
     * @param value   the value to test.
     * @return the given subject.
     * @throws EmailException if the value is null.
     */
    public static <T> T checkNonNull(final T value, final Supplier<String> message) throws EmailException {
        if (value == null) {
            throw new EmailException(message.get());
        }
        return value;
    }

    /**
     * Constructs a new {@code EmailException} with no detail message.
     */
    public EmailException() {
    }

    /**
     * Constructs a new {@code EmailException} with specified detail message.
     *
     * @param msg the error message.
     */
    public EmailException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code EmailException} with specified detail message and nested {@code Throwable} root cause.
     *
     * @param msg       the error message.
     * @param rootCause the exception or error that caused this exception to be thrown.
     */
    public EmailException(final String msg, final Throwable rootCause) {
        super(msg, rootCause);
    }

    /**
     * Constructs a new {@code EmailException} with specified nested {@code Throwable} root cause.
     *
     * @param rootCause the exception or error that caused this exception to be thrown.
     */
    public EmailException(final Throwable rootCause) {
        super(rootCause);
    }

}
