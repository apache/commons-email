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

import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AttributeSkipperTest {
	/** Test the {@link AttributeSkipper} alone.
	 */
	@Test
	public void testStandalone() {
		final AttributeSkipper as = new AttributeSkipper();
		MatcherTests.assertMatch(as, "<img src='foo'>", 0, 0, 5);
		MatcherTests.assertMatch(as, "<img src='foo'>", 4, 4, 5);
		MatcherTests.assertNoMatch(as, "", 0);
	}

	/** Test the {@link AttributeSkipper} in combination with the {@link HtmlTagMatcher}.
	 */
	@Test
	public void testChain() {
		assertChainMatch("<img src='foo'>", 0, 4, 5);
	}

	private void assertChainMatch(String text, int startOffset, int... expectedOffsets) {
		final HtmlTagMatcher htm = new HtmlTagMatcher(new String[] {"script", "img"});
		final AttributeSkipper as = new AttributeSkipper();
		final List<Integer> actualOffsets = new ArrayList<>();
		htm.find(text, startOffset, text.length(), (m,t,s,e) -> {
			assertSame(htm,m);
			assertSame(text,t);
			as.find(text, e, text.length(), (m2,t2,s2,e2) -> {
				assertSame(as,m2);
				assertSame(text,t2);
				actualOffsets.add(Integer.valueOf(s));
				actualOffsets.add(Integer.valueOf(e));
			});
		});
	}
}
