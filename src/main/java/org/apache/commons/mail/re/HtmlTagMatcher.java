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

import java.util.Arrays;
import java.util.List;

/** A {@link IMatcher} for opening HTML tags, like <pre>&lt;img</pre>,
 * or <pre>&lt;src</pre>. Ignores attribute definitions, and the closing
 * &gt; character.
 */
public class HtmlTagMatcher extends AbstractMatcher {
    private final List<String> elementNames;
    private String currentElementName;

    public HtmlTagMatcher(String[] elementNames) {
        this.elementNames = Arrays.asList(elementNames);
    }

    @Override
    public void find(String text, int startOffset, int endOffset, IMatcher.IListener listener) {
        for (int i = startOffset;   i < endOffset;  i++) {
            final char c = text.charAt(i);
            if (c == '<') {
                for (int j = 0;  j < elementNames.size();  j++) {
					final String elementName = elementNames.get(j);
					if (isWordAt(text, i+1, elementName, true)) {
						currentElementName = elementName;
						listener.match(this, text, i, i+1+elementName.length());
						currentElementName = null;
					}
                }
            }
        }
    }

    public String getCurrentElementName() {
    	if (currentElementName == null) {
    		throw new IllegalStateException("The current element name is only available inside IListener.match()");
    	}
    	return currentElementName;
    }
}
