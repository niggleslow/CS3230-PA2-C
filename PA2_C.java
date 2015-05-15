import java.util.*;
import java.io.*;

/**
 * 
 * @author Nicholas Low A0110574N
 *
 * CS3230 PA2 Part C
 * 
 * Input format:
 * 
 * First line of input is the number T of testcases. Next are T blocks of lines, each block describes a testcase. 
 * In each block: First line is N, number of variables, and K, number of clauses. Variables are indexed from 1 to N, clauses -- from 1 to K.
 * Next K lines describe K clauses in order from 1 to K, one clause per line. 
 * A clause contains 3 literals, their indices are represented by 3 numbers m, n, p. 
 * A positive number represent index of variable (positive literal). A negative number represent an index of negate variable (negative literal).
 * 
 **/
public class PA2_C {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		PrintWriter pw = new PrintWriter(
							new BufferedWriter(
									new OutputStreamWriter(System.out)));
		int numberOfTests = sc.nextInt();
		//System.out.println("Number of tests: " + numberOfTests);
		
		for(int i = 0; i < numberOfTests; i++) {
			int numberOfVariables = sc.nextInt();
			int numberOfClauses = sc.nextInt();
			//System.out.println("Number of variables: " + numberOfVariables);
			//System.out.println("Number of clauses: " + numberOfClauses);
			sc.nextLine();
			
			String clause = "";
			ArrayList<String> clauses = new ArrayList<String>();
			for(int j = 0; j < numberOfClauses; j++) {
				clause = sc.nextLine();
				clauses.add(clause);
			}
			/**
			 * To check "clauses":
			 * 		for(int a = 0; a < clauses.size(); a++) {
			 * 			System.out.print(clauses.get(a) + " ");
			 * 		}
			 **/
			DHC graph = new DHC(numberOfVariables, numberOfClauses, clauses);
			graph.birth();
			pw.write(graph.toString());		
		}
		pw.close();
		sc.close();
	}
}

class DHC { //Directed Hamiltonian Cycle class. Handles transformation of 3CNF_SAT to DHC!
	
	/*CLASS VARIABLES*/
	private int numberOfClauses;
	private int numberOfLiterals;
	private ArrayList<String> clauses;
	
	/*DATA STRUCTURES TO HOLD GRAPH (edges & adjlist)*/
	private ArrayList<ArrayList<String>> edges;
	private HashMap<String, HashSet<String>> adjList;
	
	/*CONSTRUCTOR*/
	public DHC(int noVars, int noClauses, ArrayList<String> clauses) {
		numberOfClauses = noClauses;
		numberOfLiterals = noVars;
		this.clauses = clauses; 
		
		edges = new ArrayList<ArrayList<String>>();
		adjList = new HashMap<String, HashSet<String>>();
	}
	
	/*INVOKER METHOD*/
	public void birth() {
		spawnVertices();
		spawnVerticalEdges();
		spawnSTEdges();
		spawnClausalEdges();
		
		Collections.sort(edges, new Comparator<ArrayList<String>>() {
			public int compare(ArrayList<String> a, ArrayList<String> b) {
				if (a.get(0).compareTo(b.get(0)) < 0) { //case 1: a1 < b1, return a < b
					return -1;
				} else if (a.get(0).compareTo(b.get(0)) > 0) { //case 2: a1 > b1, return a > b
					return 1;
				} else { //if a1 == b1
					if (a.get(1).compareTo(b.get(1)) < 0) { //case 3: a2 < b2, return a < b
						return -1;
					} else if (a.get(1).compareTo(b.get(1)) > 0) { //case 4: a2 > b2, return a > b
						return 1;
					} else { //case 5: a1 == b1, a2 == b2, return a == b
						return 0;
					}
				}
			}
		});
	}
	
	/*SPAWNING METHODS*/
	private void spawnVertices() {
		for(int n = 1; n <= numberOfLiterals; n++) {
			for(int k = 1; k <= numberOfClauses; k++) {
				String leftK = "X" + n + "." + k + "." + "L";
				String rightK = "X" + n + "." + k + "." + "R";
				String bufferK = "B" + n + "." + k;
				String bufferPrevious = "B" + n + "." + (k - 1);
				
				linkBothDir(bufferPrevious, leftK);
				linkBothDir(leftK, rightK);
				linkBothDir(rightK, bufferK);
			}
			String bufferFirst = "B" + n + "." + 0;
			String leftest = "L" + n;
			linkBothDir(bufferFirst, leftest);
			
			String bufferLast = "B" + n + "." + numberOfClauses;
			String rightest = "R" + n;
			linkBothDir(bufferLast, rightest);
		}
	}
	
	private void spawnVerticalEdges() {
		for(int n = 1; n < numberOfLiterals; n++) {
			String leftest = "L" + n;
			String leftestPlusOne = "L" + (n+1);
			String rightest = "R" + n;
			String rightestPlusOne = "R" + (n+1);
			
			establishEdge(leftest, leftestPlusOne);
			establishEdge(leftest, rightestPlusOne);
			establishEdge(rightest, rightestPlusOne);
			establishEdge(rightest, leftestPlusOne);
		}
	}
	
	private void spawnSTEdges() {
		String start = "S";
		String end = "T";
		String Lfirst = "L" + 1;
		String Rfirst = "R" + 1;
		String Llast = "L" + numberOfLiterals;
		String Rlast = "R" + numberOfLiterals;
		
		establishEdge(start, Lfirst);
		establishEdge(start, Rfirst);
		establishEdge(Llast, end);
		establishEdge(Rlast, end);
		establishEdge(end, start);
	}
	
	private void spawnClausalEdges() {
		for (int j = 0, k = 1; j < clauses.size(); j++, k++) {
			String clause = "C" + k;
			String[] literals = clauses.get(j).split("\\s+");
			for (String literal : literals) {
				int value = Integer.parseInt(literal);
				int index = Math.abs(value);
				boolean isPositive = (value > 0);
				String leftK = "X" + index + "." + k + "." + "L";
				String rightK = "X" + index + "." + k + "." + "R";
				if(isPositive) {
					establishEdge(leftK, clause);
					establishEdge(clause, rightK);
				} else {
					establishEdge(rightK, clause);
					establishEdge(clause, leftK);
				}
			}
		}
	}
	
	/*HELPER METHODS*/
	private void establishEdge(String from, String to) {
		// Storage in the edge list
		if (adjList.containsKey(from) && adjList.get(from).contains(to)) {
			//from link to to already exists, do nothing
		} else {
			ArrayList<String> newEdge = new ArrayList<String>();
			newEdge.add(from);
			newEdge.add(to);
			edges.add(newEdge);
		}
		// Opposite Direction
		if (adjList.containsKey(from)) {
			HashSet<String> incidentEdges =adjList.get(from);
			incidentEdges.add(to);
		} else {
			HashSet<String> incidentEdges = new HashSet<String>();
			incidentEdges.add(to);
			adjList.put(from, incidentEdges);
		}
	}
	
	private void linkBothDir(String from, String to) {
		establishEdge(from, to);
		establishEdge(to, from);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//Print number of vertices and number of edges
		sb.append(adjList.size());
		sb.append(" ");
		sb.append(edges.size());
		sb.append("\n");
		for (int i = 0; i < edges.size(); i++) { //Print edges
			ArrayList<String> currentEdge = edges.get(i);
			sb.append(currentEdge.get(0));
			sb.append(" ");
			sb.append(currentEdge.get(1));
			sb.append("\n");
		}
		return sb.toString();
	}
}
