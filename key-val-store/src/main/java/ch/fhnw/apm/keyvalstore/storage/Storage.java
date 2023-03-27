package ch.fhnw.apm.keyvalstore.storage;

public interface Storage {

    /***
     * Stores the value in the Cluster.
     *
     * @param key The key slot in which the value is stored
     * @param value The value to store. Use <code>null</code> to remove a
     * value from the store.
     * @return <code>true</code> if the value has been successfully stored
     */
    public boolean store(int key, String value);

    /***
     * Loads the value from the cluster.
     *
     * @param key The key to be retrieved
     * @return The value requested or <code>null</code> if it does not exist
     */
    public String load(int key);

}
