/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.mail2.core;

public class EmailTestConstants {
    // @formatter:off
    public static final String[] INVALID_EMAILS = {
            "local name@domain.com",
            "local(name@domain.com",
            "local)name@domain.com",
            "local<name@domain.com",
            "local>name@domain.com",
            "local,name@domain.com",
            "local;name@domain.com",
            "local:name@domain.com",
            "local[name@domain.com",
            "local]name@domain.com",
            // "local\\name@domain.com", is considered valid for mail-1.4.1
            "local\"name@domain.com",
            "local\tname@domain.com",
            "local\nname@domain.com",
            "local\rname@domain.com",
            "local.name@domain com",
            "local.name@domain(com",
            "local.name@domain)com",
            "local.name@domain<com",
            "local.name@domain>com",
            "local.name@domain,com",
            "local.name@domain;com",
            "local.name@domain:com",
            "local.name@domain..com",
            // "local.name@domain[com",
            "local.name@domain]com",
            "local.name@domain\\com",
            "local.name@domain\tcom",
            "local.name@domain\ncom",
            "local.name@domain\rcom",
            "local.name@",
            "@domain.com" };
    // @formatter:on
}
