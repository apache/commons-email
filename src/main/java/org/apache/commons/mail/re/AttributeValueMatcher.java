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

public class AttributeValueMatcher extends AbstractMatcher {
	private final String expectedAttributeName;
	private int attributeValueStart = -1;
	private int attributeValueEnd = -1;

	public AttributeValueMatcher(String expectedAttributeName) {
		this.expectedAttributeName = expectedAttributeName;
	}

	@Override
	public void find(String text, int startOffset, int endOffset, IListener listener) throws TerminationRequest {
	}

	public int getAttributeValueStart() {
		if (attributeValueStart == -1) {
    		throw new IllegalStateException("The attribute value start is only available inside IListener.match()");
		}
		return attributeValueStart;
	}

	public int getAttributeValueEnd() {
		if (attributeValueEnd == -1) {
    		throw new IllegalStateException("The attribute value end is only available inside IListener.match()");
		}
		return attributeValueEnd;
	}
}
