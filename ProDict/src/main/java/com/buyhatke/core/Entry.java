package com.buyhatke.core;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entry is the unit of data which can be stored in ProDict Cache.
 * As of now, expire time shall be specified only on created time basis. Future it may be extended to access time also.
 * Entry setters can be used in fluent style, to avoid the need for multiple constructors.
 */
public class Entry {

    private static final Pattern ACCEPTABLE_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
    private final Date createdAt = new Date();
    private String key;
    private String value;

    // 0 indicates cache entry will not be evicted at all.
    private Integer expiresIn = 0;
    private TimeUnit expiresInUnit = TimeUnit.MINUTES;
    
    public Entry() {
        
    }


    public String getKey() {
        return key;
    }

    public Entry setKey(String key) {
        validateKey(key);
        this.key = key;
        return this;
    }

    private void validateKey(String key) {
        Matcher matcher = ACCEPTABLE_PATTERN.matcher(key);
        if (!matcher.matches() || key.length() < 8) {
            throw new IllegalArgumentException(
                    "keys must match regex [a-z0-9_-]{1,120} and should be of size more than 8 : \"" + key + "\"");
        }
    }

    public String getValue() {
        return value;
    }

    public Entry setValue(String value) {
        this.value = value;
        return this;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Entry setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public TimeUnit getExpiresInUnit() {
        return expiresInUnit;
    }

    public Entry setExpiresInUnit(TimeUnit expiresInUnit) {
        this.expiresInUnit = expiresInUnit;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entry)) return false;

        Entry entry = (Entry) o;

        return key.equals(entry.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Entry{" +
                "createdAt=" + createdAt +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresInUnit=" + expiresInUnit +
                '}';
    }
}
