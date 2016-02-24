package com.buyhatke.core;

import com.buyhatke.utils.FileUtils;
import com.buyhatke.utils.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PersistenceManager takes care of persisting cache entries in Local FileSystem.
 * DirectoryPath should have proper permissions so that ProDict could be able to create sub-folders and files.
 */
public class PersistenceManager {

    private final String directoryPath;
    private static final String filePrefix = "bucket_";
    private static final String fileSuffix = ".bin";
    private final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

    public PersistenceManager(String directoryPath) {
        this.directoryPath = directoryPath;
    }


    public void persist(Entry entry) throws IOException {
        logger.debug("Persisting Entry started: " + entry);
        String filePath = getRandomFileName(entry.getKey());
        String fileContent = "";
        final File file = new File(filePath);
        if(file.exists()) {
            try {
                fileContent = FileUtils.readFileToString(file);
            } catch (IOException e) {
                // It should never happen.
                e.printStackTrace();
            }
        }
        Map<String, Entry> allEntries = new HashMap<>(10, 0.75f);
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashMap<String, Entry>>() {

            }.getType());
        }

        allEntries.put(entry.getKey(), entry);

        // Remove all the expired values from the list before writing ...
        allEntries.values().removeIf(PersistenceManager::isExpired);
        FileUtils.writeStringToFile(file, new Gson().toJson(allEntries));
        logger.debug("Persisting Entry completed: " + entry);
    }

    private String getRandomFileName(String key) {
        int bucket = Math.abs(Hashing.hash(key) % 65535);
        return directoryPath + filePrefix + bucket + fileSuffix;
    }

    public Entry fetch(String key) {
        String filePath = getRandomFileName(key);
        String fileContent = "";
        final File file = new File(filePath);
        if(file.exists()) {
            try {
                fileContent = FileUtils.readFileToString(file);
            } catch (IOException e) {
                // It should never happen.
                logger.error("Reading file failed: " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        Map<String, Entry> allEntries = new HashMap<>();
        if(!fileContent.equals("")) {
            allEntries = new Gson().fromJson(fileContent, new TypeToken<HashMap<String, Entry>>() {

            }.getType());
        }
        if(allEntries.containsKey(key)) {
            final Entry entry = allEntries.get(key);
            if(!isExpired(entry)) {
                return entry;
            } else {
                logger.debug("Entry expired: " + entry);
            }
        }

        return null;
    }

    public static boolean isExpired(Entry entry) {
        long timeNow = new Date().getTime();
        long createdTime = entry.getCreatedAt().getTime();

        final Integer duration = entry.getExpiresIn();

        // If expire duration is 0, it means it never expires.
        if(duration == 0) return false;
        long expiresIn = createdTime + entry.getExpiresInUnit().toMillis(duration);
        return expiresIn <= timeNow;
    }
}
