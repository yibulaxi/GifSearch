package com.allever.app.template.ad

import com.allever.lib.ad.ADType
import com.allever.lib.ad.AdBusiness

object AdConstants {

    private val APPID = ""
    private const val COMMON_BANNER = "ca-app-pub-5576030072710638/7114444166"
    private const val COMMON_INSERT = "ca-app-pub-5576030072710638/8257819654"
    private const val COMMON_VIDEO = "ca-app-pub-5576030072710638/8639278822"

    val AD_NAME_INSERT = "AD_NAME_INSERT"

    val AD_NAME_BANNER = "AD_NAME_BANNER"

    val AD_NAME_VIDEO = "AD_NAME_VIDEO"

    val adData = "{\n" +
            "  \"business\": [\n" +
            "    {\n" +
            "      \"name\": \"${AdBusiness.A}\",\n" +
            "      \"appId\": \"\",\n" +
            "      \"appKey\": \"\",\n" +
            "      \"token\": \"\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"adConfig\": [\n" +
            "    {\n" +
            "      \"name\": \"$AD_NAME_INSERT\",\n" +
            "      \"type\": \"${ADType.INSERT}\",\n" +
            "      \"chain\": [\n" +
            "        {\n" +
            "          \"business\": \"${AdBusiness.A}\",\n" +
            "          \"adPosition\": \"$COMMON_INSERT\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"$AD_NAME_BANNER\",\n" +
            "      \"type\": \"${ADType.BANNER}\",\n" +
            "      \"chain\": [\n" +
            "        {\n" +
            "          \"business\": \"${AdBusiness.A}\",\n" +
            "          \"adPosition\": \"$COMMON_BANNER\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"$AD_NAME_VIDEO\",\n" +
            "      \"type\": \"${ADType.VIDEO}\",\n" +
            "      \"chain\": [\n" +
            "        {\n" +
            "          \"business\": \"${AdBusiness.A}\",\n" +
            "          \"adPosition\": \"$COMMON_VIDEO\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n"
}
