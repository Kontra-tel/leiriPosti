package tel.kontra.leiriposti.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HashUtilTest {

    private HashUtil hashUtil;
    private final String salt = "testSalt";

    @BeforeEach
    void setUp() {
        hashUtil = new HashUtil(salt, HashAlgorithm.SHA_256);
    }

    @Test
    void testHashAndVerifyHash() {
        String input = "password123";
        String hash = hashUtil.hash(input, HashAlgorithm.SHA_256);
        assertNotNull(hash);
        assertTrue(hashUtil.verifyHash(input, hash, HashAlgorithm.SHA_256));
        assertFalse(hashUtil.verifyHash("wrongPassword", hash, HashAlgorithm.SHA_256));
    }

    @Test
    void testHashWithTruncation() {
        String input = "truncateMe";
        int truncLength = 16;
        String hash = hashUtil.hash(input, HashAlgorithm.SHA_256, truncLength);
        assertNotNull(hash);
        assertEquals(truncLength, hash.length());
    }

    @Test
    void testGetAppSalt() {
        assertEquals(salt, hashUtil.getAppSalt());
    }

    @Test
    void testEncode() {
        String input = "encodeTest";
        String encoded = hashUtil.encode(input);
        assertNotNull(encoded);
        assertNotEquals(input, encoded);
    }

    @Test
    void testMatches() {
        String input = "matchMe";
        String encoded = hashUtil.encode(input);
        assertTrue(hashUtil.matches(input, encoded));
        assertFalse(hashUtil.matches("notMe", encoded));
    }
}
