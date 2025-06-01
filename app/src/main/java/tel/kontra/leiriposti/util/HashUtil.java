package tel.kontra.leiriposti.util;

import java.security.MessageDigest;

/**
 * Utility class for hashing strings including a password encoder.
 * 
 * Written originally for Expense Tracker schoolproject application
 * in 2024, then modified for further use.
 * 
 * @see HashAlgorithm
 * 
 * @version 2.0
 * @author Markus Vallin - Metropolia UAE
 */
public class HashUtil {
    
    // Application salt
    // Get from properties file
    private String appSalt;
    private HashAlgorithm passwordAlgorithm;

    /**
     * Constructor
     * 
     * @param appSalt Application salt
     * @param passwordAlgorithm Password hashing algorithm
     */
    public HashUtil(String appSalt, HashAlgorithm passwordAlgorithm) {
        this.appSalt = appSalt;
        this.passwordAlgorithm = passwordAlgorithm;
    }

    /**
     * Constructor
     * 
     * @param appSalt Application salt
     */
    public HashUtil(String appSalt) {
        this(appSalt, HashAlgorithm.SHA_256); // Default to SHA-256 if no algorithm is specified
    }
    
    /**
     * Hash a string using the specified hash algorithm.
     * 
     * @param stringToHash The string to hash.
     * @param algorithm The hash algorithm to use.
     * @return The hashed string in hexadecimal format.
     * 
     * This method hashes the input string using the specified hash algorithm
     */
    public String hash(String stringToHash , HashAlgorithm algorithm) {
        return hashUtil(stringToHash, algorithm);
    }

    /**
     * Hash a string using the specified hash algorithm and truncate the result to a specified length.
     * 
     * @param stringToHash The string to hash.
     * @param algorithm The hash algorithm to use.
     * @param truncationLength The length to truncate the hash to.
     * @return The hashed string truncated to the specified length.
     * 
     * This method hashes the input string using the specified hash algorithm
     * and truncates the result to the specified length if it exceeds that length.
     * 
     * Truncated hashes can't be verified, so this method is primarily
     * used for generating identifiers or keys where a shorter hash is sufficient.
     */
    public String hash(String stringToHash, HashAlgorithm algorithm, int truncationLength) {
        String hash = hashUtil(stringToHash, algorithm);
        if (hash.length() > truncationLength) {
            return hash.substring(0, truncationLength); // Truncate the hash to the specified length
        }
        return hash; // Return the full hash if it's shorter than the truncation length
    }

    /**
     * Hashing method that takes a string and a hash algorithm,
     * 
     * @param stringToHash
     * @param algorithm
     * @return
     */
    private String hashUtil(String stringToHash, HashAlgorithm algorithm) {
        
        String hash = null;

        try {
            
            // Create MessageDigest instance for selected algorithm
            MessageDigest md = MessageDigest.getInstance(algorithm.getAlgorithm());
            
            // Add salt
            md.update(appSalt.getBytes());

            // Add string to hash
            byte[] bytes = md.digest(stringToHash.getBytes());

            // Bytes to hex
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Get complete hash
            hash = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * Verify hash
     * 
     * @param stringToVerify
     * @param hash
     * @param algorithm
     * @return
     */
    public Boolean verifyHash(String stringToVerify, String hash, HashAlgorithm algorithm) {
        return hash(stringToVerify, algorithm).equals(hash);
    }

    /**
     * Get application salt
     * 
     * @return
     */
    public String getAppSalt() {
        return appSalt;
    }

    /**
     * Set application salt
     * 
     * @param appSalt
     */
    public String encode(CharSequence rawCharSeq) {
        return hash(rawCharSeq.toString(), this.passwordAlgorithm);
    }

    /**
     * Verify password
     * 
     * @param rawCharSeq
     * @param encodedCharSeq
     * @return
     */
    public boolean matches(CharSequence rawCharSeq, String encodedCharSeq) {
        return verifyHash(rawCharSeq.toString(), encodedCharSeq, passwordAlgorithm);
    }
}