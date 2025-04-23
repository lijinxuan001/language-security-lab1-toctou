package backEnd;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.ReentrantLock;

public class Wallet {
    /**
     * The RandomAccessFile of the wallet file
     */  
    private final RandomAccessFile file;
    private final FileChannel channel;
    private final ReentrantLock lock = new ReentrantLock(); 

    /**
     * Creates a Wallet object
     *
     * A Wallet object interfaces with the wallet RandomAccessFile
     */
    public Wallet () throws Exception {
        this.file = new RandomAccessFile(new File("backEnd/wallet.txt"), "rw");
        this.channel = file.getChannel();
    }

    /**
     * Gets the wallet balance (thread-safe + shared file lock)
     *
     * @return The content of the wallet file as an integer
     */
    public int getBalance() throws IOException {
        lock.lock();
        FileLock fileLock = null;
        try {
            fileLock = channel.lock(0L, Long.MAX_VALUE, true); // Shared lock for reading
            this.file.seek(0L);
            String line = this.file.readLine();
            return (line == null || line.trim().isEmpty()) ? 0 : Integer.parseInt(line.trim());
        } finally {
            if (fileLock != null) fileLock.release();
            lock.unlock();
        }
    }

    /**
     * Sets a new balance in the wallet (thread-safe + exclusive file lock)
     *
     * @param newBalance New balance to write in the wallet
     */
    public void setBalance(int newBalance) throws Exception {
        lock.lock();
        FileLock fileLock = null;
        try {
            fileLock = channel.lock(); // Exclusive lock
            this.file.seek(0L);
            this.file.setLength(0);
            String str = Integer.toString(newBalance) + '\n'; 
            this.file.writeBytes(str);
        } finally {
            if (fileLock != null) fileLock.release();
            lock.unlock();
        }
    }

    /**
     * Atomically withdraws money from the wallet (thread-safe + exclusive file lock)
     *
     * @param valueToWithdraw amount to withdraw
     * @return true if successful, false if insufficient funds
     */
    public boolean safeWithdraw(int valueToWithdraw) throws Exception {
        lock.lock();
        FileLock fileLock = null;
        try {
            fileLock = channel.lock(); // Exclusive file lock
            this.file.seek(0L);
            String line = this.file.readLine();
            int currentBalance = (line == null || line.trim().isEmpty()) ? 0 : Integer.parseInt(line.trim());
            if (currentBalance < valueToWithdraw) {
                return false;
            } else {
                this.file.setLength(0L);
                this.file.seek(0L);
                this.file.writeBytes((currentBalance - valueToWithdraw) + "\n");
                return true;
            }
        } finally {
            if (fileLock != null) fileLock.release();
            lock.unlock();
        }
    }

    /**
     * Closes the RandomAccessFile in this.file
     */
    public void close() throws Exception {
        this.file.close();
    }
}
