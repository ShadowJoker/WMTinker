package com.baidu.waimai.wmtinker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class PatchUpdateManager {

    private Context mContext;
    private BugUpgradeModel mUpdateModel;
    private File savePath = null;
    private File saveFile = null;
    private static final int DOWN_OVER = 1;
    private Thread downLoadThread;

    private SharedPreferences mPrefs;
    private static final String KEY_PATCH_MD5 = "tinker_patch_md5";

    public PatchUpdateManager(Context mContext, BugUpgradeModel updateModel) {
        super();
        this.mContext = mContext;
        this.mUpdateModel = updateModel;
        this.mPrefs = mContext.getSharedPreferences("pref_tinker_info", Context.MODE_PRIVATE);
    }

    /**
     * 判断是否需要安装补丁
     */
    public boolean needUpdate() {
        if (mUpdateModel == null || mUpdateModel.getFileMd5() == null || mUpdateModel.getFileMd5().isEmpty() || mUpdateModel.getFileMd5().equals(getCurrentPatchMD5())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 下载并安装补丁
     */
    public void downloadAndInstallPatch() {
        initPath();
        downLoadThread = new Thread(mdownPatchRunnable);
        downLoadThread.start();
    }

    private void initPath() {
        if (checkSDcard()) {
            savePath = new File(WMTinkerManager.PATCH_PATH_FOLDER);
            saveFile = new File(savePath.getPath(), "patch.zip");
        } else {
            savePath = new File(mContext.getFilesDir().getPath());
            saveFile = new File(savePath.getPath(), "/patch.zip");
            Process process;
            try {
                process = Runtime.getRuntime().exec("chmod 666 " + mContext.getFilesDir().getPath() + "/patch.zip");
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DOWN_OVER:
                    setCurrentPatchMD5(getFileMD5(saveFile));
                    WMTinkerManager.getInstance().installerPatchFromFile(WMTinkerManager.getInstance().getApplication(), saveFile.getAbsolutePath());
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private Runnable mdownPatchRunnable = new Runnable() {
        private InputStream is;
        private FileOutputStream fos;

        @Override
        public void run() {
            try {
                URL url = new URL(mUpdateModel.getUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                is = conn.getInputStream();
                if (!savePath.exists()) {
                    savePath.mkdir();
                }
                if (saveFile.exists()) {
                    saveFile.delete();
                    saveFile.createNewFile();
                } else {
                    saveFile.createNewFile();
                }
                fos = new FileOutputStream(saveFile);
                byte buf[] = new byte[1024];
                do {
                    int numread = is.read(buf);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean checkSDcard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public String getFileMD5(File file) {
        if (null == file || (null != file && !file.isFile())) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return byteArray2HexString(digest.digest());
    }

    public String byteArray2HexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            byte[] var5 = bytes;
            int var4 = bytes.length;
            for (int var3 = 0; var3 < var4; ++var3) {
                byte b = var5[var3];
                String str;
                for (str = Integer.toHexString(255 & b); str.length() < 2; str = "0" + str) {
                }
                sb.append(str);
            }
            return sb.toString();
        }
    }

    private String getCurrentPatchMD5() {
        return mPrefs.getString(KEY_PATCH_MD5, "");
    }

    private void setCurrentPatchMD5(String md5) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(KEY_PATCH_MD5, md5);
        editor.apply();
    }

}
