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


/** Abstract base class for deriving implementations of {@link IMatcher}.
 */
public abstract class AbstractMatcher implements IMatcher {

	/** Checks, whether a given word is in the given text at the given offset.
	 * @param text The text, in which the word should be present.
	 * @param offset The offset, at which to look for the word.
	 * @param word The word to look for.
	 * @param caseInsensitive Whether the match might be case insensitive.
	 * @return True, if the word is founf. Otherwise false.
	 */
	protected boolean isWordAt(String text, int offset, String word, boolean caseInsensitive) {
		if (offset+word.length() <= text.length()) {
			for (int i = 0;  i < word.length();  i++) {
				final char c1 = text.charAt(offset+i);
				final char c2 = word.charAt(i);
				if (caseInsensitive) {
					if (Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
						return false;
					}
				} else {
					if (c1 != c2) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

}
