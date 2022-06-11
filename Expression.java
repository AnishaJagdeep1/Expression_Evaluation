package app;

import java.io.*;

import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
	
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/

    	String prevToken = "";
    	String current = "";
    	
    	StringTokenizer separateTokens = new StringTokenizer(expr, delims, true);
    	while(separateTokens.hasMoreTokens() == true
    			|| (isDelims(prevToken) == false && Character.isDigit(prevToken.charAt(0)) == false)) {
    		
    		if(separateTokens.hasMoreTokens() == false) {
    			current = "+"; //We will ignore this anyways as it's the last item.
    		} else {
    			current = separateTokens.nextToken().trim();
    		}
    		if(current.length() == 0) continue;
  
    		if(isDelims(current) == true) {
    			if(current.equals("[") == true) {
    				Array arrTemp = new Array(prevToken);
    				if(arrays.contains(arrTemp) == false) {
						arrays.add(arrTemp);
					}
    			} else {
    				if(prevToken.trim().length() != 0) {
    					if(isDelims(prevToken) == false && Character.isDigit(prevToken.charAt(0)) == false) {
    						Variable varTemp = new Variable(prevToken);
    						if(vars.contains(varTemp) == false) {
    							vars.add(varTemp);
    						}
    					}
    				}
    			}
    		}
    		
    		prevToken = current;
    	}    	
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    
    
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	float result = 0;
    	
    	StringTokenizer separateTokens = new StringTokenizer(expr, delims, true); 
    	List<String> listTokens = new ArrayList<String>();
    	while(separateTokens.hasMoreTokens() == true) {
    		String temp = separateTokens.nextToken().trim(); //removes any spaces between each token
    		
    		if(temp.length() != 0) {
    			listTokens.add(temp);
    		}
    		
    	}
    	
    	//takes care of negative results obtained by solveMath
    	//turns the negative sign into a subtraction sign
    	int x = 0;
    	while(x < listTokens.size()) {
    		if(listTokens.get(x).equals("-") == true) {
    			if(listTokens.get(x - 1).equals("+") || listTokens.get(x - 1).equals("-") 
    					|| listTokens.get(x - 1).equals("*") || listTokens.get(x - 1).equals("/")
    					|| listTokens.get(x - 1).equals("(") || listTokens.get(x - 1).equals("[")) {
    				listTokens.set(x + 1, "-" + listTokens.get(x + 1));
    				listTokens.remove(x);
    			}
    		}
    		
    		x++;
    	}
    	
    	int indOpenPar = 0; //first instance of open parentheses
    	int countOpenPar = 0; //increments at every instance of open parentheses after the first one
    	int countClosedPar = 0; //increments at every instance of closed parentheses
    	
    	int i = 0;
    	String smallExpr = "";
    	
    	//checks for statements inside parentheses
    	while(listTokens.contains("(")) {
    		String current = listTokens.get(i);
    		 
    		if(current.equals(")")) {
     			countClosedPar++;
     		}
     		
    		if(current.equals("(") && countOpenPar == 0) {
    			indOpenPar = i;
    			countOpenPar++;
    		} else if(current.equals("(")) {
    			countOpenPar++;
    		} else if(current.equals(")") && (countOpenPar != 0 && countClosedPar != 0) && countOpenPar == countClosedPar) {
    			smallExpr = convertListToStr(indOpenPar + 1, i, listTokens);
    			
    			float value = evaluate(smallExpr, vars, arrays);
    			
    			
    			for(int j = i; j >= indOpenPar; j--) {
    				listTokens.remove(j);
    			}
    			
    			listTokens.add(indOpenPar, "" + value);
    			
    			if(indOpenPar != 0) {
    				if(isDelims(listTokens.get(indOpenPar - 1)) == false) {
    					listTokens.add(indOpenPar, "*");
    				}
    			}
    			
    			indOpenPar = 0;
    	    	countOpenPar = 0; 
    	    	countClosedPar = 0;
    	    	i = -1;
    		}
    		
    		i++;
    	}
    	
    	int indOpenBrack = 0; //first instance of open brackets
    	int countOpenBrack = 0; //increments at every instance of open brackets after the first one
    	int countClosedBrack = 0; //increments at every instance of closed brackets
    	
    	int k = 0;
    	smallExpr = "";
    	
    	//checks for statements inside brackets
    	while(listTokens.contains("[")) {
    		String current = listTokens.get(k);
    		
    		if(current.equals("]")) {
     			countClosedBrack++;
     		}
    		
    		if(current.equals("[") && countOpenBrack == 0) {
    			indOpenBrack = k;
    			countOpenBrack++;
    		} else if(current.equals("[")) {
    			countOpenBrack++;
    		} else if(current.equals("]") && (countOpenBrack != 0 && countClosedBrack != 0) && countOpenBrack == countClosedBrack) {
    			smallExpr = convertListToStr(indOpenBrack + 1, k, listTokens);
    			   			
    			float value = evaluate(smallExpr, vars, arrays);
    			
    			for(int l = k - 1; l > indOpenBrack; l--) {
    				listTokens.remove(l);
    				k--;
    			}
    			
    			listTokens.add(indOpenBrack + 1, "" + value);
    			
    			substituteArrayValue(indOpenBrack - 1, k + 1, "" + value, listTokens, arrays);		
    			
      			indOpenBrack = 0;
    	    	countOpenBrack = 0; 
    	    	countClosedBrack = 0; 
    	    	k = -1;
    		}
    		
    		k++;
    	}
    	
    	int countVar = 0;
    	
    	//substitutes numbers in for all variables in expr
    	while(countVar < listTokens.size()) {
    		String element = listTokens.get(countVar);
    		
    		for(int z = 0; z < vars.size(); z++) {
    			if(element.equals(vars.get(z).name)) {
    				listTokens.set(countVar, "" + vars.get(z).value);
    				break;
    			}
    		}
    		
    		countVar++;
    	}
    	
    	//checks if mathExpr is a negative number
    	String mathExpr = solveMath(listTokens);
    	if(mathExpr.contains("-") == true) {
    		mathExpr = mathExpr.substring(1, mathExpr.length());
    		//converts string to float
    		result = 0 - Float.parseFloat(mathExpr);
    	} else {
    		result = Float.parseFloat(mathExpr);
    	}
    	
    	return result;
    }
    
    private static String solveMath(List<String> listTokens) {
    	Stack<String> numbers = new Stack<String>();
    	Stack<String> operators = new Stack<String>();
    	Stack<String> opMD = new Stack<String>(); //contains only "*" or "/"
    	Stack<String> opAS = new Stack<String>(); //contains only "+" or "-"
    	
    	for(int i = 0; i < listTokens.size(); i++) {
    		String current = listTokens.get(i);

    		//pushes in a number 
    		if(isDelims(current) == false) {
    			numbers.push(current);
    			//first checks if there are multiplication and division operators
    			while(operators.isEmpty() == false && numbers.size() != 1) {
    				if(operators.peek().equals("*") == true) {
    					multiply(numbers, operators, opMD);
    				} else if(operators.peek().equals("/") == true) {
    					divide(numbers, operators, opMD);
    					//then checks if there are addition and subtraction operators
    					//makes sure if there are multiplication or division operators after
    				} else if(operators.peek().equals("+") == true) {
    					if(opMD.isEmpty() == false) {
    						String MD = opMD.pop(); 
    						if(i != listTokens.size() - 1) {
    							if(listTokens.get(i + 1).equals("*") == true 
    									|| listTokens.get(i + 1).equals("/") == true) {
    								break;
    							} else {
    								add(numbers, operators, opAS);
    							}
    						} else {
    							add(numbers, operators, opAS);
    						}
    					} else {
    						if(i != listTokens.size() - 1) {
    							if(listTokens.get(i + 1).equals("*") == true 
    									|| listTokens.get(i + 1).equals("/") == true) {
    								break;
    							} else {
    								add(numbers, operators, opAS);
    							}
    						} else {
    							add(numbers, operators, opAS);
    						}
    					}
    				} else if(operators.peek().equals("-") == true) {
    					if(opMD.isEmpty() == false) {
    						String MD = opMD.pop();
    						if(i != listTokens.size() - 1) {
    							if(listTokens.get(i + 1).equals("*") == true 
    									|| listTokens.get(i + 1).equals("/") == true) {
    								break;
    							} else {
    								subtract(numbers, operators, opAS);
    							}
    						} else {
    							subtract(numbers, operators, opAS);
    						}
    					} else {
    						if(i != listTokens.size() - 1) {
    							if(listTokens.get(i + 1).equals("*") == true 
    									|| listTokens.get(i + 1).equals("/") == true) {
    								break;
    							} else {
    								subtract(numbers, operators, opAS);
    							}
    						} else {
    							subtract(numbers, operators, opAS);
    						}
    					}
    				}
    			}
    		}

    		//pushes in an operator
    		if(isDelims(current) == true) {
    			operators.push(current);

    			if(current.equals("*") == true || current.equals("/") == true) {
    				opMD.push(current);
    			} else {
    				opAS.push(current);
    			}
    		}
    	}

    	return numbers.pop();
    }
    
    private static void substituteArrayValue(int startVal, int endVal, String value, List<String> listTokens, ArrayList<Array> arrays) {
    	//checks if value inside brackets is a float
    	if(value.contains(".") == true) {
    		value = value.substring(0, value.indexOf("."));
    	}
    	
    	int arrayVal = Integer.parseInt(value);
    	int countArr = 0;
    	
    	//substitutes numbers in for all arrays in expr
   		String element = listTokens.get(startVal);
    		
    		for(int i = 0; i < arrays.size(); i++) {
    			if(element.equals(arrays.get(i).name)) {
    				if(arrays.get(i).values.length < arrayVal) {
    					System.out.println("Array index " + arrayVal + " is invalid.");
    				}
    				
    				listTokens.set(startVal, "" + arrays.get(i).values[arrayVal]);
    				
    				for(int j = endVal; j >= startVal + 1; j--) {
    					listTokens.remove(j);
    				}
    			}
    		}
    		
   
    }
    
    private static boolean isDelims(String element) {
    	if(element.equals(" ") == true || element.equals("\t") == true || element.equals("*") == true 
    			|| element.equals("+") == true || element.equals("-") == true || element.equals("/") == true 
    			|| element.equals("(") == true || element.equals(")") == true || element.equals("[") == true 
    			|| element.equals("]") == true) {
    		return true;
    	}
    	
    	return false;
    }
    
    private static String convertListToStr(int startVal, int endVal, List<String> listTokens) {
    	String expr = "";
    	
    	for(int i = startVal; i < endVal; i++) {
			expr = expr + listTokens.get(i);
		}
    	
    	return expr;
    }
    
    private static void multiply(Stack<String> numbers, Stack<String> operators, Stack<String> opMD) {
    	String multiply = operators.pop();
		String num1 = numbers.pop();
		String num2 = numbers.pop();
		String result = "" + (Float.parseFloat(num2)*Float.parseFloat(num1));
		numbers.push(result);
    }
    
    private static void divide(Stack<String> numbers, Stack<String> operators, Stack<String> opMD) {
    	String divide = operators.pop();
		String num1 = numbers.pop();
		String num2 = numbers.pop();
		String result = "" + (Float.parseFloat(num2)/Float.parseFloat(num1));
		numbers.push(result);
    }
    
    private static void add(Stack<String> numbers, Stack<String> operators, Stack<String> opAS) {
    	String add = operators.pop();
		String num1 = numbers.pop();
		String num2 = numbers.pop();
		String result = "" + (Float.parseFloat(num2) + Float.parseFloat(num1));
		numbers.push(result);
		String AS = opAS.pop();
    }
    
    private static void subtract(Stack<String> numbers, Stack<String> operators, Stack<String> opAS) {
    	String subtract = operators.pop();
		String num1 = numbers.pop();
		String num2 = numbers.pop();
		String result = "" + (Float.parseFloat(num2) - Float.parseFloat(num1));
		numbers.push(result);
		String AS = opAS.pop();
    }
}