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


/** An {@link IMatcher} is a helper classes, that is designed to replace
 * regular expressions, like
 * <pre>
 *   (&lt;[Ii][Mm][Gg]\\s*[^&gt;]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])
 * </pre>
 * The reason for using the helper classes, and not a simple regular
 * expression is a performance problem of the latter, that we wish to overcome.
 * The main idea of the {@link IMatcher} is to divide the regular expression into a
 * cascade of so-called matchers. A matcher replaces a comparatively simple
 * subexpression, like <pre>\\s*</pre>. The simplicity of the replaced expression
 * allows a manual, performace optimized implementation of the corresponding
 * matcher.
 */
public interface IMatcher {
    /** This exception is being thrown, if the search for matches should
     * be terminated.
     */
    public static class TerminationRequest extends RuntimeException {
        private static final long serialVersionUID = 5706488409254083692L;

        public TerminationRequest() {
        }
    }
	/** Interface of a match listener, which is being invoked, if a match
	 * has been found.
	 */
	public interface IListener {
        /** Called, if a match has been found.
         * @param matcher The matcher, which is sending the notification.
         * @param text The text, in which a match has been found.
         * @param startOffset Offset of the matches first character in the text.
         * @param endOffset Offset of the first character in the text, that
         * follows after the match.
         * @throws TerminationRequest The caller is supposed to stop
         * searching for further matches.
         */
        void match(IMatcher matcher, String text, int startOffset, int endOffset) throws TerminationRequest;
    }

    /** Called to find matches in the given text.
     * @param text The text, in which a match has been found.
     * @param startOffset Offset of the matches first character in the text.
     * @param endOffset Offset of the first character in the text, that
     * follows after the match.
     * @param listener The listener, which is being notified in case of matches.
     * @throws TerminationRequest The caller is supposed to stop
     * searching for further matches.
     */
    void find(String text, int startOffset, int endOffset, IListener listener) throws TerminationRequest;
}
