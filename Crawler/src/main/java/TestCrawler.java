import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class TestCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))$");

    /**
     * Specify whether the given url should be crawled or not based on
     * the crawling logic. Here URLs with extensions css, js etc will not be visited
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean result = !FILTERS.matcher(href).matches() && href.startsWith("https://shikimori.one/animes");
        return result;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by the program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        int depth = page.getWebURL().getDepth();
        System.out.println("URL: " + url + " " + depth);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            for (int i = 0; i < 100; i++) {
                File file = new File(i + "");
                try {
                    FileWriter fileWriter = new FileWriter("" + i);
                    fileWriter.write(htmlParseData.getHtml());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            System.out.println(links.size());
            FileWriter file = null;
            try {
                file = new FileWriter("index.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (WebURL link : links) {
                try {
                    file.write(link + " " + depth + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}