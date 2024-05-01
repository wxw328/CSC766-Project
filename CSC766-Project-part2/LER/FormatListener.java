import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;


public class FormatListener extends GloryBaseListener {

    private int loopCount = 0;
    private LinkedList<Integer> loopTypeList;
	private LinkedList<String> factorIds;
	private LinkedList<String> loopIds;
	private HashMap <String, GloryParser.LContext> loopIdLookup = new HashMap<>();
	private final ParseTreeProperty<String> newNodes = new ParseTreeProperty<>();
	private HashMap <GloryParser.FactorContext, LinkedList<String>> v2idMap = new HashMap<>();
	private HashMap <String, Set<String>> relLoops = new HashMap<>();
	private int inFactor = 0;
	public LinkedList<String> optimizedLER = new LinkedList<>();
	private boolean inE = false;
	private HashMap<GloryParser.TermContext, Set<GloryParser.FactorContext>> termLookup = new HashMap<>();
	private HashMap<String, String> loopTextLookUp = new HashMap<>();
	GloryParser.TermContext curTerm = null;

	public String getNewText(ParserRuleContext node) {
        return newNodes.get(node);
    }

    public FormatListener () {
        loopTypeList = new LinkedList<>();
		factorIds = new LinkedList<>();
		loopIds = new LinkedList<>();
    }


    @Override
    public void enterExpressionPrime(GloryParser.ExpressionPrimeContext ctx) {
				// // System.out.println(ctx.r().getText());
		// // System.out.println(ctx.l().getText());
		super.enterExpressionPrime(ctx);
		GloryParser.AddOpContext addOrMinus = ctx.addOp();
		if (addOrMinus != null && addOrMinus.getText() == "-")
		{
			curTerm = ctx.expression().term();
			newNodes.put(addOrMinus, "+");
			GloryParser.TermContext termCtx = ctx.expression().term();
			newNodes.put(termCtx, "-(" + termCtx.getText() + ")");
			// System.out.println(getNewText(termCtx));
		}
    }

	@Override
    public void enterE(GloryParser.EContext ctx) {
				// // System.out.println(ctx.r().getText());
		// // System.out.println(ctx.l().getText());
		// System.out.println("enter E"+ ctx.getText());
		super.enterE(ctx);
		inE = true;
		curTerm = ctx.expression().term();
    }

	@Override
    public void exitE(GloryParser.EContext ctx) {
				// // System.out.println(ctx.r().getText());
		// // System.out.println(ctx.l().getText());
		// System.out.println("exit E"+ ctx.getText());
		super.exitE(ctx);
		inE = false;
    }

	// Document all ids in a factor
	@Override
    public void enterFactor(GloryParser.FactorContext ctx) {
		// System.out.println("enter factor"+ ctx.getText());
				// // System.out.println(ctx.r().getText());
		// // System.out.println(ctx.l().getText());
		super.enterFactor(ctx);
		inFactor += 1;
		if (inFactor == 1)
		{
			if (termLookup.containsKey(curTerm))
				termLookup.get(curTerm).add(ctx);
			else
			{
				termLookup.put(curTerm, new HashSet<GloryParser.FactorContext>());
				termLookup.get(curTerm).add(ctx);
			}
		}
    }

	@Override
    public void enterId(GloryParser.IdContext ctx) {
		// System.out.println("enter id"+ ctx.getText());
		super.enterId(ctx);
		if (inFactor > 0 && loopIds.contains(ctx.getText()))
		{
			// factor's id list
			factorIds.add(ctx.getText());
		}
    }

	@Override
    public void exitFactor(GloryParser.FactorContext ctx) {
		// System.out.println("exit factor "+ ctx.getText() + " " + factorIds.size());
				// // System.out.println(ctx.r().getText());
		// // System.out.println(ctx.l().getText());
		super.exitFactor(ctx);
		inFactor -= 1;
		if (!factorIds.isEmpty() && inFactor == 0)
			{
				v2idMap.put(ctx, factorIds);
				factorIds = new LinkedList<>();
			}
    }

    @Override
    public void enterL(GloryParser.LContext ctx) {
		// System.out.println("enter loop"+ ctx.getText());
        super.enterL(ctx);

        if (ctx.loopType == null) {
            return;
        }
        loopTypeList.add(ctx.loopType.getType());
        loopCount++;

        int intend = loopCount;
        while (intend > 0) {
            intend--;
            for (int i = 0; i < intend; i++) {
                // System.out.printf(" ");
            }
        }

        if (ctx.loopType.getType() == GloryParser.OTHER) {
			String id = idFilter(ctx.subscript().id().getText());
			loopIds.add(id);
			int textLen = ctx.getText().length();
			if (ctx.l() != null)
				textLen -= ctx.l().getText().length();
			loopTextLookUp.put(id, ctx.getText().substring(0, textLen));
			loopIdLookup.put(id, ctx);
            // // System.out.print("while ");
        } else {
			String id = idFilter(ctx.forParam().id().getText());
			loopIds.add(id);
			int textLen = ctx.getText().length();
			if (ctx.l() != null)
				textLen -= ctx.l().getText().length();
			loopTextLookUp.put(id, ctx.getText().substring(0, textLen));
			loopIdLookup.put(id, ctx);
        }
    }

	@Override
    public void exitL(GloryParser.LContext ctx) {
		// System.out.println("exit loop " + ctx.getText());
        super.exitL(ctx);

        if (ctx.loopType == null) {
            return;
        }

		String loopId = "";

		if (ctx.loopType.getType() == GloryParser.OTHER) {
			loopId = idFilter(ctx.subscript().id().getText());
            // // System.out.print("while ");
        } else {
			loopId = idFilter(ctx.forParam().id().getText());
            // // System.out.print("for ");
        }
	
    }

	@Override
    public void exitR(GloryParser.RContext ctx) {
		// System.out.println("exit R"+ ctx.getText());
        super.exitR(ctx);
		// Check each term
		Integer tempId = 0;
		LinkedList<String> results = new LinkedList<>();
		for (GloryParser.TermContext term : termLookup.keySet())
		{
			String resultR = "";
			// one term, no need to seperate
			if (termLookup.keySet().size() == 1)
				resultR = ctx.getText();
			else
			{
				resultR = "temp" + tempId.toString();
				results.add(resultR);
				tempId++;
			}
			// System.out.println("here1");
			for (String loopId : loopIds)
			{
				relLoops.put(loopId, new HashSet<String>());
				relLoops.get(loopId).add(loopId);
				// for each factor related to some loops, add the loop ids to relloops
				// System.out.println("v2idMap " + v2idMap.entrySet().toString());
				for (GloryParser.FactorContext c : termLookup.get(term))
				{
					// System.out.println("Factor in term: " + c.getText());
					if (!v2idMap.containsKey(c))
						continue;
					LinkedList<String> lids = v2idMap.get(c);
					// System.out.println(c.getText() + " " + v2idMap.get(c));
					if (lids.contains(loopId))
					{
						for (String i : lids)
						{
							relLoops.get(loopId).add(i);
						}
					}
				}
			}
			// Split from the original grammar
			// System.out.println("Relloops " + relLoops.entrySet().toString());
			// System.out.println("loopidlookup " + loopIdLookup.entrySet().toString());
			for (String loopId : loopIds)
			{
				// System.out.println(loopId + " " + relLoops.get(loopId).toString());
				if ((loopIdLookup.get(loopId) != null && loopIdLookup.get(loopId).loopType.getType() == GloryParser.SUMMATION && relLoops.get(loopId).size() < loopIds.size())
				|| (loopIdLookup.get(loopId) != null && relLoops.get(loopId).size() == 1))
				{
					// System.out.println("here3 " + loopId);
					String extractedLoops = "";
					String relevantLoops = "";
					for (String lid: loopIds)
					{
						if (relLoops.get(loopId).contains(lid))
						{
							extractedLoops += loopTextLookUp.get(lid);
							// Other loops
							if (!lid.equals(loopId))
							{
								if (relevantLoops.length() == 0)
									relevantLoops += lid;
								else
									relevantLoops += "," + lid;
							}
						}
					}
					// System.out.println("here3");
					// Get the term and result
					String relevantFactors = "";
					for (GloryParser.FactorContext c : termLookup.get(term))
					{
						if (!v2idMap.containsKey(c))
							continue;
						LinkedList<String> lids = v2idMap.get(c);
						if (lids.contains(loopId))
						{
							if (relevantFactors.length() == 0)
								relevantFactors += c.getText();
							else
								relevantFactors += "*" + c.getText();
						}
					}
					if (relevantFactors.length() > 0)
					{
						extractedLoops += relevantFactors + "=";
						String tempVar = "temp" + tempId.toString() + "[" + relevantLoops + "]";
						extractedLoops += tempVar;
						tempId++;
						// System.out.println(extractedLoops);
						optimizedLER.add(extractedLoops);
						// Deal with the second part
						String secondPart = "";
						for (String lid: loopIds)
						{
							if (!lid.equals(loopId))
								secondPart += loopTextLookUp.get(lid);
						}
						relevantFactors = tempVar;
						for (GloryParser.FactorContext c : termLookup.get(term))
						{
							if (!v2idMap.containsKey(c))
								continue;
							LinkedList<String> lids = v2idMap.get(c);
							if (!lids.contains(loopId))
							{
								relevantFactors += "*" + c.getText();
							}
						}
						secondPart += relevantFactors + "=" + resultR;
						optimizedLER.add(secondPart);
						// System.out.println(secondPart);
					}
					// Just ignore that loop
					else
					{
						String secondPart = "";
						for (String lid: loopIds)
						{
							if (!lid.equals(loopId))
								secondPart += loopTextLookUp.get(lid);
						}
						relevantFactors = "";
						for (GloryParser.FactorContext c : termLookup.get(term))
						{
							if (!v2idMap.containsKey(c))
								continue;
							LinkedList<String> lids = v2idMap.get(c);
							if (!lids.contains(loopId))
							{
								relevantFactors += "*" + c.getText();
							}
						}
						secondPart += relevantFactors.substring(1) + "=" + resultR;
						optimizedLER.add(secondPart);
						// System.out.println(secondPart);
					}

					break;
				}
			}
		}
		if (results.size() > 0)
		{
			String res = "";
			for (String partial : results)
				res += partial + "+";
			res = res.substring(0, res.length()-1);
			res += "=" + ctx.getText();
			optimizedLER.add(res);
			// System.out.println(res);
		}
	}

    private static String checkSin(String string) {
        boolean isFound = string.contains("synthetic");

        if (isFound) {
            String refinedString = "sin(";
            int indexCount = 0;
            String[] arr = string.split("_");
            for (char ch : arr[1].toCharArray()) {
                if (ch == '[' ) {
                    break;
                }
                indexCount++;
            }

            String[] idArray = arr[1].substring(0, indexCount).split("");

            String[] indexArray = arr[1].substring(indexCount+1, arr[1].length()-1).split(",");

            for (int i = 0; i < indexArray.length; i++) {
                refinedString = refinedString + idArray[i] + "[" + indexArray[i] + "]+";
            }

            refinedString = refinedString.substring(0, refinedString.length()-1);
            string = refinedString + ")";
        }
        return string;
    }

    private String idFilter(String string) {

        StringBuilder sb = new StringBuilder();

        boolean andSwitch = true; //
        for (char ch : string.toCharArray()) {
            if (ch == '$') { // when encounter a '&', flip
                andSwitch = !andSwitch;
                continue;
            }

            if (andSwitch) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}

