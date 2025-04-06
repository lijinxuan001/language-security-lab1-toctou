import backEnd.*;
import backEnd.Store;
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
            if(wallet.getBalance() < Store.getProductPrice(product)){ //Checks if the balance is smaller than the price of the requested product
                return; //Quits if that is the case
            }
        else { //Product was not too expensive
            wallet.setBalance(wallet.getBalance()-Store.getProductPrice(product)); // Removes the product price from the balance
            pocket.addProduct(product); //Adds product to pocket
            System.out.println("Your new balance is: " + wallet.getBalance() + " credits."); //Prints new balance
        }

            // Just to print everything again...
            print(wallet, pocket);
            product = scan(scanner);
        }
    }
}
