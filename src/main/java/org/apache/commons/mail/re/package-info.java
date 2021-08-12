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
package org.apache.commons.mail.re;

/** This package provides helper classes, that are designed to replace
 * regular expressions, like
 * <pre>
 *   (&lt;[Ii][Mm][Gg]\\s*[^&gt;]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])
 * </pre>
 * The reason for using the helper classes, and not a simple regular
 * expression is a performance problem of the latter, that we wish to overcome.
 * The main idea of the helper class is to divide the regular expression into a
 * cascade of so-called matchers. A matcher replaces a comparatively simple
 * subexpression, like <pre>\\s*</pre>. The simplicity of the replaced expression
 * allows a manual, performace optimized implementation of the corresponding
 * matcher.
 */
