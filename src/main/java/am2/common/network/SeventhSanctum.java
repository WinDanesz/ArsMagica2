package am2.common.network;

import am2.ArsMagica;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SeventhSanctum {
    private static final String webURL = "http://www.seventhsanctum.com/generate.php?Genname=spell";
    private boolean failed = false;

    private static final HashMap<String, String> postOptions = new HashMap<String, String>();

    public static final SeventhSanctum instance = new SeventhSanctum();

    private ConcurrentLinkedQueue<String> suggestions;
    private boolean isFetching = false;

    private SeventhSanctum() {
        suggestions = new ConcurrentLinkedQueue<String>();
    }

    public void init() {
        postOptions.clear();
        postOptions.put("selGenCount", "25");
        postOptions.put("selGenType", "SEEDALL");

        if (ArsMagica.config.suggestSpellNames())
            getSuggestions();
        else
            failed = true;
    }

    public String getNextSuggestion() {
        if (failed) return "";

        if (suggestions.isEmpty() && !isFetching)
            getSuggestions();

        if (suggestions.isEmpty())
            return "";

        return suggestions.poll();
    }

    private void getSuggestions() {
        if (isFetching) return;
        isFetching = true;
        new Thread(() -> {
            try {
                String s = am2.common.utils.WebRequestUtils.sendPost(webURL, postOptions);
                int startIndex = s.lastIndexOf("SubSubContentTitle");
                if (startIndex == -1) return;
                startIndex = s.indexOf("<!--Title -->", startIndex) + 13;
                int endIndex = s.indexOf("&nbsp;", startIndex);
                if (endIndex == -1) return;

                s = s.substring(startIndex, endIndex);
                s = s.replace("\t", "");
                String[] newSuggestions = s.split("<div class=\"GeneratorResult");
                for (String suggestion : newSuggestions)
                    parseAndAddSuggestion(suggestion);
            } catch (Throwable t) {
                am2.ArsMagica.LOGGER.error("Exception caught: ", t);
                failed = true;
            } finally {
                isFetching = false;
            }
        }, "SeventhSanctum Fetcher Thread").start();
    }

    private void parseAndAddSuggestion(String s) {
        if (!s.endsWith("</div>")) return;

        int startIndex = s.indexOf(">");
        if (startIndex == -1) return;
        int endIndex = s.indexOf("<", startIndex);
        if (endIndex == -1) return;

        s = s.substring(startIndex + 1, endIndex);
        if (s.length() <= 20)
            suggestions.add(s);
    }
}
