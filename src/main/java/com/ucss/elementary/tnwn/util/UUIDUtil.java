package com.ucss.elementary.tnwn.util;

import java.util.UUID;

public class UUIDUtil {

    //生成不带-的UUID
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}