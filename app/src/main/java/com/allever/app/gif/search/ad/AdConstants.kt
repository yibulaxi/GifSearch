package com.allever.app.gif.search.ad

import com.allever.lib.ad.ADType
import com.allever.lib.ad.AdBusiness

object AdConstants {

    //adeverdeng
    private val APPID = "ca-app-pub-9972782174497381~7798072468"
    private const val COMMON_BANNER = "ca-app-pub-9972782174497381/5280748004"
    private const val EXIT_INSERT = "ca-app-pub-9972782174497381/7195437239"
    private const val DETAIL_INSERT = "ca-app-pub-9972782174497381/5499212180"
    private const val COMMON_VIDEO = "ca-app-pub-9972782174497381/5882355569"

    val AD_NAME_EXIT_INSERT = "AD_NAME_EXIT_INSERT"

    val AD_NAME_DETAIL_INSERT = "AD_NAME_DETAIL_INSERT"

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
            "      \"name\": \"$AD_NAME_EXIT_INSERT\",\n" +
            "      \"type\": \"${ADType.INSERT}\",\n" +
            "      \"chain\": [\n" +
            "        {\n" +
            "          \"business\": \"${AdBusiness.A}\",\n" +
            "          \"adPosition\": \"$EXIT_INSERT\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"$AD_NAME_DETAIL_INSERT\",\n" +
            "      \"type\": \"${ADType.INSERT}\",\n" +
            "      \"chain\": [\n" +
            "        {\n" +
            "          \"business\": \"${AdBusiness.A}\",\n" +
            "          \"adPosition\": \"$DETAIL_INSERT\"\n" +
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
