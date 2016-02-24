package com.buyhatke.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ProDictList Map is sort of LinkedHashMap with added features like removeLastEntry, etc.
 */
class ProDictListMap {

    private final Logger logger = LoggerFactory.getLogger(ProDictListMap.class);
    private final Deque<Entry> entriesList;
    private final Map<String, Entry> entriesMap;
    private AtomicInteger currentSize = new AtomicInteger(0);

    public ProDictListMap(int capacity) {
        entriesList = new ConcurrentLinkedDeque<>();
        entriesMap = new ConcurrentHashMap<>(capacity, 0.75f);
    }

    /**
     * Put operation is thread safe as it uses concurrent variants for Deque and Map.
     * Since adding entry in List and Map are independent operations,
     * thread ordering of executions doesn't matter, as long as every entry in Map has corresponding entry in List also.
     * It will update the entry at the head of list, so it is the most recently used.
     * If key exists in the system, entry will be updated with new value.
     * @param key for the entry
     * @param entry to be saved or updated.
     */
    public void put(String key, Entry entry) {
        entriesList.addFirst(entry);
        entriesMap.put(key, entry);
        currentSize.incrementAndGet();
    }

    public boolean containsKey(String key) {
        return entriesMap.containsKey(key);
    }

    /**
     * Removes the entry from In Memory cache. It is thread-safe.
     * It will not remove the entry if the entry is there in file system.
     * @param key to remove from cache.
     */
    public void remove(String key) {
        Entry entry = entriesMap.get(key);
        if(entry != null) {
            entriesList.remove(entry);
            entriesMap.remove(entry);
            currentSize.decrementAndGet();
        }
    }

    public int size() {
        return currentSize.get();
    }

    public Entry get(String key) {
        return entriesMap.get(key);
    }

    /**
     * Removes Least recently used entry in In Memory cache and returns the same.
     * @return removed element.
     */
    public Entry removeLastEntry() {
        logger.debug("Removing Last Entry from the In Memory cache");
        Entry v = entriesList.pollLast();
        entriesMap.remove(v.getKey());
        currentSize.decrementAndGet();
        return v;
    }

    public List<Entry> getAll() {
        List<Entry> entriesListNew = new ArrayList<>();
        entriesListNew.addAll(entriesList);
        return entriesListNew;
    }

    public void moveEntryToHead(Entry entry) {
        entriesList.remove(entry);
        entriesList.addFirst(entry);
    }
}
