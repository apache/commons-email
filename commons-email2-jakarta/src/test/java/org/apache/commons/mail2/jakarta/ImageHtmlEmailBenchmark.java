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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHtmlEmailBenchmark  {

    @Benchmark
    public void testRegularExpressions(BenchmarkContext context, Blackhole blackhole) {
        blackhole.consume(context.matcherImg.find());
        blackhole.consume(context.matcherScript.find());
        context.matcherImg.reset();
        context.matcherScript.reset();
    }

    @State(Scope.Thread)
    public static class BenchmarkContext {
        Matcher matcherImg;
        Matcher matcherScript;

        @Setup
        public void prepare() {
            StringBuilder html = new StringBuilder();
            html.append("<html><body><pre>");
            html.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
            html.append("<img    some=\"true\" other=\"1\" attributes=\"yes\"  src = \"this-might-be-a-long-url-url\">");
            html.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
            html.append("</pre></body></html>");
            Pattern patternImg = Pattern.compile(ImageHtmlEmail.REGEX_IMG_SRC);
            matcherImg = patternImg.matcher(html);
            Pattern patternScript = Pattern.compile(ImageHtmlEmail.REGEX_SCRIPT_SRC);
            matcherScript = patternScript.matcher(html);
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
