# TOCTOU Lab Report
## Isak Nordin && Jinxuan Li
## Part 0

### **Code for ShoppingCart.java**
```java
import backEnd.*;
import java.util.Scanner;

public class ShoppingCart {
    private static void print(Wallet wallet, Pocket pocket) throws Exception {
        System.out.println("Your current balance is: " + wallet.getBalance() + " credits.");
        System.out.println(Store.asString());
        System.out.println("Your current pocket is:\n" + pocket.getPocket());
    }

    private static String scan(Scanner scanner) throws Exception {
        System.out.print("What do you want to buy? (type quit to stop) ");
        return scanner.nextLine();
    }

    public static void main(String[] args) throws Exception {
        Wallet wallet = new Wallet();
        Pocket pocket = new Pocket();
        Scanner scanner = new Scanner(System.in);

        print(wallet, pocket);
        String product = scan(scanner);

        while(!product.equals("quit")) {
            int price = Store.getProductPrice(product); //Gets the price of the requested product
            if (price == -1) {
                System.out.println("Product not found.");
        }
        else if(wallet.safeWithdraw(price)){ //Product was not too expensive
            pocket.addProduct(product); //Adds product to pocket
            System.out.println("You bought " + product ); //Prints success message
        }else{
            System.out.println("Insufficient balance for " + product + "."); //Prints error message
        }  
            // Just to print everything again...
            print(wallet, pocket);
            product = scan(scanner);
        }
    }
}
```
To compile the code, run a command line from the directory where the code is located, and then write make all to compile and java ShoppingCart to run.


## Part 1

### **Shared Resource**
The shared resource in this system is `wallet.txt`, which is managed by the `Wallet` class. It stores the user's balance.

### **Who Is Sharing It?**
The main thread of the program (`ShoppingCart.java`) uses this file to read the balance and perform withdrawal operations.  
However, when multiple instances of the frontend are running simultaneously, they all interact with the same `wallet.txt` file, effectively sharing access to this resource during the purchasing process.

---

### **Root Cause of the Problem**
The key issue lies in the separation of two operations:

- **Checking** the balance: `wallet.getBalance()`
- **Modifying** the balance (deduction): `wallet.setBalance(...)`

These two actions are performed separately. The time gap between them creates a **race condition window**.  
If another thread or process modifies the wallet during this window, the balance check becomes outdated, potentially allowing unauthorized purchases.

---

###  How to Exploit the System (TOCTOU Attack)

This vulnerability can be exploited by simulating a TOCTOU attack using multiple concurrent frontend instances (here we use two instances):

#### Step 1: Thread T1
Start the program: `java ShoppingCart`. Choose a product, e.g., **Car**, which costs `30000`, equal to the current wallet balance. Pause the execution before the program deducts the balance (using a debugger)
![alt text](image-3.png)
![alt text](image-2.png)
#### Step 2: Thread T2
Start another instance of java ShoppingCart. Choose a cheaper product (≤ 30000). Since T1 hasn’t updated the wallet yet, the balance is still seen as 30000. T2 successfully purchases the item and modifies the wallet.
![alt text](image-1.png)
#### Step 3: Resume T1
T1 continues and completes the withdrawal of 30000. The system has now overdrawn the wallet.
![alt text](image-4.png) 

## Part 2: Fix the API
### Write a Wallet method implementing the necessary protections
```java
public boolean safeWithdraw(int valueToWithdraw) throws Exception {
    // ReentrantLock lock = new ReentrantLock();    
    lock.lock();
    try {
        this.file.seek(0L);
        int currentBalance = Integer.parseInt(this.file.readLine());
        if (currentBalance < valueToWithdraw) {
            return false;
        }else {
            this.file.setLength(0L);
            this.file.writeBytes((currentBalance-valueToWithdraw)+"\n");
            return true;
        }
    } finally {
        lock.unlock();
    }
    }
```

Other APIs may also suffer from race conditions include the `Pocket.class`, which writes purchased products to a shared file `pocket.txt`. Without synchronization, concurrent processes may cause corrupted or lost writes. We can solve this by writing a safeassProduct method implementing the necessary protections with ReentrantLock. 
```java
    public void safeaddProduct(String product) throws Exception {
        lock.lock();
        try {
        this.file.seek(this.file.length());
        this.file.writeBytes(product+'\n');
        } finally {
            lock.unlock();
        }
    }
```
While, the `Store.class` only serves as a read-only source of static product data, and thus does not require protection. 

We minimal the loc scope within critical sections.We don't lock read-only data. And locks are held for the shortest time possible, reducing contention windows. 