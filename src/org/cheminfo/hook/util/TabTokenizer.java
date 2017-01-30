package org.cheminfo.hook.util;


import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
  *
  **/

public class TabTokenizer implements	Enumeration	{

	 final private static String FIRST_DELIMITERS="\t";
	 final private static String SECOND_DELIMITERS="\t";

	 /**
	 * String to tokenize.
	 */
	 private String	str	= null;

	 /**
	 * Current position	in str string.
	 */
	 private int current = 0;

	 /**
	  *	Maximal	number of token
	  */

	 private int maximalNumberOfToken=1000000;

	 /**
	  *	Current	token
	  */

	 private int currentToken=0;

	 /**
	 * The first delimiter (a String containing all the possible characters).
	 */
	 private String firstDelimiters;

	 /**
	 * The second delimiters (a String containing all the possible characters).
	 */
	 private String	secondDelimiters;

	 /**
	 * Maximal position	in str string.
	 */

	private	int	max	= 0;


   /**
	* Constructs a string tokenizer	for	the	specified string.
	* The delimiter	is a comma followed	by any number of space.
	* A	generalised	version	allowed	to use any "first delimiter" followed by
	* any character	contained in a string (second delimiter). It
	*
	* @param str		   a string	to be parsed
	*/
	public TabTokenizer(String str) {
		this(str, FIRST_DELIMITERS,	SECOND_DELIMITERS);
	}

   /**
	* Constructs a string tokenizer	for	the	specified string.
	* The delimiter	is a comma followed	by any number of space
	*
	* @param str				 a string to be	parsed
	* @param firstDelimiter		 one character corresponding to	the	first delimiter
	*/
	public TabTokenizer(String str, String firstDelimiters) {
		this(str, firstDelimiters,	SECOND_DELIMITERS);
	}

   /**
	* Constructs a string tokenizer	for	the	specified string.
	* The delimiter	is a comma followed	by any number of space
	*
	* @param str				 a string to be	parsed
	* @param firstDelimiter		 one character corresponding to	the	first delimiter
	* @param secondDelimiters	 a string containing all the characters	that
	*							 can be	used as	second delimiters
	*/
	protected TabTokenizer(String str, String firstDelimiters, String secondDelimiters) {
		this.str =	str;
		this.firstDelimiters=firstDelimiters;
		this.secondDelimiters=secondDelimiters;
		max = str.length()-1;
	}

   /**
	* Tests	if there are more tokens available from	this tokenizer's string.
	* If this method returns true, then	a subsequent call to nextToken with
	* no argument will successfully	return a token.
	*
	* @return true if and only if there	is at least	one	token in the string
	* after	the	current	position; false	otherwise.
	*/
	public boolean hasMoreTokens() {
		return	(current <=	max);
	}

   /**
	* Returns the next token from this string tokenizer.
	*
	* @return the next token from this string tokenizer
	*
	* @exception NoSuchElementException	 if	there are no more tokens in	this
	*									 tokenizer's string
	*/
	public String nextToken()
	{
		if (current >	max) return	"";
		
		currentToken++;
		
		if (current == max) return str.substring(current,++current);

		int nextDel=this.str.indexOf(this.firstDelimiters, current);
		
		String returnString;
		
		if (nextDel < 0)
		{
			returnString=this.str.substring(current, this.str.length());
			current=max+1;
		}
		else
		{
			returnString=this.str.substring(current, nextDel);
			current=nextDel+1;
		}
		return returnString;
	}
	
	/** Set the maximum number of Token to be returned.
	* When this value is reached, the last token will contain all what left.
	*/
		
	public void	setMaxNumberToken (int maximalNumberOfToken) {
		this.maximalNumberOfToken=maximalNumberOfToken;
	}

	/**
	 * Returns the same	value as the hasMoreTokens method. It exists so	that
	 * this	class can implement	the	Enumeration	interface.
	 *
	 * @return true	if there are more tokens; false	otherwise.
	 */
	public boolean hasMoreElements() {
	  return hasMoreTokens();
	}


	protected static void main(String args[]) { // we	will just test this	procedure
		TabTokenizer cst=new TabTokenizer("this, is,just, a, test, X");
		while	(cst.hasMoreTokens()) System.out.println("-->"+cst.nextToken()+"<--");
		cst=new TabTokenizer("this,  is,  another,		test,	X,");
		cst.setMaxNumberToken(2);
		while	(cst.hasMoreTokens()) System.out.println("-->"+cst.nextToken()+"<--");
		cst=new TabTokenizer("and another,	");
		while	(cst.hasMoreTokens()) System.out.println("-->"+cst.nextToken()+"<--");
	}

	/**
	* Returns the same value as	the	nextToken method, except that its
	* declared return value	is Object rather than String. It exists	so that
	* this class can implement the Enumeration interface.
	*
	* @return the next token in	the	string
	*
	* a	null value if no more token
	*/
	public Object nextElement()	{
		return nextToken();
	}

}
