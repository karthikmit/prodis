package com.buyhatke.prodis.core;

import com.buyhatke.core.Entry;
import com.buyhatke.core.ProDict;
import com.buyhatke.prodis.exceptions.NoKeyFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ProDict Facade is a wrapper for the internal ProDict lib and exposes succinct APIs for ProDis.
 */
@Component
public class ProDictFacade {

    //TODO: Should be kept in Configuration file rather than hard-coding
    public static final int CAPACITY = 100 * 1000;
    private ProDict proDict;
    private Logger logger = LoggerFactory.getLogger(ProDictFacade.class);

    public ProDictFacade() {
        this.proDict = new ProDict(CAPACITY, "/data/prodict/");
    }

    public void put(Entry entry) {
        proDict.put(entry);
    }

    public Entry get(String key) {
        final Entry entryStored = proDict.get(key);
        if(entryStored == null)
            throw new NoKeyFoundException(key + " doesn't exist");
        return entryStored;
    }

    public void close() {
        try {
            this.proDict.flush();
        } catch (IOException e) {
            logger.error("ProDict Flush failed, may be the data stored in cache is lost !!");
            e.printStackTrace();
        }
    }
}
