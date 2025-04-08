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
