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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

/** Helper class for matcher tests.
 */
public class MatcherTests {

	public static void assertMatch(IMatcher matcher, String text, int startOffset, int... expectedOffsets) {
		final List<Integer> list = new ArrayList<>();
		matcher.find(text, startOffset, text.length(), (m,t,s,e) -> {
			assertSame(matcher,m);
			assertSame(text,t);
			list.add(Integer.valueOf(s));
			list.add(Integer.valueOf(e));
		});
		assertEquals(expectedOffsets.length, list.size());
		for (int i = 0;  i < expectedOffsets.length;  i++) {
			assertEquals(String.valueOf(i), expectedOffsets[i], list.get(i).intValue());
		}
	}

	public static void assertNoMatch(IMatcher matcher, String text, int startOffset) {
		matcher.find(text, startOffset, text.length(), (m,t,s,e) -> {
			throw new IllegalStateException("Unexpected match");
		});
	}

}
