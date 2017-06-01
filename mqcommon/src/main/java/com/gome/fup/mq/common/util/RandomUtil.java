package com.gome.fup.mq.common.util;

import java.util.Random;

/**
 * Created by fupeng-ds on 2017/5/27.
 */
public class RandomUtil {

    private static Random random = new Random();

    public static int random(int n) {
        return random.nextInt(n);
    }
}
