package com.baidu.waimai.wmtinker;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.listener.PatchListener;
import com.tencent.tinker.lib.patch.AbstractPatch;
import com.tencent.tinker.lib.patch.UpgradePatch;
import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.reporter.LoadReporter;
import com.tencent.tinker.lib.reporter.PatchReporter;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tinkerpatch.sdk.tinker.service.TinkerServerResultService;

import java.io.File;

/**
 * 外卖自定义TinkerSDK对外统一接口
 * Created by ZhangPeng on 17/4/10.
 */

public class WMTinkerManager {

    private static final WMTinkerManager instance = new WMTinkerManager();
    public static String PATCH_PATH_FOLDER = "";
    private Application application;
    private ApplicationLike applicationLike;

    private WMTinkerManager() {
    }

    public static WMTinkerManager getInstance() {
        return instance;
    }

    public Application getApplication() {
        return application;
    }

    /**
     * 初始化TinkerSDK，建议在Application的onCreat()中加入，紧随super.onCreat()
     */
    public void initTinker(ApplicationLike tinkerApplicationLike) {
        if (tinkerApplicationLike == null) {
            return;
        }
        this.applicationLike = tinkerApplicationLike;
        this.application = applicationLike.getApplication();
        PATCH_PATH_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + application.getPackageName() + ".tinker";
        // 自定义Tinkera安装
        //or you can just use DefaultLoadReporter
        LoadReporter loadReporter = new DefaultLoadReporter(tinkerApplicationLike.getApplication());
        //or you can just use DefaultPatchReporter
        PatchReporter patchReporter = new DefaultPatchReporter(tinkerApplicationLike.getApplication());
        //or you can just use DefaultPatchListener
        PatchListener patchListener = new DefaultPatchListener(tinkerApplicationLike.getApplication());
        //you can set your own upgrade patch if you need
        AbstractPatch upgradePatchProcessor = new UpgradePatch();
        TinkerInstaller.install(tinkerApplicationLike,
                loadReporter, patchReporter, patchListener,
                TinkerServerResultService.class, upgradePatchProcessor
        );
    }

    /**
     * 判断Tinker是否已启用，如果未启用，不建议调用init方法
     */
    public boolean isTinkerEnable(Application application) {
        return (Tinker.isTinkerInstalled() && Tinker.with(application).isTinkerEnabled());
    }

    /**
     * 根据指定文件路径来安装补丁
     */
    public void installerPatchFromFile(Context context, String filePath) {
        TinkerInstaller.onReceiveUpgradePatch(context, filePath);
    }

    /**
     * 根据服务端的热修复数据model来自动安装补丁
     */
    public void installerPatchFromDataModel(Context context, BugUpgradeModel model) {
        PatchUpdateManager manager = new PatchUpdateManager(context, model);
        if (context != null && model != null && manager.needUpdate()) {
            manager.downloadAndInstallPatch();
        }
    }

}
