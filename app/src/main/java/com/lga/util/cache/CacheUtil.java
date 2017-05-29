package com.lga.util.cache;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.LruCache;

import com.alibaba.fastjson.JSON;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Jay on 2017/5/26.
 * 封装缓存管理库
 */

public class CacheUtil {

    private Context mContext;
    private DiskLruCache mDiskLruCache;
    private LruCache<String, Object> mMemoryCache;

    public CacheUtil(Context context) {
        mContext = context;
    }

    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param cacheDir   缓存目录
     * @param appVersion 应用程序版本号，
     *                   当版本号改变，缓存路径下存储的所有数据都会被清除掉，
     *                   因为DiskLruCache认为当应用程序有版本更新的时候，所有的数据都应该从网上重新获取
     * @param valueCount 指定同一个key可以对应多少个缓存文件
     * @param maxSize    最多可以缓存的数据量，单位字节
     */
    public void openCache(File cacheDir, int appVersion, int valueCount, long maxSize) {
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 32;
        mMemoryCache = new LruCache<String, Object>(cacheSize) {

            @Override
            protected int sizeOf(String key, Object object) {
                return object.toString().length();
            }
        };

        try {
            mDiskLruCache = DiskLruCache.open(cacheDir, appVersion, valueCount, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置缓存目录
     *
     * @param uniqueName 目录名称
     * @return
     */
    public File setCacheDir(String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = mContext.getExternalCacheDir().getPath();
        } else {
            cachePath = mContext.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取应用版本号
     *
     * @return
     */
    public int getAppVersion() {
        try {
            return mContext
                    .getPackageManager()
                    .getPackageInfo(
                            mContext.getPackageName(),
                            0)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String getKeyByMD5(String url) {
        String cacheKey;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }

        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public Object getCachedObject(String url, Class clss) {
        String cacheKey = getKeyByMD5(url);
        Object object = getObjectFromMemory(cacheKey);
        if (object == null) {
            try {
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(cacheKey);
                if (snapshot != null) {
                    object = JSON.parseObject(snapshot.getString(0), clss);
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    private Object getObjectFromMemory(String key) {
        return mMemoryCache.get(key);
    }

    public void cacheObject(String url, Object object, String jsonString) {
        if (object == null) return;

        String cacheKey = getKeyByMD5(url);
        if (getObjectFromMemory(cacheKey) == null) {
            mMemoryCache.put(cacheKey, object);
        }

        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(cacheKey);
            if (editor != null) {
                editor.set(0, jsonString);
                editor.commit();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void deleteCacheByKey(String url) {
        String cacheKey = getKeyByMD5(url);
        if (getObjectFromMemory(cacheKey) == null) {
            mMemoryCache.remove(cacheKey);
        }

        try {
            mDiskLruCache.remove(cacheKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void flushCache() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
