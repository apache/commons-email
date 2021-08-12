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

import java.util.function.Function;

import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.re.IMatcher.TerminationRequest;

/** The {@link ImageTagHandler} implements the regular expressions
 * <pre>
 *   (&lt;[Ii][Mm][Gg]\\s*[^&gt;]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])
 * </pre>
 * , and,
 * <pre>
 *   (&lt;[Ss][Cc][Rr][Ii][Pp][Tt]\\s*.*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])
 * </pre>
 * ({@link ImageHtmlEmail#REGEX_IMG_SRC}, and {@link ImageHtmlEmail#REGEX_SCRIPT_SRC})
 * with instances of {@link IMatcher}.
 */
public class ImageTagHandler {
    private static class MatchFoundException extends TerminationRequest {
        private static final long serialVersionUID = 1206347581416053598L;
        private final int matchStartOffset, matchEndOffset;

        public MatchFoundException(int matchStartOffset, int matchEndOffset) {
            this.matchStartOffset = matchStartOffset;
            this.matchEndOffset = matchEndOffset;
        }

        public int getMatchStartOffset() {
            return matchStartOffset;
        }

        public int getMatchEndOffset() {
            return matchEndOffset;
        }
    }

	public String findReferences(String text, Function<String,String> editor) {
		final HtmlTagMatcher htm = new HtmlTagMatcher(new String[] {"img", "script"});
		final AttributeSkipper as = new AttributeSkipper();
		final AttributeValueMatcher avm = new AttributeValueMatcher("src");

		String txt = text;
		for (;;) {
			MatchFoundException mfe;
			try {
				htm.find(text, 0, text.length(), (m,t,s,e) -> {
					as.find(t, e, text.length(), (m2,t2,s2,e2) -> {
						avm.find(t2, e2, t2.length(), (m3,t3,s3,e3) -> {
							throw new MatchFoundException(avm.getAttributeValueStart(), avm.getAttributeValueEnd());
						});
					});
				});
				mfe = null;
			} catch (MatchFoundException e) {
				mfe = e;
			}
			if (mfe != null) {
				txt = txt.substring(0, mfe.matchStartOffset)
						+ editor.apply(txt.substring(mfe.matchStartOffset+1, mfe.matchEndOffset))
						+ txt.substring(mfe.matchEndOffset);
			} else {
				break;
			}
		}
		return txt;
	}
}
