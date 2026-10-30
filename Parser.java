import java.util.Scanner;
import java.io.*;

/*
 * Parser class with scanner to parse configuration control file
 *
 * Table of Contents:
 *
 * public Parser(String configFile)
 * public int parseInteger()
 * public double parseDouble()
 * public String parseString()
 * public boolean parseBoolean()
 * public int[] parseArray()
 * public String parseOptionalString()
 */
public class Parser
{
   Scanner scanner;

/*
 * Creates scanner and input file to read from
 */
   public Parser(String configFile)
   {
      try
      {
         FileInputStream inStream = new FileInputStream(configFile);
         scanner = new Scanner(inStream);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   } // public Parser(String configFile)

/*
 * Eats variable, =, then returns integer
 */
   public int parseInteger()
   {
      scanner.next();
      scanner.next();
      return(scanner.nextInt());
   }

/*
 * Eats variable, =, then returns double
 */
   public double parseDouble()
   {
      scanner.next();
      scanner.next();
      return(scanner.nextDouble());
   }

/*
 * Eats variable, =, then returns string
 */
   public String parseString()
   {
      scanner.next();
      scanner.next();
      return(scanner.next());
   }

/*
 * Eats variable, =, then returns boolean
 */
   public boolean parseBoolean()
   {
      scanner.next();
      scanner.next();
      return(scanner.nextBoolean());
   }

/*
 * Parses activations in hidden nodes as array
 */
   public int[] parseArray()
   {
      scanner.next();
      scanner.next();

      String s = (scanner.nextLine()).substring(1);

      String[] stringActivNodes = s.split(" ");
      int lenIndexOne = stringActivNodes.length + 2;
      int[] intActiveNodes = new int[lenIndexOne];

      intActiveNodes[0] = 0;
      for (int index = 1; index < intActiveNodes.length - 1; index++)
      {
         intActiveNodes[index] = Integer.parseInt(stringActivNodes[index - 1]);
      }

      intActiveNodes[lenIndexOne - 1] = 0;

      return intActiveNodes;
   } // public int[] parseArray()

/*
 * If 0 is scanned, scanner eats variable, =, then returns empty string
 * Otherwise, scanner eats variable, = then returns string
 */
   public String parseOptionalString()
   {
      String val = "";
      if (!scanner.next().equals("0"))
      {
         scanner.next();
         val = scanner.next();
      }
      else
      {
         scanner.next();
         scanner.next();
      }
      return val;
   } // public String parseOptionalString()
} // public class Parser