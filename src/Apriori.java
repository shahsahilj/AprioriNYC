import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class Apriori {
	
	static ArrayList<String> itemset=new ArrayList<String>();
	static ArrayList<Basket> transactions=new ArrayList<Basket>();
	static ArrayList<ArrayList<Basket>> largeitemsets=new ArrayList<ArrayList<Basket>>();
	static HashMap<String, Double> sortedlargeitemsets=new HashMap<String, Double>();
	static HashMap<String, Double> sortedassociationrules=new HashMap<String, Double>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double minsupp=Double.parseDouble(args[1]);
		double minconf=Double.parseDouble(args[2]);
		String csvFile = args[0];
		File file = new File("output.txt");
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				Basket temp=new Basket();
				String[] t=line.split(",");
				temp.items.addAll(Arrays.asList(t));
				temp.items.remove("N/A");
				transactions.add(temp);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	      // creates the file
		PrintWriter printWriter=null;
		try {
			printWriter = new PrintWriter (file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		generateItemset();
		generateLargeSets(minsupp,printWriter);	
		printWriter.println();
		generateConfidence(minconf,printWriter);
		printWriter.close();
	}
	
	//Gets all items and puts in itemset from the transactions
	public static void generateItemset(){
		for (int i = 0; i < transactions.size(); i++) {
			Basket b=transactions.get(i);
			for (int j = 0; j < b.items.size(); j++) {
				if(!itemset.contains(b.items.get(j))){
					itemset.add(b.items.get(j));
				}
			}
		}
		
	}
	
	
	public static void generateLargeSets(double minsupp,PrintWriter printWriter){
		ArrayList<Basket> currentlis;
		ArrayList<Basket> prevlis=new ArrayList<Basket>();//This is L(k)
		Basket temp=new Basket();
		prevlis.add(temp);//Initializing L0 to null basket
		largeitemsets.add(prevlis);
		int count=0;
		do{
			currentlis=new ArrayList<Basket>();//This is L(k+1)
			ArrayList<Basket> candidateSet=new ArrayList<Basket>();
			prevlis=largeitemsets.get(count);//Getting the previous  large itemset
			for (int i = 0; i < prevlis.size(); i++) {
				Basket l=prevlis.get(i);//For each l in large itemset L(k)
				ArrayList<String> itemset_copy=new ArrayList<String>();
				itemset_copy.addAll(itemset);
				for (int j = 0; j < l.items.size(); j++) {
					itemset_copy.remove(l.items.get(j));//Finding itemset-l
				}
				for (int j = 0; j < itemset_copy.size(); j++) {
					Basket candidate=new Basket();
					candidate.items.addAll(l.items);
					candidate.items.add(itemset_copy.get(j));//l union with each i such that i -> Itemset-l
					boolean addc=true;
					for (int k = 0; k < candidate.items.size(); k++) {
						boolean exists=false;
						ArrayList<String> checkitems=new ArrayList<String>();
						checkitems.addAll(candidate.items);
						checkitems.remove(k);
						for (int m = 0; m < prevlis.size(); m++) {
							if(prevlis.get(m).items.containsAll(checkitems)){
								exists=true;
							}
						}
						if(exists==false){
							addc=false;
						}
					}
					if(addc==true){
						candidateSet.add(candidate);
					}
									}	
			}
			//Calculating support and generating the new large itemset
			double support;
			for (int i = 0; i < candidateSet.size(); i++) {
				Basket candidate=candidateSet.get(i);
				support=0;
				for (int j = 0; j < transactions.size(); j++) {
					Basket u=transactions.get(j);
					if(u.items.containsAll(candidate.items)){//If the candidate exists in the transaction
						support=support+1;
					}
				}
				support=support/transactions.size();
				if(support>=minsupp){
					candidate.support=support;
					boolean flag2=true;
					for (int j = 0; j < currentlis.size(); j++) {
						if(currentlis.get(j).items.containsAll(candidate.items)&&candidate.items.containsAll(currentlis.get(j).items)){
							flag2=false;
						}
					}
					if(flag2==true){
						currentlis.add(candidate);//Adding c to L(k+1) when its support>=minsupp
					}
				}
			}
			largeitemsets.add(currentlis);
			count++;
			
		}while(!currentlis.isEmpty());
		printWriter.println("==Frequent itemsets (min_sup="+minsupp*100+"%)");
		for (int i = 1; i < largeitemsets.size(); i++) {
			ArrayList<Basket> current=largeitemsets.get(i);
			for (int j = 0; j < current.size(); j++) {
				ArrayList<String> s=current.get(j).items;
				String a=s.toString()+","+current.get(j).support*100+"%";
				sortedlargeitemsets.put(a, current.get(j).support);
			}
		}
		TreeMap<String, Double> printlis=SortMap.SortByValue(sortedlargeitemsets);
		for(Map.Entry<String, Double> entry:printlis.entrySet()){
			printWriter.println(entry.getKey());
		}
	}
	
	public static void generateConfidence(double minconf,PrintWriter printWriter){
		printWriter.println("==High-confidence association rules (min_conf="+minconf*100+"%)");
		for (int i = 2; i < largeitemsets.size(); i++) {
			ArrayList<Basket> prev=largeitemsets.get(i-1);
			ArrayList<Basket> current=largeitemsets.get(i);
			for (int j = 0; j < current.size(); j++) {
				double num_support=current.get(j).support;
				ArrayList<String> s=current.get(j).items;
				for (int k = 0; k < s.size(); k++) {
					ArrayList<String> temp=new ArrayList<String>();
					temp.addAll(s);
					String rhs=s.get(k);
					double den_supp=0;
					boolean flag=false;
					temp.remove(rhs);
					for (int l = 0; l < prev.size(); l++) {
						if(prev.get(l).items.containsAll(temp)){
							den_supp=prev.get(l).support;
							flag=true;
							break;
						}
					}
					if(flag==true){
						double confidence=num_support/den_supp;
						if(confidence>=minconf){
							String b=temp.toString()+"=>["+rhs+"] (Conf: "+confidence*100+"%, Supp:"+num_support*100+"%)";
							sortedassociationrules.put(b, confidence);
						}
					}
				}
			}
		}
		TreeMap<String, Double> printassocrules=SortMap.SortByValue(sortedassociationrules);
		for(Map.Entry<String, Double> entry:printassocrules.entrySet()){
			printWriter.println(entry.getKey());
		}
	}

}
