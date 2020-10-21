/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import java.util.HashMap;
import java.util.Map;

public class RefTest {


    public static void main(String[] args) {
        //test1();
        //new RefTest().test2();
        new RefTest().test3();
    }

    public static void test1() {
        Map<String, String> arg = new HashMap<>();
        arg.put("1", "2");

        System.out.println("1: " + arg);

        Map<String, String> arg2 = arg;

        System.out.println("2: " + arg);
        System.out.println("2: " + arg2);

        arg = null;

        System.out.println("3: " + arg);
        System.out.println("3: " + arg2);
    }


    public void test2() {
        Map<String, String> arg = new HashMap<>();
        arg.put("1", "2");

        System.out.println("1: " + arg);

        test2innner(arg);

        System.out.println("4: " + arg);

    }

    public void test2innner(Map<String, String> arg) {
        Map<String, String> arg2 = arg;

        System.out.println("2: " + arg);
        System.out.println("2: " + arg2);

        arg.put("1", "MURAT");
        arg = null;

        System.out.println("3: " + arg);
        System.out.println("3: " + arg2);

    }

    public void test3() {
        Map<String, String> arg = new HashMap<>();
        arg.put("1", "2");

        System.out.println("1: " + arg);

        test3innner(arg);

        System.out.println("5: " + arg);

    }

    public void test3innner(Map<String, String> arg) {
        Map<String, String> arg2 = arg;

        System.out.println("2: " + arg);
        System.out.println("2: " + arg2);

        arg = new HashMap<>();
        arg.put("NEW", "NEW");

        System.out.println("3: " + arg);
        System.out.println("3: " + arg2);

        arg = null;

        System.out.println("4: " + arg);
        System.out.println("4: " + arg2);

    }
}
