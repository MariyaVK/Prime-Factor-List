package edu.iastate.cs228.hw3;

/**
 * 
 * @author Mariya Karasseva mariyak
 *
 */
public class PrimeFactorizationTester {

	public static void main(String[] args) {
		PrimeFactorization pf1 = new PrimeFactorization(5814);
		System.out.println(pf1.toString());
		PrimeFactorization pf2 = new PrimeFactorization(564);
		// System.out.println(pf1.gcd(pf2).toString());
		// System.out.println(pf1.gcd(pf2).value());
		// System.out.println(pf2.toString());
		// System.out.println(pf1.lcm(pf2).toString());
		// System.out.println(pf1.lcm(pf2).value());
		// System.out.println(pf1.containsPrimeFactor(3));
		PrimeFactor[] pf4 = { new PrimeFactor(2, 3), new PrimeFactor(3, 4) };
		PrimeFactorization pf3 = new PrimeFactorization(pf4);
		PrimeFactorization pf5 = new PrimeFactorization(pf3);
		System.out.println(pf3.toString());
		System.out.println(pf3.lcm(pf3,pf2));
	}

}
