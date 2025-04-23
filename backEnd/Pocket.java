package backEnd;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;


public class Pocket {
    /**
     * The RandomAccessFile of the pocket file
     */
    private RandomAccessFile file;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Creates a Pocket object
     * 
     * A Pocket object interfaces with the pocket RandomAccessFile.
     */
    public Pocket () throws Exception {
        this.file = new RandomAccessFile(new File("backEnd/pocket.txt"), "rw");
    }

    /**
     * Adds a product to the pocket. 
     *
     * @param  product           product name to add to the pocket (e.g. "car")
     */
    public void addProduct(String product) throws Exception {
        this.file.seek(this.file.length());
        this.file.writeBytes(product+'\n'); 
    }
    public void safeaddProduct(String product) throws Exception {
        lock.lock();
        try {
        this.file.seek(this.file.length());
        this.file.writeBytes(product+'\n');
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generates a string representation of the pocket
     *
     * @return a string representing the pocket
     */
    public String getPocket() throws Exception {
        StringBuilder sb = new StringBuilder();
        this.file.seek(0);
        String line;
        while((line = this.file.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }

        return sb.toString();
    }
    
    /**
     * Closes the RandomAccessFile in this.file
     */
    public void close() throws Exception {
        this.file.close();
    }
}
