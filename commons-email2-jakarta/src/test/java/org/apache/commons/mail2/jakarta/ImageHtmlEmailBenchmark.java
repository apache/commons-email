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
package org.apache.commons.mail2.jakarta;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHtmlEmailBenchmark  {

    private final static int MATCHES_TO_FIND = 50;

    private final static String TEST_HTML = testHtml();

    public final static TestHarness testHarnessCurrentRegex = new TestHarness(
            ImageHtmlEmail.REGEX_IMG_SRC,
            ImageHtmlEmail.REGEX_SCRIPT_SRC
    );

    /**
     * The original regex pre EMAIL-198
     */
    public final static TestHarness testHarnessOriginalRegex = new TestHarness(
            "(<[Ii][Mm][Gg]\\s*[^>]*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])",
            "(<[Ss][Cc][Rr][Ii][Pp][Tt]\\s*.*?\\s+[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])"
    );

    /**
     * Test an alternative regex with a non-greedy url: note the {@code ([^"']+?)} capture.
     * This should be slower than the current regex test case.
     */
    public final static TestHarness testHarnessCurrentRegexNonGreedyUrl = new TestHarness(
            "(<[Ii][Mm][Gg](?=\\s)[^>]*?\\s[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])",
            "(<[Ss][Cc][Rr][Ii][Pp][Tt](?=\\s)[^>]*?\\s[Ss][Rr][Cc]\\s*=\\s*[\"'])([^\"']+?)([\"'])"
    );

    @Benchmark
    public void testCaseCurrentRegex(Blackhole blackhole) {
        testHarnessCurrentRegex.runTest(blackhole);
    }

    @Benchmark
    public void testCaseOriginalRegex(Blackhole blackhole) {
        testHarnessOriginalRegex.runTest(blackhole);
    }

    @Benchmark
    public void testCaseCurrentRegexNonGreedyUrl(Blackhole blackhole) {
        testHarnessCurrentRegexNonGreedyUrl.runTest(blackhole);
    }

    public static class TestHarness {
        private Matcher matcherImg;
        private Matcher matcherScript;

        public TestHarness(String patternImg, String patternScript){
            matcherImg = prepareRegex(patternImg);
            matcherScript = prepareRegex(patternScript);
        }

        public void runTest(Blackhole blackhole) {
            for (int i = 0; i < MATCHES_TO_FIND; i++) {
                blackhole.consume(matcherImg.find());
                blackhole.consume(matcherScript.find());
            }
            matcherImg.reset();
            matcherScript.reset();
        }

        public Matcher prepareRegex(String pattern) {
            return Pattern.compile(pattern).matcher(TEST_HTML);
        }
    }

    private static String testHtml() {
        String longUrl = new String(new char[200]).replace("\0", "a");
        String longSpace = new String(new char[100]).replace("\0", " ");
        StringBuilder html = new StringBuilder();
        html.append("<html><body><pre>");
        html.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        for (int i = 0; i < MATCHES_TO_FIND; i++) {
            html.append("<img" + longSpace +  "xxx=\"no-src-attribute\">");
            html.append("<script    some=\"true\" other=\"1\" attributes=\"yes\"  src = \"" + longUrl + "\">");
            html.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        }
        html.append("</pre></body></html>");
        return html.toString();
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
