import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.InputStream;

public class Glory {
    public static void main(String[] args) throws Exception {

        ANTLRInputStream inputStream = new ANTLRInputStream(
                new FileInputStream(args[0]));
        // Get our lexer
        GloryLexer lexer = new GloryLexer(inputStream);
        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parsercatcat
        GloryParser parser = new GloryParser(tokens);

        // Specify our entry point
        GloryParser.StatementContext drinkSentenceContext = parser.statement();


        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        FormatListener myListener = new FormatListener();
        DirectiveListener listener = new DirectiveListener();
        try {
            walker.walk(myListener, drinkSentenceContext);
            if (myListener.optimizedLER.size() == 0)
                walker.walk(listener, drinkSentenceContext);
            else
            {
                System.out.println("Optimized LER:");
                for (String newLER : myListener.optimizedLER)
                    System.out.println(newLER + "\n");
                System.out.println("Optimized Program:");
                for (String newLER : myListener.optimizedLER)
                {
                    ANTLRInputStream optInputStream = new ANTLRInputStream(
                        newLER);
                    GloryLexer optlexer = new GloryLexer(optInputStream);
                    // Get a list of matched tokens
                    CommonTokenStream opttokens = new CommonTokenStream(optlexer);
        
                    // Pass the tokens to the parsercatcat
                    GloryParser optParser = new GloryParser(opttokens);
        
                    // Specify our entry point
                    GloryParser.StatementContext optContext = optParser.statement();
                    walker.walk(listener, optContext);
                }
            }
                
        } catch (Exception e) {
            // System.out.println(e);
            walker.walk(listener, drinkSentenceContext);
        }
    }


}
