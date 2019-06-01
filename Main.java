import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.*;

public class Main {
	// Anything after cutoff character in link name is irrelevant; reduces redundancy
    private static final int cutoff = 5;
    private static final String wiki = "https://en.wikipedia.org/wiki/";
    private static Map<String, String> parent = new HashMap();
    private static Map<String, List<String>> linkMap = new HashMap();
    private static boolean found = false;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: Main startPage endPage");
            System.exit(1);
        }

        // discards unnessesary output
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);

        WebClient webClient = new WebClient();
        parent.put(args[0], args[0]);
        BFS(args[0], args[1], webClient);
        System.out.println();
        System.out.println(parent.get(args[1]));
    }

    // Produces list of main links from given page
    private static List<String> getLinks(String pageLink, WebClient webClient) throws Exception {
        System.out.println("Searching " + parent.get(pageLink));

        // parse page xml as string
        final HtmlPage page = webClient.getPage(wiki + pageLink);
        String pageInXML = page.asXml();
        int start = pageInXML.indexOf("<div id=\"bodyContent\"");
        int end = pageInXML.indexOf("<span class=\"mw-headline\" id=\"Sources\">");
        if (end == -1) end = pageInXML.indexOf("<span class=\"mw-headline\" id=\"References\">");
        if (end == -1) end = pageInXML.indexOf("<span class=\"mw-headline\" id=\"See_also\">");
        if (start == -1 || end == -1) return null;
        pageInXML = pageInXML.substring(start, end);

        // gather all links from xml string
        int fromIndex = 0;
        List<String> links = new ArrayList();
        while ((fromIndex = pageInXML.indexOf("href=\"/wiki/", fromIndex)) != -1) {
            String link = pageInXML.substring(fromIndex += 12, pageInXML.indexOf("\"", fromIndex));
            // discard images and other unwanted pages
            if (link.indexOf("File:") == -1 && link.indexOf("/") == -1 && link.indexOf(":") == -1)
                links.add(link);
        }
        return links;
    }

    // Breadth First Search
    private static void BFS (String s, String d, WebClient webClient) throws Exception {
        Queue<String> Q = new LinkedList();
        Q.add(s);
        while (!found && Q.size() > 0) {
            String x = Q.poll();
            // anything after cutoff is discarded
            String linkMapString = (x.length() > cutoff ? x.substring(0, cutoff) : x);
            if (!linkMap.containsKey(linkMapString)) linkMap.put(linkMapString, getLinks(x, webClient));
            List<String> links = linkMap.get(linkMapString);
            if (links != null)
                for (String link : links) {
                    if (!parent.containsKey(link)) {
                        if (link.equals(d)) found = true;
                        parent.put(link, parent.get(x) + "->" + link);
                        Q.add(link);
                    }
                }
        }

    }
}
