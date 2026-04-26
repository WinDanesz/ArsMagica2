package am2.common.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class WebRequestUtils {
    private static final String charset = "UTF-8";
    private static final String USER_AGENT = "Mozilla/5.0";

    public static String sendPost(String webURL, HashMap<String, String> postOptions) throws Exception {
        URL obj = new URL(webURL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(10000);
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "";
        for (String s : postOptions.keySet()) {
            urlParameters += String.format("%s=%s&", s, URLEncoder.encode(postOptions.get(s), charset));
        }
        if (urlParameters.contains("&"))
            urlParameters = urlParameters.substring(0, urlParameters.lastIndexOf('&'));

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(urlParameters);
            wr.flush();
        }

        StringBuffer response = new StringBuffer();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine + "\r\n");
            }
        }

        //print result
        return response.toString();

    }
}
