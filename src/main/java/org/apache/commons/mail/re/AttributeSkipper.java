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

/** An implementation of {@link IMatcher}, that implements the regular expression
 * <pre>
 *   \\s*[^&gt;]*?\\s+
 * </pre>
 */
public class AttributeSkipper extends AbstractMatcher {
	@Override
	public void find(String text, int startOffset, int endOffset, IListener listener) throws TerminationRequest {
		// Implement the initial \s*
		int offset2 = startOffset;
		for (int i = startOffset;  i < endOffset;  i++) {
			final char c = text.charAt(i);
			if (!Character.isWhitespace(c)) {
				break;
			} else {
				++offset2;
			}
		}
		if (offset2 > startOffset) {
			listener.match(this, text, startOffset, offset2);
		}
		// Implement the [^>]*
		int offset3 = offset2;
		for (int i = startOffset;  i < endOffset;  i++) {
			final char c = text.charAt(i);
			if (Character.isWhitespace(c)) {
				for (int j = offset3;  j < endOffset;  j++) {
					if (Character.isWhitespace(text.charAt(j))) {
						if (j+1 > offset2) {
							listener.match(this, text, startOffset, j+1);
						} else {
							// Already notified, do nothing.
						}
					}
				}
			} else if (c == '>') {
				return;
			} else {
				++offset3;
			}
		}
	}

}
