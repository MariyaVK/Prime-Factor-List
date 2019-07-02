package edu.iastate.cs228.hw3;

/**
 *  
 * @author Mariya Karasseva mariyak
 *
 */

import java.util.ListIterator;

public class PrimeFactorization implements Iterable<PrimeFactor> {
	private static final long OVERFLOW = -1;
	private long value; // the factored integer
						// it is set to OVERFLOW when the number is greater than 2^63-1, the
						// largest number representable by the type long.

	/**
	 * Reference to dummy node at the head.
	 */
	private Node head;

	/**
	 * Reference to dummy node at the tail.
	 */
	private Node tail;

	private int size; // number of distinct prime factors

	// ------------
	// Constructors
	// ------------

	/**
	 * Default constructor constructs an empty list to represent the number 1.
	 * 
	 * Combined with the add() method, it can be used to create a prime
	 * factorization.
	 */
	public PrimeFactorization() {
		head = new Node();
		tail = new Node();
		head.next = tail;
		// head.previous = null;
		tail.previous = head;
		// tail.next = null;
		size = 0;
		value=1;
	}

	/**
	 * Obtains the prime factorization of n and creates a doubly linked list to
	 * store the result. Follows the direct search factorization algorithm in
	 * Section 1.2 of the project description.
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization(long n) throws IllegalArgumentException {
		if (n < 1)
			throw new IllegalArgumentException();
		head = new Node();
		tail = new Node();
		head.next = tail;
		head.previous = null;
		tail.previous = head;
		tail.next = null;
		size = 0;
		value = n;
		long m = n;
		int d = 2;
		// while (d * d <= n) {
		while (d * d <= m) {
			if (m % d == 0) {
				add(d, 1);
				m = m / d;
			} else {
				d++;
				if (d % 2 == 0)
					d++;
			}
		}
		// }
		add((int) m, 1);
		updateValue();
	}

	/**
	 * Copy constructor. It is unnecessary to verify the primality of the numbers in
	 * the list.
	 * 
	 * @param pf
	 */
	public PrimeFactorization(PrimeFactorization pf) {
		head = new Node();
		tail = new Node();
		head.next = tail;
		head.previous = null;
		tail.previous = head;
		tail.next = null;
		size = 0;
		PrimeFactorizationIterator iter2 = pf.iterator();
		while (iter2.hasNext()) {
			iter2.next();
			add(iter2.pending.pFactor.prime, iter2.pending.pFactor.multiplicity);
		}
		updateValue();
	}

	/**
	 * Constructs a factorization from an array of prime factors. Useful when the
	 * number is too large to be represented even as a long integer.
	 * 
	 * @param pflist
	 */
	public PrimeFactorization(PrimeFactor[] pfList) {
		head = new Node();
		tail = new Node();
		head.next = tail;
		head.previous = null;
		tail.previous = head;
		tail.next = null;
		size = 0;
		for (int i = 0; i < pfList.length; i++) {
			add(pfList[i].prime, pfList[i].multiplicity);
		}
		updateValue();
	}

	// --------------
	// Primality Test
	// --------------

	/**
	 * Test if a number is a prime or not. Check iteratively from 2 to the largest
	 * integer not exceeding the square root of n to see if it divides n.
	 * 
	 * @param n
	 * @return true if n is a prime false otherwise
	 */
	public static boolean isPrime(long n) {
		for (int i = 2; i * i < n; i++) {
			if (n % i == 0)
				return false;
		}
		return true;
	}

	// ---------------------------
	// Multiplication and Division
	// ---------------------------

	/**
	 * Multiplies the integer v represented by this object with another number n.
	 * Note that v may be too large (in which case this.value == OVERFLOW). You can
	 * do this in one loop: Factor n and traverse the doubly linked list
	 * simultaneously. For details refer to Section 3.1 in the project description.
	 * Store the prime factorization of the product. Update value and size.
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public void multiply(long n) throws IllegalArgumentException {
		if (n < 1)
			throw new IllegalArgumentException();
		PrimeFactorization newP = new PrimeFactorization(n);
		PrimeFactorizationIterator iter = newP.iterator();
		multiply(newP);
	}

	/**
	 * Multiplies the represented integer v with another number in the factorization
	 * form. Traverse both linked lists and store the result in this list object.
	 * See Section 3.1 in the project description for details of algorithm.
	 * 
	 * @param pf
	 */
	public void multiply(PrimeFactorization pf) {
		this.head = multiply(this, pf).head;

		if (value != OVERFLOW)
			updateValue();
	}

	/**
	 * Multiplies the integers represented by two PrimeFactorization objects.
	 * 
	 * @param pf1
	 * @param pf2
	 * @return object of PrimeFactorization to represent the product
	 */
	public static PrimeFactorization multiply(PrimeFactorization pf1, PrimeFactorization pf2) {
		PrimeFactorization pf3 = new PrimeFactorization(pf1);
		PrimeFactorizationIterator iter = pf2.iterator();
		while (iter.hasNext()) {
			iter.next();
			pf3.add(iter.pending.pFactor.prime, iter.pending.pFactor.multiplicity);
		}
		return pf3;
	}

	/**
	 * Divides the represented integer v by n. Make updates to the list, value, size
	 * if divisible. No update otherwise. Refer to Section 3.2 in the project
	 * description for details.
	 * 
	 * @param n
	 * @return true if divisible false if not divisible
	 * @throws IllegalArgumentException if n <= 0
	 */
	public boolean dividedBy(long n) throws IllegalArgumentException {
		if (n <= 0)
			throw new IllegalArgumentException();
		if (!this.valueOverflow() && this.value() < n)
			return false;
		return dividedBy(new PrimeFactorization(n));
	}

	/**
	 * Division where the divisor is represented in the factorization form. Update
	 * the linked list of this object accordingly by removing those nodes housing
	 * prime factors that disappear after the division. No update if this number is
	 * not divisible by pf. Algorithm details are given in Section 3.2.
	 * 
	 * @param pf
	 * @return true if divisible by pf false otherwise
	 */
	public boolean dividedBy(PrimeFactorization pf) {
		if (!this.valueOverflow() && pf.valueOverflow())
			return false;
		if (!this.valueOverflow() && !pf.valueOverflow() && this.value() < pf.value())
			return false;
		if (!this.valueOverflow() && !pf.valueOverflow() && this.value() == pf.value()) {
			this.clearList();
			return true;
		}
		PrimeFactorization temp = dividedBy(this, pf);
		if (temp == null)
			return false;
		this.head = temp.head;
		this.size = temp.size();
		updateValue();
		return true;
	}

	/**
	 * Divide the integer represented by the object pf1 by that represented by the
	 * object pf2. Return a new object representing the quotient if divisible. Do
	 * not make changes to pf1 and pf2. No update if the first number is not
	 * divisible by the second one.
	 * 
	 * @param pf1
	 * @param pf2
	 * @return quotient as a new PrimeFactorization object if divisible null
	 *         otherwise
	 */
	public static PrimeFactorization dividedBy(PrimeFactorization pf1, PrimeFactorization pf2) {
		PrimeFactorization pf3 = new PrimeFactorization(pf1);
		PrimeFactorizationIterator iter1 = pf3.iterator();
		PrimeFactorizationIterator iter2 = pf2.iterator();
		while (iter1.cursor.pFactor.prime < iter2.cursor.pFactor.prime) {
			if (!iter1.hasNext() && iter2.hasNext())
				return null;
			if (iter1.cursor.pFactor.prime > iter2.cursor.pFactor.prime)
				return null;
			if (iter1.cursor.pFactor.prime == iter2.cursor.pFactor.prime
					&& iter1.cursor.pFactor.multiplicity < iter2.cursor.pFactor.multiplicity)
				return null;
			pf3.remove(iter2.cursor.pFactor.prime, iter2.cursor.pFactor.multiplicity);
			iter1.next();
			iter2.next();
			if (!iter2.hasNext())
				return pf3;
			if (!iter1.hasNext() && iter2.hasNext())
				return null;
		}
		return pf3;
	}

	// -------------------------------------------------
	// Greatest Common Divisor and Least Common Multiple
	// -------------------------------------------------

	/**
	 * Computes the greatest common divisor (gcd) of the represented integer v and
	 * an input integer n. Returns the result as a PrimeFactor object. Calls the
	 * method Euclidean() if this.value != OVERFLOW.
	 * 
	 * It is more efficient to factorize the gcd than n, which can be much greater.
	 * 
	 * @param n
	 * @return prime factorization of gcd
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization gcd(long n) throws IllegalArgumentException {
		if (n < 1)
			throw new IllegalArgumentException();
		if (!this.valueOverflow())
			return new PrimeFactorization(Euclidean(this.value(), n));
		PrimeFactorization pf2 = new PrimeFactorization(n);
		return gcd(pf2);
	}

	/**
	 * Implements the Euclidean algorithm to compute the gcd of two natural numbers
	 * m and n. The algorithm is described in Section 4.1 of the project
	 * description.
	 * 
	 * @param m
	 * @param n
	 * @return gcd of m and n.
	 * @throws IllegalArgumentException if m < 1 or n < 1
	 */
	public static long Euclidean(long m, long n) throws IllegalArgumentException {
		if (m < 1 || n < 1)
			throw new IllegalArgumentException();
		if (m == n)
			return m;
		long b = Math.min(m, n);
		long a = Math.max(m, n);
		while (a % b != 0) {
			long remainder = a % b;
			a = b;
			b = remainder;
		}
		return b;
	}

	/**
	 * Computes the gcd of the values represented by this object and pf by
	 * traversing the two lists. No direct computation involving value and pf.value.
	 * Refer to Section 4.2 in the project description on how to proceed.
	 * 
	 * @param pf
	 * @return prime factorization of the gcd
	 * @throws IllegalArgumentException if pf == null
	 */
	public PrimeFactorization gcd(PrimeFactorization pf) throws IllegalArgumentException {
		if (pf == null)
			throw new IllegalStateException();
		return gcd(this,pf);
	}

	/**
	 * 
	 * @param pf1
	 * @param pf2
	 * @return prime factorization of the gcd of two numbers represented by pf1 and
	 *         pf2
	 * @throws IllegalArgumentException if pf1 == null or pf2 == null
	 */
	public static PrimeFactorization gcd(PrimeFactorization pf1, PrimeFactorization pf2)
			throws IllegalArgumentException {
		if (pf1 == null || pf2==null) throw new IllegalArgumentException();
		PrimeFactorization gcd = new PrimeFactorization();
		PrimeFactorizationIterator iter1 = pf1.iterator();
		PrimeFactorizationIterator iter2 = pf2.iterator();
		while (iter1.hasNext() && iter2.hasNext()) {
			if (iter1.cursor.pFactor.prime == iter2.cursor.pFactor.prime) {
				gcd.add(iter1.cursor.pFactor.prime, Math.min(iter1.cursor.pFactor.multiplicity, iter2.cursor.pFactor.multiplicity));
				iter1.next();
				iter2.next();
			}
			else if (iter1.cursor.pFactor.prime > iter2.cursor.pFactor.prime) {
				iter2.next();
			}
			else iter1.next();
		}
		gcd.updateValue();
		return gcd;
	}

	/**
	 * Computes the least common multiple (lcm) of the two integers represented by
	 * this object and pf. The list-based algorithm is given in Section 4.3 in the
	 * project description.
	 * 
	 * @param pf
	 * @return factored least common multiple
	 * @throws IllegalArgumentException if pf == null
	 */
	public PrimeFactorization lcm(PrimeFactorization pf) throws IllegalArgumentException {
		if (pf == null) throw new IllegalArgumentException();
		return lcm(this,pf);
	}

	/**
	 * Computes the least common multiple of the represented integer v and an
	 * integer n. Construct a PrimeFactors object using n and then call the lcm()
	 * method above. Calls the first lcm() method.
	 * 
	 * @param n
	 * @return factored least common multiple
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization lcm(long n) throws IllegalArgumentException {
		if (n<1) throw new IllegalArgumentException();
		PrimeFactorization pf = new PrimeFactorization(n);
		return lcm(pf);
	}

	/**
	 * Computes the least common multiple of the integers represented by pf1 and
	 * pf2.
	 * 
	 * @param pf1
	 * @param pf2
	 * @return prime factorization of the lcm of two numbers represented by pf1 and
	 *         pf2
	 * @throws IllegalArgumentException if pf1 == null or pf2 == null
	 */
	public static PrimeFactorization lcm(PrimeFactorization pf1, PrimeFactorization pf2)
			throws IllegalArgumentException {
		if(pf1 == null || pf2==null) throw new IllegalArgumentException();
		PrimeFactorization lcm = new PrimeFactorization();
		PrimeFactorizationIterator iter1 = pf1.iterator();
		PrimeFactorizationIterator iter2 = pf2.iterator();
		
		while(iter1.hasNext() && iter2.hasNext()) {
			if (iter1.cursor.pFactor.prime == iter2.cursor.pFactor.prime) {
				lcm.add(iter1.cursor.pFactor.prime, Math.max(iter1.cursor.pFactor.multiplicity, iter2.cursor.pFactor.multiplicity));
				iter1.next();
				iter2.next();
			}
			else if (iter1.cursor.pFactor.prime > iter2.cursor.pFactor.prime) {
				lcm.add(iter2.cursor.pFactor.prime, iter2.cursor.pFactor.multiplicity);
				iter2.next();
			} 
			else {
				lcm.add(iter1.cursor.pFactor.prime, iter1.cursor.pFactor.multiplicity);
				iter1.next();
			}
		}
		while(iter1.hasNext()) {
			lcm.add(iter1.cursor.pFactor.prime, iter1.cursor.pFactor.multiplicity);
			iter1.next();
		}
		while(iter2.hasNext()) {
			lcm.add(iter2.cursor.pFactor.prime, iter2.cursor.pFactor.multiplicity);
			iter2.next();
		}
		if (!pf1.valueOverflow() && !pf2.valueOverflow())
		lcm.updateValue();
		return lcm;
	}

	// ------------
	// List Methods
	// ------------

	/**
	 * Traverses the list to determine if p is a prime factor.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p
	 * @return true if p is a prime factor of the number v represented by this
	 *         linked list false otherwise
	 */
	public boolean containsPrimeFactor(int p) {
		PrimeFactorizationIterator iter = iterator();
		while(iter.hasNext()) {
			if(iter.cursor.pFactor.prime == p) return true;
			iter.next();
		}
		return false;
	}

	// The next two methods ought to be private but are made public for testing
	// purpose. Keep
	// them public

	/**
	 * Adds a prime factor p of multiplicity m. Search for p in the linked list. If
	 * p is found at a node N, add m to N.multiplicity. Otherwise, create a new node
	 * to store p and m.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p prime
	 * @param m multiplicity
	 * @return true if m >= 1 false if m < 1
	 */
	public boolean add(int p, int m) {
		if (m < 1)
			return false;
		PrimeFactorizationIterator iter = iterator();
		if (size == 0) {
		} else {
			while (iter.hasNext() && iter.cursor.pFactor.prime < p) {
				iter.next();
			}
		}
		iter.add(new PrimeFactor(p, m));

		return true;
	}

	/**
	 * Removes m from the multiplicity of a prime p on the linked list. It starts by
	 * searching for p. Returns false if p is not found, and true if p is found. In
	 * the latter case, let N be the node that stores p. If N.multiplicity > m,
	 * subtracts m from N.multiplicity. If N.multiplicity <= m, removes the node N.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p
	 * @param m
	 * @return true when p is found. false when p is not found.
	 * @throws IllegalArgumentException if m < 1
	 */
	public boolean remove(int p, int m) throws IllegalArgumentException {
		PrimeFactorizationIterator iter = iterator();
		while (iter.hasNext() && iter.cursor.pFactor.prime < p) {
			iter.next();
		}
		if (!iter.hasNext() || iter.cursor.pFactor.prime > p)
			return false;
		if (iter.cursor.pFactor.multiplicity > m)
			iter.cursor.pFactor.multiplicity -= m;
		else
			iter.remove();
		return true;
	}

	/**
	 * 
	 * @return size of the list
	 */
	public int size() {
		return size;
	}

	/**
	 * Writes out the list as a factorization in the form of a product. Represents
	 * exponentiation by a caret. For example, if the number is 5814, the returned
	 * string would be printed out as "2 * 3^2 * 17 * 19".
	 */
	@Override
	public String toString() {
		PrimeFactorizationIterator iter = iterator();
		String s = "";

		if (iter.hasNext())
			s += iter.next().toString();
		while (iter.hasNext())
			s += " * " + iter.next().toString();
		return s;
	}

	// The next three methods are for testing, but you may use them as you like.

	/**
	 * @return true if this PrimeFactorization is representing a value that is too
	 *         large to be within long's range. e.g. 999^999. false otherwise.
	 */
	public boolean valueOverflow() {
		return value == OVERFLOW;
	}

	/**
	 * @return value represented by this PrimeFactorization, or -1 if
	 *         valueOverflow()
	 */
	public long value() {
		return value;
	}

	public PrimeFactor[] toArray() {
		PrimeFactor[] arr = new PrimeFactor[size];
		int i = 0;
		for (PrimeFactor pf : this)
			arr[i++] = pf;
		return arr;
	}

	@Override
	public PrimeFactorizationIterator iterator() {
		return new PrimeFactorizationIterator();
	}

	/**
	 * Doubly-linked node type for this class.
	 */
	private class Node {
		public PrimeFactor pFactor; // prime factor
		public Node next;
		public Node previous;

		/**
		 * Default constructor for creating a dummy node.
		 */
		public Node() {
			next = null;
			previous = null;
		}

		/**
		 * Precondition: p is a prime
		 * 
		 * @param p prime number
		 * @param m multiplicity
		 * @throws IllegalArgumentException if m < 1
		 */
		public Node(int p, int m) throws IllegalArgumentException {
			if (m < 0)
				throw new IllegalArgumentException();
			next = null;
			previous = null;
			pFactor = new PrimeFactor(p, m);
		}

		/**
		 * Constructs a node over a provided PrimeFactor object.
		 * 
		 * @param pf
		 * @throws IllegalArgumentException
		 */
		public Node(PrimeFactor pf) {
			next = null;
			previous = null;
			pFactor = pf;
			// new PrimeFactor(pf.prime,pf.multiplicity);

		}

		/**
		 * Printed out in the form: prime + "^" + multiplicity. For instance "2^3".
		 * Also, deal with the case pFactor == null in which a string "dummy" is
		 * returned instead.
		 */
		@Override
		public String toString() {
			if (this.pFactor == null)
				return "dummy";
			return this.pFactor.toString();
		}
	}

	private class PrimeFactorizationIterator implements ListIterator<PrimeFactor> {
		// Class invariants:
		// 1) logical cursor position is always between cursor.previous and cursor
		// 2) after a call to next(), cursor.previous refers to the node just returned
		// 3) after a call to previous() cursor refers to the node just returned
		// 4) index is always the logical index of node pointed to by cursor

		private Node cursor = head.next;
		private Node pending = null; // node pending for removal
		private int index = 0;

		// other instance variables ...

		/**
		 * Default constructor positions the cursor before the smallest prime factor.
		 */
		public PrimeFactorizationIterator() {

		}

		@Override
		public boolean hasNext() {
			return nextIndex() < size();
		}

		@Override
		public boolean hasPrevious() {
			return false;
		}

		@Override
		public PrimeFactor next() {
			pending = cursor;
			cursor = cursor.next;
			index++;
			return pending.pFactor;
		}

		@Override
		public PrimeFactor previous() {
			pending = cursor;
			cursor = cursor.previous;
			index--;
			return pending.pFactor;
		}

		/**
		 * Removes the prime factor returned by next() or previous()
		 * 
		 * @throws IllegalStateException if pending == null
		 */
		@Override
		public void remove() throws IllegalStateException {
			if (pending == null)
				throw new IllegalStateException();
			unlink(pending);
			pending = null;
			size--;
		}

		/**
		 * Adds a prime factor at the cursor position. The cursor is at a wrong position
		 * in either of the two situations below:
		 * 
		 * a) pf.prime < cursor.previous.pFactor.prime if cursor.previous != head. b)
		 * pf.prime > cursor.pFactor.prime if cursor != tail.
		 * 
		 * Take into account the possibility that pf.prime == cursor.pFactor.prime.
		 * 
		 * Precondition: pf.prime is a prime.
		 * 
		 * @param pf
		 * @throws IllegalArgumentException if the cursor is at a wrong position.
		 */
		@Override
		public void add(PrimeFactor pf) throws IllegalArgumentException {
			if (size == 0) {
				Node temp = new Node(pf);
				link(head, temp);
				size++;
			} else {
				if (cursor.previous != head && pf.prime < cursor.previous.pFactor.prime)
					throw new IllegalArgumentException();
				if (cursor != tail && pf.prime > cursor.pFactor.prime)
					throw new IllegalArgumentException();
				if (cursor == tail)
					previous();
				if (pf.prime == cursor.pFactor.prime)
					cursor.pFactor.multiplicity += pf.multiplicity;
				else {
					Node temp = new Node(pf);
					link(cursor, temp);
					size++;
				}
			}
		}

		@Override
		public int nextIndex() {
			return index;
		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		@Deprecated
		@Override
		public void set(PrimeFactor pf) {
			throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support set method");
		}

		// Other methods you may want to add or override that could possibly facilitate
		// other operations, for instance, addition, access to the previous element,
		// etc.
		//
		// ...
		//
	}

	// --------------
	// Helper methods
	// --------------

	/**
	 * Inserts toAdd into the list after current without updating size.
	 * 
	 * Precondition: current != null, toAdd != null
	 */
	private void link(Node current, Node toAdd) {
		toAdd.next = current.next;
		toAdd.previous = current;
		toAdd.next.previous = toAdd;
		current.next = toAdd;
	}

	/**
	 * Removes toRemove from the list without updating size.
	 */
	private void unlink(Node toRemove) {
		toRemove.next.previous = toRemove.previous;
		toRemove.previous.next = toRemove.next;
	}

	/**
	 * Remove all the nodes in the linked list except the two dummy nodes.
	 * 
	 * Made public for testing purpose. Ought to be private otherwise.
	 */
	public void clearList() {
		head.next = tail;
		tail.previous = head;
		size = 0;
	}

	/**
	 * Multiply the prime factors (with multiplicities) out to obtain the
	 * represented integer. Use Math.multiply(). If an exception is throw, assign
	 * OVERFLOW to the instance variable value. Otherwise, assign the multiplication
	 * result to the variable.
	 * 
	 */
	private void updateValue() {
		try {
			long v = 1;
			PrimeFactorizationIterator iter = iterator();
			while (iter.hasNext()) {
				iter.next();
				int p = iter.pending.pFactor.prime;
				int m = iter.pending.pFactor.multiplicity;
				for (int i = 0; i < m; i++)
					v = Math.multiplyExact(v, (long) p);
			}
			this.value = v;
		}

		catch (ArithmeticException e) {
			value = OVERFLOW;
		}

	}
}
