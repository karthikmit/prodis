import com.buyhatke.core.Entry;
import com.buyhatke.core.ProDict;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests for ProDict.
 */
public class ProDictTests {

    private final String directoryPath = "/tmp/";

    @Test
    public void testProDictGetAndPut() {
        ProDict proDict = new ProDict(100, directoryPath);

        final String value = "Hello World!";
        final Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);
        Entry result = proDict.get(entry.getKey());
        Assert.assertEquals(result.getValue(), value);
    }

    @Test
    public void testProDictGetAndPutBeyondCapacityAndCheckInMemoryGet() {
        ProDict proDict = new ProDict(1, directoryPath);

        final String value = "Hello World!";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue(value);
        proDict.put(entry);

        Entry result = proDict.getOnlyIfInMemory("Test-Karthik-1".toLowerCase());
        Assert.assertEquals(result, null);
    }

    @Test
    public void testExpiredItems() throws InterruptedException, IOException {
        ProDict proDict = new ProDict(1, directoryPath);

        final String value = "Hello World!";
        final String key = "Test-Karthik-1".toLowerCase();
        Entry entry = new Entry().setExpiresIn(1).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey(key).setValue(value);
        proDict.put(entry);
        entry = new Entry().setExpiresIn(1).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey(key + "_somerandomstuff").setValue(value);
        proDict.put(entry);

        Thread.sleep(3 * 1000);
        Assert.assertEquals(proDict.get(key), null);
        proDict.flush();
    }

    @Test
    public void testProDictGetAndPutBeyondCapaity() {
        ProDict proDict = new ProDict(1, directoryPath);

        final String value = "Hello World 123";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue(value);
        proDict.put(entry);

        Entry result = proDict.get("Test-Karthik-1".toLowerCase());
        Assert.assertNotEquals(result, null);
    }

    @Test
    public void testProDictGetAndPutForOrder() {
        ProDict proDict = new ProDict(2, directoryPath);

        final String value = "Hello World 1";
        Entry entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-1".toLowerCase()).setValue(value);
        proDict.put(entry);

        entry = new Entry().setExpiresIn(10).setExpiresInUnit(TimeUnit.SECONDS)
                .setKey("Test-Karthik-2".toLowerCase()).setValue("Hello world 2");
        proDict.put(entry);

        List<Entry> all = proDict.getAll();

        Assert.assertEquals(all.get(0).getKey(), "Test-Karthik-2".toLowerCase());

        // Fetch call should rearrange the inner entries.
        proDict.get("Test-Karthik-1".toLowerCase());
        all = proDict.getAll();
        Assert.assertEquals("Test-Karthik-1".toLowerCase(), all.get(0).getKey());
    }
}
