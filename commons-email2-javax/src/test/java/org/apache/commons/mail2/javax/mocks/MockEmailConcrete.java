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
package org.apache.commons.mail2.javax.mocks;

import org.apache.commons.mail2.javax.Email;

/**
 * Concrete Implementation on the Abstract Email Class (used to allow testing only). Supplies getters for methods that normally only have setters.
 *
 * @since 1.0
 */
public class MockEmailConcrete extends Email {

    /**
     * No-op for testing.
     *
     * @param msg The email message.
     * @return Email msg.
     */
    @Override
    public Email setMsg(final String msg) {
        // This abstract method should be tested in the concrete
        // implementation classes only.
        return this;
    }

}
