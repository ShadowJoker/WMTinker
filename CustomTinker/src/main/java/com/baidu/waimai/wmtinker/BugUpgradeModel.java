package com.baidu.waimai.wmtinker;

/**
 * 热修复升级数据模型
 * Created by ZhangPeng on 17/4/10.
 */

public class BugUpgradeModel {
    private String product_name;
    private String from;
    private String sv;
    private String force_start;
    private String file_md5;
    private String url;

    public BugUpgradeModel(String product_name, String from, String sv, String force_start, String file_md5, String url) {
        this.product_name = product_name;
        this.from = from;
        this.sv = sv;
        this.force_start = force_start;
        this.file_md5 = file_md5;
        this.url = url;
    }

    public String getProductName() {
        return product_name;
    }

    public String getFrom() {
        return from;
    }

    public String getSv() {
        return sv;
    }

    public String getForceStart() {
        return force_start;
    }

    public String getFileMd5() {
        return file_md5;
    }

    public String getUrl() {
        return url;
    }
}
