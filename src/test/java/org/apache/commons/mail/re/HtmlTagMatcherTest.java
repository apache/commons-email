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

import org.junit.Test;

public class HtmlTagMatcherTest {
	@Test
	public void testMatch() {
		final HtmlTagMatcher htm = new HtmlTagMatcher(new String[] {"img", "src"});
		MatcherTests.assertMatch(htm, "<img", 0, 0, 4);
		MatcherTests.assertMatch(htm, "<img>", 0, 0, 4);
		MatcherTests.assertMatch(htm, "<iMg", 0, 0, 4);
		MatcherTests.assertMatch(htm, "<imG>", 0, 0, 4);
		MatcherTests.assertMatch(htm, " <img", 0, 1, 5);
		MatcherTests.assertMatch(htm, " <img>", 0, 1, 5);
		MatcherTests.assertMatch(htm, " <img", 1, 1, 5);
		MatcherTests.assertMatch(htm, " <img>", 1, 1, 5);
		MatcherTests.assertMatch(htm, " <img>", 0, 1, 5);
		MatcherTests.assertMatch(htm, " <img> <src", 0, 1, 5, 7, 11);
	}
}
