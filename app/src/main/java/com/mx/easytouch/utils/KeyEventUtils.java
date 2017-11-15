package com.mx.easytouch.utils;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maoxin on 2017/10/13.
 */


public class KeyEventUtils {

    public static String getEventCode(String input){
        Map<String, String> codeMap = new HashMap<String, String>();
        codeMap.put("0", "7");
        codeMap.put("1", "8");
        codeMap.put("2", "9");
        codeMap.put("3", "10");
        codeMap.put("4", "11");
        codeMap.put("5", "12");
        codeMap.put("6", "13");
        codeMap.put("7", "14");
        codeMap.put("8", "15");
        codeMap.put("9", "16");

        codeMap.put("a", "29");
        codeMap.put("b", "30");
        codeMap.put("c", "31");
        codeMap.put("d", "32");
        codeMap.put("e", "33");
        codeMap.put("f", "34");
        codeMap.put("g", "35");
        codeMap.put("h", "36");
        codeMap.put("i", "37");
        codeMap.put("j", "38");
        codeMap.put("k", "39");
        codeMap.put("l", "40");
        codeMap.put("m", "41");
        codeMap.put("n", "42");
        codeMap.put("o", "43");
        codeMap.put("p", "44");
        codeMap.put("q", "45");
        codeMap.put("r", "46");
        codeMap.put("s", "47");
        codeMap.put("t", "48");
        codeMap.put("u", "49");
        codeMap.put("v", "50");
        codeMap.put("w", "51");
        codeMap.put("x", "52");
        codeMap.put("y", "53");
        codeMap.put("z", "54");
        codeMap.put(" ", "62"); //空格
        codeMap.put("#", "66"); //回车
        return codeMap.get(input);
    }

}
