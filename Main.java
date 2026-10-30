import java.io.*;
import java.util.Scanner;

/*
 * Runs and trains an N-layer network with backpropagation optimization. Steepest descent training is implemented
 *
 * Table of Contents:
 *
 * public static void main(String[] args)
 * public void saveWeights()
 * public boolean continueTraining()
 * public void setConfigParameters(Parser parser)
 * public void echoConfigParameters(String control_file)
 * public void allocateMemory()
 * public void allocateTrainingMemory()
 * public void populateArray()
 * public void populateInputTable()
 * public void populateTrainingArray()
 * public void setWeight(String userChoice)
 * public void randomize()
 * public void load()
 * public double randomNumber()
 * public void beginTraining()
 * public void trainOnAllTrainingCases()
 * public void loadInputs(int caseNum)
 * public void reportTraining(long trainingTime)
 * public void runOnAllCases()
 * public void reportRunning(long runningTime)
 * public double runForTraining(int caseNumber)
 * public void runForRunning()
 * public double activationFunction(double xVar)
 * public double activationFunctionDeriv(double xVar)
 * public double sigmoid(double xVar)
 * public double sigmoidDeriv(double xVar)
 * public double tanh(double xVar)
 * public double tanhDeriv(double xVar)
 * public double linear(double xVar)
 * public double linearDeriv(double xVar)
 */
public class Main
{
   public int numberOfInputNodes;
   public int numberOfOutputNodes;

   public int numberOfConnectivityLayers;
   public int numberOfActivationLayers;

   public int[] numberOfActivationsInActivationLayer;
   
   public double[][] a;

   public double[][][] weights;

   public double randomLowVal;
   public double randomHighVal;
   public int maxIterations;
   public double errorThreshold;
   public double lambda;

   public int currentIterations;
   public double averageError;

   public String setWeightMethod;

   public double[][] theta;
   public double[][] psi;

   public double[][] inputTable;
   public double[][] truthTable;
   public int numberOfTrainingOrTestCases;
   public double[][] actualOutputs;

   public int keepAlive;

   public boolean train;
   public boolean run;

   public String load_file;
   public String save_file;

   public String test_case_file;
   public String truth_table_file;

   public static final String CONTROL_DEFAULT = "control.txt";

   public static final String RANDOMIZE = "randomize";
   public static final String LOAD = "load";

   public static final int INPUT_LAYER = 0;
   public static final int HIDDEN_LAYER1 = 1;
   public int outputLayer;

/*
 * Main method that sets up network, then trains and/or runs. Provides option to save weights
 */
   public static void main(String[] args)
   {
      Main runCode = new Main();

      String control_file;

      if (args.length == 0)
      {
         control_file = CONTROL_DEFAULT;
      }
      else
      {
         control_file = args[0];
      }

      Parser parser = new Parser(control_file);

      runCode.setConfigParameters(parser);
      runCode.echoConfigParameters(control_file);
      runCode.allocateMemory();
      runCode.populateArray();

      runCode.beginTraining();
      runCode.runOnAllCases();

      runCode.saveWeights();
   } // public static void main(String[] args)

/*
 * Saves weight to binary file if the file name is specified by user
 */
   public void saveWeights()
   {
      if (!save_file.isEmpty())
      {
         try
         {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(save_file));

            int n;

            for (n = INPUT_LAYER; n <= outputLayer; n++)
            {
               outputStream.writeInt(numberOfActivationsInActivationLayer[n]);
            }

            for (n = INPUT_LAYER; n < outputLayer; n++)
            {
               for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
               {
                  for (int j = 0; j < numberOfActivationsInActivationLayer[n + 1]; j++)
                  {
                     outputStream.writeDouble(weights[n][k][j]);
                  }
               }
            } // for (n = INPUT_LAYER; n < outputLayer; n++)

            outputStream.close();
         } // try (writes to file)
         catch (IOException e)
         {
            e.printStackTrace();
         }
      } // if (!save_file.isEmpty())
   } // public void saveWeights()

/*
 * This method determines if training has concluded by checking if current iterations exceed
 * max iterations or if the average error is less than the error threshold
 * @return true if we should continue training
 */
   public boolean continueTraining()
   {
      return currentIterations < maxIterations && averageError >= errorThreshold;
   } // public boolean continueTraining()

/*
 * This is a space to set parameters, such as network configuration, method of setting weights,
 * and other runtime training parameters through the parser.
 */
   public void setConfigParameters(Parser parser)
   {
      numberOfInputNodes = parser.parseInteger();
      int numberOfHiddenLayers = parser.parseInteger();
      numberOfActivationsInActivationLayer = parser.parseArray();
      numberOfOutputNodes = parser.parseInteger();

      numberOfConnectivityLayers = numberOfHiddenLayers + 1;
      numberOfActivationLayers = numberOfConnectivityLayers + 1;
      outputLayer = numberOfActivationLayers - 1;

      numberOfActivationsInActivationLayer[INPUT_LAYER] = numberOfInputNodes;
      numberOfActivationsInActivationLayer[outputLayer] = numberOfOutputNodes;

      keepAlive = parser.parseInteger();

      train = parser.parseBoolean();
      run = parser.parseBoolean();

      numberOfTrainingOrTestCases = parser.parseInteger();

      test_case_file = parser.parseString();
      truth_table_file = parser.parseOptionalString();

      setWeightMethod = parser.parseString();

      load_file = parser.parseOptionalString();
      save_file = parser.parseOptionalString();

      randomLowVal = parser.parseDouble();
      randomHighVal = parser.parseDouble();
      maxIterations = parser.parseInteger();
      errorThreshold = parser.parseDouble();
      lambda = parser.parseDouble();
   } // public void setConfigParameters(Parser parser)

/*
 * This method echoes the parameters (such as network configuration and training parameters if training)
 * back to the user
 */
   public void echoConfigParameters(String control_file)
   {
      System.out.println("Control file used: " + control_file);

      int n;

      System.out.print("Network configuration: ");
      for (n = INPUT_LAYER; n < outputLayer; n++)
      {
         System.out.print(numberOfActivationsInActivationLayer[n] + "-");
      }
      n = outputLayer;
      System.out.println(numberOfActivationsInActivationLayer[n]);

      System.out.println("Training/test case file used: " + test_case_file);

      if (!load_file.isEmpty() && setWeightMethod.equals(LOAD))
      {
         System.out.println("Weights loaded from: " + load_file);
      }
      System.out.print("\n");

      if (train)
      {
         System.out.println("Keep alive iterations: " + keepAlive);
         System.out.println("Truth table file used: " + truth_table_file);
         if (!save_file.isEmpty()) System.out.println("Weights saved to: " + save_file);
         System.out.println("Runtime training parameters: ");
         System.out.println("   Random number range: [" + randomLowVal + ", " + randomHighVal +"]");
         System.out.println("   Max iterations: " + maxIterations);
         System.out.println("   Error threshold: " + errorThreshold);
         System.out.println("   Lambda value: " + lambda + "\n");
      } // if (train)
   } // public void echoConfigParameters(String control_file)

/*
 * This method allocates network arrays including the input, hidden and output layers; weights; and other
 * training computational variables
 */
   public void allocateMemory()
   {
      a = new double[numberOfActivationLayers][];

      int n;

      for (n = INPUT_LAYER; n <= outputLayer; n++)
      {
         a[n] = new double[numberOfActivationsInActivationLayer[n]];
      }

      weights = new double[numberOfConnectivityLayers][][];

      for (n = INPUT_LAYER; n < outputLayer; n++)
      {
         weights[n] = new double[numberOfActivationsInActivationLayer[n]][numberOfActivationsInActivationLayer[n + 1]];
      }

      inputTable = new double[numberOfTrainingOrTestCases][numberOfInputNodes];
      actualOutputs = new double[numberOfTrainingOrTestCases][numberOfOutputNodes];

      if (train) allocateTrainingMemory();
   } // public void allocateMemory()

/*
 *  If training, this method allocates space for the weight changes and the truth table
 */
   public void allocateTrainingMemory()
   {
      theta = new double[numberOfActivationLayers - 1][];

      int n;

      for (n = HIDDEN_LAYER1; n < outputLayer; n++)
      {
         theta[n] = new double[numberOfActivationsInActivationLayer[n]];
      }

      truthTable = new double[numberOfTrainingOrTestCases][numberOfOutputNodes];

      psi = new double[numberOfActivationLayers][];

      for (n = HIDDEN_LAYER1; n <= outputLayer; n++)
      {
         psi[n] = new double[numberOfActivationsInActivationLayer[n]];
      }
   } // public void allocateTrainingMemory()

/*
 * This method populates arrays that are needed before training or running such as the input table
 */
   public void populateArray()
   {
      setWeight(setWeightMethod);
      populateInputTable();

      if (train) populateTrainingArray();
   } // public void populateArray()

/*
 * Populates input table by reading from binary file
 */
   public void populateInputTable()
   {
      try
      {
         DataInputStream inputStream = new DataInputStream(new FileInputStream(test_case_file));

         for (int testCaseNum = 0; testCaseNum < numberOfTrainingOrTestCases; testCaseNum++)
         {
            for (int k = 0; k < numberOfInputNodes; k++)
            {
               inputTable[testCaseNum][k] = inputStream.readDouble();
            }
         }

         inputStream.close();
      } // try (reads from file containing inputs)
      catch (IOException e)
      {
         e.printStackTrace();
      }
   } // public void populateInputTable()

/*
 * If training, this method populates the truth table
 */
   public void populateTrainingArray()
   {
      try
      {
         FileInputStream inputStream = new FileInputStream(truth_table_file);
         Scanner scanner = new Scanner(inputStream);

         for (int testCaseNum = 0; testCaseNum < numberOfTrainingOrTestCases; testCaseNum++)
         {
            for (int i = 0; i < numberOfOutputNodes; i++)
            {
               truthTable[testCaseNum][i] = scanner.nextDouble();
            }
         }

         inputStream.close();
      } // try (reads from truth table file)
      catch (IOException e)
      {
         e.printStackTrace();
      }
   } // public void populateTrainingArray()

/*
 * This method sets the weight depending on the user preference
 * @param userChoice is the user selection for weight setting
 */
   public void setWeight(String userChoice)
   {
      if (userChoice.equals(RANDOMIZE))
         randomize();
      else if (userChoice.equals(LOAD))
         load();
   } // public void setWeight(String userChoice)

/*
 * Randomizes all weights with a random number generator
 */
   public void randomize()
   {
      int n;

      for (n = INPUT_LAYER; n < outputLayer; n++)
      {
         for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
         {
            for (int j = 0; j < numberOfActivationsInActivationLayer[n + 1]; j++)
            {
               weights[n][k][j] = randomNumber();
            }
         }
      } // for (n = INPUT_LAYER; n < outputLayer; n++)
   } // public void randomize()

/*
 * Loads weights from given binary file. If loaded weights do not match network configuration, exception is thrown
 */
   public void load()
   {
      try
      {
         DataInputStream inputStream = new DataInputStream(new FileInputStream(load_file));

         int n, read;
         boolean fileMatches = true;

         for (n = INPUT_LAYER; n <= outputLayer; n++)
         {
            read = inputStream.readInt();

            if (read != numberOfActivationsInActivationLayer[n])
            {
               fileMatches = false;
            }
         } // for (n = INPUT_LAYER; n <= outputLayer; n++)

         if (!fileMatches)
         {
            System.out.println("WARNING: Loaded weights do not match network configuration.");
            throw new IOException();
         }

         for (n = INPUT_LAYER; n < outputLayer; n++)
         {
            for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
            {
               for (int j = 0; j < numberOfActivationsInActivationLayer[n + 1]; j++)
               {
                  weights[n][k][j] = inputStream.readDouble();
               }
            }
         } // for (n = INPUT_LAYER; n < outputLayer; n++)
         inputStream.close();
      } // try (reads from file)
      catch (IOException e)
      {
         e.printStackTrace();
      }
   } // public void load()

/*
 * Generates random number from interval ranging from randomHighVal to randomLowVal
 * @return generated number
 */
   public double randomNumber()
   {
      return Math.random() * (randomHighVal - randomLowVal) + randomLowVal;
   } // public double randomNumber()

/*
 * Sets current iterations to zero and begins training until conditions are met or iterations exceeded the limit
 */
   public void beginTraining()
   {
      if (train)
      {
         currentIterations = 0;

         long start_time = System.currentTimeMillis();

         do
         {
            trainOnAllTrainingCases();

            if ((keepAlive != 0) && ((currentIterations % keepAlive) == 0))
            {
               System.out.printf("Iteration %d, Error = %f\n", currentIterations, averageError);
            }
         }
         while (continueTraining());

         long end_time = System.currentTimeMillis();

         reportTraining(end_time - start_time);
      } // if (train)
   } // public void beginTraining()

/*
 * Trains network on one iteration of the training cases
 * Keeps track of average error on this iteration and the number of iterations already performed
 */
   public void trainOnAllTrainingCases()
   {
      double totalError = 0.0;

      for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)
      {
         loadInputs(caseNumber);

         totalError += runForTraining(caseNumber);

         int n;
         int lastHiddenLayerIndex = outputLayer - 1;

         for (n = lastHiddenLayerIndex; n > HIDDEN_LAYER1; n--)
         {
            for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
            {
               double omega_k = 0.0;

               for (int j = 0; j < numberOfActivationsInActivationLayer[n + 1]; j++)
               {
                  omega_k += psi[n + 1][j] * weights[n][k][j];
                  weights[n][k][j] += lambda * a[n][k] * psi[n + 1][j];
               }

               psi[n][k] = omega_k * activationFunctionDeriv(theta[n][k]);
            } // for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
         } // for (n = lastHiddenLayerIndex; n > HIDDEN_LAYER1; n--)

         int numberOfActivationsInFirstHiddenLayer = numberOfActivationsInActivationLayer[n];

         for (int m = 0; m < numberOfActivationsInFirstHiddenLayer; m++)
         {
            double omega_m = 0.0;

            n = HIDDEN_LAYER1;
            for (int k = 0; k < numberOfActivationsInActivationLayer[n + 1]; k++)
            {
               omega_m += psi[n + 1][k] * weights[n][m][k];
               weights[n][m][k] += lambda * a[n][m] * psi[n + 1][k];
            }

            double psi_m = omega_m * activationFunctionDeriv(theta[n][m]);

            n = INPUT_LAYER;
            for (int x = 0; x < numberOfActivationsInActivationLayer[n]; x++)
            {
               weights[n][x][m] += lambda * a[n][x] * psi_m;
            }
         } // for (int m = 0; m < numberOfActivationsInFirstHiddenLayer; m++)
      } // for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)

      averageError = 0.5 * totalError / (double)numberOfTrainingOrTestCases;
      currentIterations++;
   } // public void trainOnAllTrainingCases()

/*
 * Loads values from input table into input activations
 * @param caseNum is the numbering of the training/test case to load
 */
   public void loadInputs(int caseNum)
   {
      int n = INPUT_LAYER;

      for (int k = 0; k < numberOfActivationsInActivationLayer[n]; k++)
      {
         a[n][k] = inputTable[caseNum][k];
      }
   } // public void loadInputs(int caseNum)

/*
 * Reports training results, including why training has ended and training time
 */
   public void reportTraining(long trainingTime)
   {
      System.out.println("-------------Network trained! End of training-------------");
      System.out.println("Training ended because: ");
      if (currentIterations >= maxIterations) System.out.println("   Iterations exceeded maximum iteration limit.");
      if (averageError <= errorThreshold) System.out.println("   Average error met error threshold.");

      System.out.println("Iterations reached: " + currentIterations);
      System.out.println("Average error reached: " + averageError);
      System.out.println("Training time: " + trainingTime + " milliseconds" + "\n");
   } // public void reportTraining(long trainingTime)

/*
 * Runs the network on all the cases once
 */
   public void runOnAllCases()
   {
      if (run)
      {
         long begin_run = System.currentTimeMillis();

         int n;

         for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)
         {
            loadInputs(caseNumber);

            runForRunning();

            n = outputLayer;
            for (int i = 0; i < numberOfActivationsInActivationLayer[n]; i++)
            {
               actualOutputs[caseNumber][i] = a[n][i];
            }
         } // for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)

         long end_run = System.currentTimeMillis();

         reportRunning(end_run - begin_run);
      } // if (run)
   } // public void runOnAllCases()

/*
 * Reports running results and prints the final values in the output layer
 */
   public void reportRunning(long runningTime)
   {
      System.out.println("-------------Network ran! End of running-------------");
      System.out.println("Running time: " + runningTime + " milliseconds");

      System.out.println("Running results: ");

      for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)
      {
         for (int k = 0; k < numberOfInputNodes; k++)
         {
            System.out.print(inputTable[caseNumber][k] + "  ");
         }

         System.out.print("|  ");

         for (int i = 0; i < numberOfOutputNodes; i++)
         {
            System.out.print(String.format("%.4f", actualOutputs[caseNumber][i]) + "   ");
         }

         System.out.print("\n");
      } // for (int caseNumber = 0; caseNumber < numberOfTrainingOrTestCases; caseNumber++)
   } // public void reportRunning(long runningTime)

/*
 * Runs the network once. Called for training. Returns twice the error
 * @param caseNumber is the training case we are running on
 */
   public double runForTraining(int caseNumber)
   {
      for (int n = HIDDEN_LAYER1; n < outputLayer; n++)
      {
         for (int j = 0; j < numberOfActivationsInActivationLayer[n]; j++)
         {
            theta[n][j] = 0.0;

            for (int k = 0; k < numberOfActivationsInActivationLayer[n - 1]; k++)
            {
               theta[n][j] += a[n - 1][k] * weights[n - 1][k][j];
            }

            a[n][j] = activationFunction(theta[n][j]);
         } // for (int j = 0; j < numberOfActivationsInActivationLayer[n]; j++)
      } // for (int n = HIDDEN_LAYER1; n < outputLayer; n++)

      double twiceError = 0.0;

      int n = outputLayer;
      for (int i = 0; i < numberOfActivationsInActivationLayer[n]; i++)
      {
         double theta_i = 0.0;

         for (int j = 0; j < numberOfActivationsInActivationLayer[n - 1]; j++)
         {
            theta_i += a[n - 1][j] * weights[n - 1][j][i];
         }

         a[n][i] = activationFunction(theta_i);

         double omega_i = truthTable[caseNumber][i] - a[n][i];
         psi[n][i] = omega_i * activationFunctionDeriv(theta_i);
         twiceError += omega_i * omega_i;
      } // for (int i = 0; i < numberOfActivationsInActivationLayer[n]; i++)

      return twiceError;
   } // public double runForTraining(int caseNumber)

/*
 * Runs the network once. Called for only running.
 */
   public void runForRunning()
   {
      for (int n = HIDDEN_LAYER1; n <= outputLayer; n++)
      {
         for (int j = 0; j < numberOfActivationsInActivationLayer[n]; j++)
         {
            double theta = 0.0;

            for (int k = 0; k < numberOfActivationsInActivationLayer[n - 1]; k++)
            {
               theta += a[n - 1][k] * weights[n - 1][k][j];
            }

            a[n][j] = activationFunction(theta);
         } // for (int j = 0; j < numberOfActivationsInActivationLayer[n]; j++)
      } // for (int n = HIDDEN_LAYER1; n <= outputLayer; n++)
   } // public void runForRunning()

/*
 * Returns the output after applying the threshold function, f(x)
 * @param xVar is the x in f(x)
 */
   public double activationFunction(double xVar)
   {
      return sigmoid(xVar);      // options: sigmoid, linear, tanh
   }

/*
 * Returns the derivative of the threshold function
 * @param xVar is the x in f'(x)
 */
   public double activationFunctionDeriv(double xVar)
   {
      return sigmoidDeriv(xVar); // options: sigmoidDeriv, linearDeriv, tanhDeriv
   }

/*
 * Returns value of sigmoid threshold function
 * @param xVar is the x in f(x)
 */
   public double sigmoid(double xVar)
   {
      return 1.0 / (1.0 + Math.exp(-xVar));
   }

/*
 * Returns value of sigmoid threshold function's derivative
 * @param xVar is the x in f'(x)
 */
   public double sigmoidDeriv(double xVar)
   {
      double f_val = sigmoid(xVar);
      return f_val * (1.0 - f_val);
   }

/*
 * Returns value of hyperbolic tangent threshold function
 * @param xVar is the x in f(x)
 */
   public double tanh(double xVar)
   {
      double e_x = (xVar < 0.0) ? 1.0 : -1.0;
      double e_epsilon = Math.exp(e_x * 2.0 * xVar);
      return e_x * (e_epsilon - 1.0) / (e_epsilon + 1.0);
   }

/*
 * Returns value of hyperbolic tangent derivative
 * @param xVar is the x in f(x)
 */
   public double tanhDeriv(double xVar)
   {
      double tanh = tanh(xVar);
      return 1.0 - tanh * tanh;
   }

/*
 * Returns value of linear threshold function
 * @param xVar is the x in f(x)
 */
   public double linear(double xVar)
   {
      return xVar;
   }

/*
 * Returns value of linear threshold function's derivative
 * @param xVar is the x in f'(x)
 */
   public double linearDeriv(double xVar)
   {
      return 1.0;
   }
} // public class Main