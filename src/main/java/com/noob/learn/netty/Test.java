package com.noob.learn.netty;

import lombok.AllArgsConstructor;
import lombok.Data;

public class Test {
    private static void function(String a, int b) {
        a = "100a";
        b = 1000;
    }

    private static void function2(Arg arg) {
        arg.arg1 = "function2";
        arg.arg2 = 100;
    }

    public static void main(String[] args) {
        String arg1 = "arg1";
        int arg2 = 0;
        function(arg1, arg2);
        System.out.println(String.format("arg1=%s, arg2=%s", arg1, arg2));
        Arg arg = new Arg("arg1", 0);
        function2(arg);
        System.out.println(arg.toString());

    }

}

@Data
@AllArgsConstructor
class Arg {
    public String arg1;
    public int    arg2;
}
