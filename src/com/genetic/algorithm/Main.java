package com.genetic.algorithm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * At the beginning of a run of a genetic algorithm a large population of random chromosomes is created.
 * Each one, when decoded will represent a different solution to the problem at hand.
 * Let's say there are N chromosomes in the initial population.
 * Then, the following steps are repeated until a solution is found:
 *
 * 1 :
 * Test each chromosome to see how good it is at solving the problem at hand and assign a fitness score accordingly.
 * The fitness score is a measure of how good that chromosome is at solving the problem to hand.
 *
 * 2:
 * Select two members from the current population.
 * The chance of being selected is proportional to the chromosomes fitness.
 * Roulette wheel selection is a commonly used method.
 *
 * 3:
 * Dependent on the crossover rate crossover the bits from each chosen chromosome at a randomly chosen point.
 *
 * 4:
 * Step through the chosen chromosomes bits and flip dependent on the mutation rate.
 *
 *
 * Repeat step 2, 3, 4 until a new population of N members has been created.
 * I'm going to start with obsfucated, procedural codes and put everything in a single file.
 */

public class Main {

  private static final Map<String, String> encoding = new HashMap<String, String>();

  private static final double BIT_STRING_MUTATION_RATE = 0.02;

  private static final double SINGLE_POINT_CROSSOVER_RATE = 0.7;
  private static final double TWO_POINT_CROSSOVER_RATE = 0.6;
  private static final double UNIFORM_CROSSOVER_RATE = 0.5;

  private static final int NUMBER_OF_POPULATIONS = 500;

  private static int SEARCHED_NUMBER;
  private static int RADIX = 10;

  private static final DecimalFormat decimalFormat = new DecimalFormat("#.00000");

  /*
  We do not care about the calculation method.
  E.g. 2 + 3 * 4 equals 20, not 14.
   */

  public static void main(String[] args) {
    System.out.println("Enter number from 1 to 100: ");
    Scanner scanner = new Scanner(System.in);
    SEARCHED_NUMBER = scanner.nextInt();

    //might want to use radix, but whatever.
    //number encoding
    encoding.put("0000", "0");
    encoding.put("0001", "1");
    encoding.put("0010", "2");
    encoding.put("0011", "3");
    encoding.put("0100", "4");
    encoding.put("0101", "5");
    encoding.put("0110", "6");
    encoding.put("0111", "7");
    encoding.put("1000", "8");
    encoding.put("1001", "9");

    //operator encoding
    encoding.put("1010", "+");
    encoding.put("1011", "-");
    encoding.put("1100", "/");
    encoding.put("1101", "x");

    //randomize population

    SingleChromosome[] initialRandomPopulation = new SingleChromosome[NUMBER_OF_POPULATIONS];
    SingleChromosome[] newPopulation = new SingleChromosome[NUMBER_OF_POPULATIONS];
    Random randBit = new Random();
    while(true) {
      double[] selectionProbability = new double[NUMBER_OF_POPULATIONS];
      double[] populationFitness = new double[NUMBER_OF_POPULATIONS];
      double sumOfFitness = 0.0;
      boolean flagFound = false;

      for (int i = 0; i < NUMBER_OF_POPULATIONS; i++) {

        //use stupid random per bit 32 times instead of randomizing 32 bit shits at once.
        char[] charArray = new char[32];
        for (int j = 0; j < 32; j++) {
          charArray[j] = Character.forDigit(randBit.nextInt(2), RADIX);
        }

        initialRandomPopulation[i] = new SingleChromosome();
        initialRandomPopulation[i].setBitString(charArray);
        System.out.println(new String(initialRandomPopulation[i].getBitString()));

        double fitness = determineFitness(initialRandomPopulation[i]);
        if (fitness == -1) {
          System.out.println();
          System.out.println("Stopping GA...");
          System.out.println("Solution found at population number " + i + "...");
          System.out.println("Chromosome: " + new String(initialRandomPopulation[i].getBitString()));
          System.out.println("Decoded: " + Arrays.toString(decodeChromosome(initialRandomPopulation[i]).toArray()));
          System.out.println();
          flagFound = true;
          break;
        } else {
          populationFitness[i] = fitness;
        }

        sumOfFitness = Double.parseDouble(decimalFormat.format(sumOfFitness + populationFitness[i]));
      }

      if(flagFound)
        break;

      //determine selection probability using uniform distribution
      //will use roulette wheel selection.
      System.out.println(Arrays.toString(populationFitness));
      System.out.println(sumOfFitness);
      System.out.println();

      double sumOfProbability = 0.0;
      double temporarySum = 0.0;
      for(int i=0; i<NUMBER_OF_POPULATIONS; i++) {
        temporarySum = temporarySum + populationFitness[i];
        double norm = Double.parseDouble(decimalFormat.format(temporarySum / sumOfFitness));
        selectionProbability[i] = Double.parseDouble(String.valueOf(norm));
      }

      System.out.println(Arrays.toString(selectionProbability));

      int popCounter = 0;
      while (popCounter < NUMBER_OF_POPULATIONS) {
        SingleChromosome[] parents = new SingleChromosome[2];
        Random generator = new Random();

        double firstRandom = 0.0;
        double secondRandom = 0.0;
        for (int x = 0; x < 2; x++) {
          double random = Double.parseDouble(decimalFormat.format(Math.random()));
          if(x==0) firstRandom = random;
          if(x==1) secondRandom = random;
          for (int i = 0; i < NUMBER_OF_POPULATIONS-1; i++) {
            if (random == 0.0 || random < selectionProbability[0]) {
              parents[x] = initialRandomPopulation[0];
              if(parents[x] == null)
                System.out.println("NULL");
            }
            else if (random >= selectionProbability[i] && random <= selectionProbability[i + 1]) {
              parents[x] = initialRandomPopulation[i];
              if(parents[x] == null)
                System.out.println("NULL");
            }
          }
        }

        newPopulation[popCounter] = createOffspring(parents);
        System.out.println(new String(newPopulation[popCounter].getBitString()) + " -- " + popCounter);
        popCounter++;
      }

      newPopulation = mutatePopulation(newPopulation);

      initialRandomPopulation = newPopulation;
    }

  }

  private static SingleChromosome[] mutatePopulation(SingleChromosome[] newPopulation) {
    Random rand = new Random();
    SingleChromosome[] mutatedPopulation = new SingleChromosome[NUMBER_OF_POPULATIONS];
    for(int i=0; i<newPopulation.length; i++) {
      char[] bitString = newPopulation[i].getBitString();
      if(rand.nextDouble() < BIT_STRING_MUTATION_RATE) {
        //mutation!
        System.out.print("Mutation: " + Arrays.toString(bitString));
        for(int j=0; j<bitString.length; j++) {
          int bit = Integer.parseInt("" + bitString[j]);
          if(rand.nextDouble() < 0.5) {
            bit = bit ^ 1;
            bitString[j] = Character.forDigit(bit, RADIX);
          }
        }
        char[] afterBit = bitString;
        System.out.println(" to " + Arrays.toString(bitString));
      }
      mutatedPopulation[i] = new SingleChromosome();
      mutatedPopulation[i].setBitString(bitString);
    }
    return mutatedPopulation;
  }

  private static SingleChromosome createOffspring(SingleChromosome[] parents) {
    Random rand = new Random();
    
    char[] firstParentBit = parents[0].getBitString();
    char[] secondParentBit = parents[1].getBitString();

    if(rand.nextDouble() < SINGLE_POINT_CROSSOVER_RATE) {
      //do single point crossover.
      int pointer = rand.nextInt(32);

      char[] firstOffspringBit = new char[32];
      char[] secondOffspringBit = new char[32];

      for(int i=0; i<32; i++) {
        if(i<pointer) {
          firstOffspringBit[i] = firstParentBit[i];
          secondOffspringBit[i] = secondParentBit[i];
        } else {
          firstOffspringBit[i] = secondParentBit[i];
          secondOffspringBit[i] = firstParentBit[i];
        }
      }

      SingleChromosome offspring = new SingleChromosome();

      if(rand.nextDouble() < 0.5)
        offspring.setBitString(firstOffspringBit);
      else
        offspring.setBitString(secondOffspringBit);

      return offspring;
    } else {
      //just copy directly from one of the parents.
      if(rand.nextDouble() < 0.5)
        return parents[0];
      else
        return parents[1];
    }
  }


  private static double determineFitness(SingleChromosome singleChromosome) {
    List<String> decodedChromosome = decodeChromosome(singleChromosome);
    int decodedResult = countDecodedChromosome(decodedChromosome);

    int denom = Math.abs(SEARCHED_NUMBER - decodedResult);
    if(denom == 0) {
      //found?
      return -1;
    } else {
      return Double.parseDouble(decimalFormat.format((double) 1 / denom));
    }
  }

  private static List<String> decodeChromosome(SingleChromosome singleChromosome) {
    char[] chromosomeBit = singleChromosome.getBitString();
    List<String> decodedString = new ArrayList<String>();

    String sumString = "";
    for(int i=0; i<=32; i++) {
      if(i==0 || i % 4 != 0) {
        sumString = sumString + chromosomeBit[i];
      } else {
        if(!sumString.equals("1110") && !sumString.equals("1111")) {
          decodedString.add(encoding.get(sumString));
        }
        if (i < 32)
          sumString = "" + chromosomeBit[i];
      }
    }

    return decodedString;
  }

  private static int countDecodedChromosome(List<String> decodedString) {
    String currentOperator = "";
    String firstNumber = "";
    String secondNumber = "";
    int result = 0;
    for(String oneCharacter : decodedString) {
      if(oneCharacter.equals("+") || oneCharacter.equals("-") || oneCharacter.equals("/") || oneCharacter.equals("x")) {
        currentOperator = oneCharacter;
      } else {
        if(firstNumber.equals("") || !firstNumber.equals("") && currentOperator.equals("")) {
          firstNumber = oneCharacter;
          if(!currentOperator.equals(""))
            currentOperator = "";
        } else {
          if(!currentOperator.equals("")) {
            secondNumber = oneCharacter;
            result = getResult(firstNumber, secondNumber, currentOperator);
            firstNumber = Integer.toString(result);
            currentOperator = "";
          }
        }
      }
    }

    return result;
  }

  private static int getResult(String firstNumber, String secondNumber, String currentOperator) {
    int firstNumberValue = Integer.valueOf(firstNumber);
    int secondNumberValue = Integer.valueOf(secondNumber);

    if(currentOperator.equals("+")) {
      return firstNumberValue + secondNumberValue;
    } else if(currentOperator.equals("-")) {
      return firstNumberValue - secondNumberValue;
    } else if(currentOperator.equals("/")) {
      //if there is division by zero, we return 0 anyway.
      if(secondNumberValue == 0)
        return 0;
      return firstNumberValue / secondNumberValue;
    } else {
      return firstNumberValue * secondNumberValue;
    }
  }

  private static class SingleChromosome {
    private char[] bitString;

    public SingleChromosome() {
      bitString = new char[32];
    }

    public char[] getBitString() {
      return bitString;
    }

    public void setBitString(char[] bitString) {
      this.bitString = bitString;
    }
  }
}
