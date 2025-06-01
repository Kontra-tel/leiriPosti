package tel.kontra.leiriposti.util;

/**
 * Enum for hash algorithms
 * 
 * @version 1.0
 */
public enum HashAlgorithm {
    
    // Sha algorithms
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512"),

    // MD algorithms
    MD5("MD5"); // Not secure but for identification purposes only

    // Stupid SHA_ breaks the hashing function

    private final String algorithm;

    /**
     * Constructor
     * 
     * @param algorithm hash algorithm
     */
    private HashAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    /**
     * Get algorithm
     * 
     * @return String
     */
    public String getAlgorithm() {
        return algorithm;
    }
}