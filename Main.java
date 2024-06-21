
import java.util.*;
import java.io.*;

public class Main {

    public static int cache_size = 0;
    public static int block_size = 0, associativity = 0;
    public static int number_of_set = 0, tagoffset = 0, block_offset = 0, set_offset = 0;
    public static String input_file;
    public static int total_Misses = 0;
    public static int total_Hits = 0;
    public static ArrayList<ArrayList<Structure>> cache = new ArrayList<ArrayList<Structure>>();
    public static ArrayList<Features> Set = new ArrayList<Features>();

    Main() {
        total_Misses = 0;
        total_Hits = 0;
        number_of_set = 0;
        tagoffset = 0;
        block_offset = 0;
        set_offset = 0;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 4) {

            Main.cache_size = (int) (1024 * (Float.parseFloat(args[0])));
            try {               
                Main.associativity = Integer.parseInt(args[1]);
                Main.block_size = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("Input a valid integer for block and associativity");
                System.exit(0);
            }
            Main.input_file = (args[3]);
            calculate(Main.cache_size, Main.block_size, Main.associativity);

            // System.out.println("number of sets " + Main.number_of_set + ".");
            // System.out.println("block_offset " + Main.block_offset + ".");
            // System.out.println("set_offset " + Main.set_offset + ".");
            // System.out.println("tagoffset " + Main.tagoffset + ".");
            try {
            File file = new File(input_file);
            BufferedReader br = new BufferedReader(new FileReader(file));

            initialise();
            String input_hex;
            while ((input_hex = br.readLine()) != null) {

                long num = Long.parseLong(input_hex, 16);
                String input = Long.toBinaryString(num);
                int number = 32 - input.length();
                for (int k = 0; k < number; k++) {
                    input = "0" + input;
                }

                String s1 = input.substring(0, Main.tagoffset);
                String s2 = input.substring(Main.tagoffset, Main.tagoffset + Main.set_offset);
                String s3 = input.substring(Main.tagoffset + Main.set_offset, 32);
                int position ;
                try {
                position = Integer.parseInt(s2, 2);
                }
                catch (NumberFormatException e){
                    position = 0;
                }
                int block_no = Integer.parseInt(s3, 2);
                Structure filled = new Structure(position, s1, block_no);
                int j;
                int count = 0;
                int pos = -1;
                for (j = 0; j < Main.associativity; j++) {
                    if ((Main.cache.get(position).get(j).tagvalue).equals(s1)) {
                        total_Hits++;
                        pos = j;
                        int y = Main.Set.get(position).setHits;
                        int a = Main.Set.get(position).setMiss;
                        ArrayList<Integer> b = Set.get(position).LRU;
                        Features update = new Features(y, a, b);
                        update.setHits = y + 1;
                        Main.Set.set(position, update);
                        Main.Set.get(position).LRU.set(j, 0);
                        break;
                    } else if ((Main.cache.get(position).get(j).tagvalue).equals("null")) {
                        count++;
                    }
                }

                for (int i = 0; i < Main.associativity; i++) {
                    if (i != pos) {
                        int x = Main.Set.get(position).LRU.get(i);
                        Main.Set.get(position).LRU.set(i, x + 1);
                    }
                }
                if (j == Main.associativity && count > 0) {
                    Main.total_Misses++;
                    int y = Set.get(position).setHits;
                    int a = Set.get(position).setMiss;
                    ArrayList<Integer> b = Main.Set.get(position).LRU;
                    Features update = new Features(y, a, b);
                    update.setMiss = a + 1;
                    Main.Set.set(position, update);
                    for (int i = 0; i < Main.associativity; i++) {
                        if ((Main.cache.get(position).get(i).tagvalue).equals("null")) {
                            Structure insert = new Structure(position, s1, block_no);
                            Main.cache.get(position).set(i, insert);
                            Main.Set.get(position).LRU.set(i, 0);
                            break;
                        }
                    }
                } else if (j == Main.associativity) {
                    int max1 = 0;
                    int position_to_change = 0;
                    Main.total_Misses++;
                    int y = Set.get(position).setHits;
                    int a = Set.get(position).setMiss;
                    ArrayList<Integer> b = Main.Set.get(position).LRU;
                    Features update = new Features(y, a, b);
                    update.setMiss = a + 1;
                    Main.Set.set(position, update);
                    for (int i = 0; i < Main.associativity; i++) {
                        int x = Main.Set.get(position).LRU.get(i);
                        if (x > max1) {
                            max1 = x;
                            position_to_change = i;
                        }

                    }

                    Main.cache.get(position).set(position_to_change, filled);
                    Main.Set.get(position).LRU.set(position_to_change, 0);

                }

            }
            br.close();

        } 
        catch( FileNotFoundException e)
        {
            System.out.println("Inavlid file name");
            System.exit(0);
            
        }
    }
        else {
            System.out.println("invalid input");
        }
        System.out.println("Total Hits: " + Main.total_Hits);
        System.out.println("Total Misses: " + Main.total_Misses);
        System.out.println("SET Number SET Hits  SET Misses");
        int k =0;
        for (Features i : Set) {
            System.out.println(k + "\t" +i.setHits + "\t" + i.setMiss);
            k++;

        }
    
}

    public static void calculate(int cache_size, int block_size, int associativity) {
        try {

            Main.number_of_set = (cache_size) / (block_size * associativity);
            Main.block_offset = (int) (Math.log10(block_size) / Math.log10(2));
            Main.set_offset = (int) (Math.log10(Main.number_of_set) / Math.log10(2));
            Main.tagoffset = 32 - (Main.block_offset + Main.set_offset);

        } catch (ArithmeticException e) {
            System.out.println("Invalid input : division by zero");
            System.exit(0);
        }

    }

    public static void initialise() {
        for (int i = 0; i < Main.number_of_set; i++) {
            ArrayList<Structure> line = new ArrayList<Structure>();

            for (int j = 0; j < Main.associativity; j++) {
                Structure def = new Structure(0, "null", 0);
                line.add(def);

            }
            Main.cache.add(line);

            Features f1 = new Features();
            Main.Set.add(i, f1);

        }

    }
    

}

class Structure {

    int set_value;
    String tagvalue;
    int block_offset;

    Structure(int set_value, String tagvalue, int block_offset) {
        this.set_value = set_value;
        this.tagvalue = tagvalue;
        this.block_offset = block_offset;

    }

    Structure() {
        this.set_value = 0;
        this.tagvalue = "null";
        this.block_offset = 0;

    }

    public String toString() {

        return this.tagvalue + " " + this.set_value + " " + this.block_offset;
    }
}

class Features {

    int setHits;
    int setMiss;
    public ArrayList<Integer> LRU = new ArrayList<Integer>();
    int value = 0;

    Features() {
        this.setHits = 0;
        this.setMiss = 0;
        for (int k = 0; k < Main.associativity; k++) {
            LRU.add(value);
        }
    }

    Features(int setHits, int setMiss, ArrayList<Integer> LRU_old) {
        this.setHits = setHits;
        this.setMiss = setMiss;
        for (int k = 0; k < Main.associativity; k++) {
            int x = LRU_old.get(k);
            this.LRU.add(x);
        }
    }

}