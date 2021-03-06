package crawler.utils.ip;

import crawler.httpClient.HttpClientUtils;
import crawler.result.IPPort;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import crawler.utils.other.CustomedMethod;

import java.util.HashSet;
import java.util.Set;

public class XiCiIP extends  WebSite {
    private HttpClientUtils httpClientUtils = new HttpClientUtils();
    private volatile IPPort ipPort ;

    public XiCiIP(IPPort ipPort, String url, String keyWord, String webName,String charset) {
        this.ipPort = ipPort;
        this.setUrl(url);
        this.setWebName(webName);
        this.setKeyword(keyWord);
        this.setCharset(charset);
    }

    public Set<String> getFreeIpInSet() {
        //ipPort port,for example: 13.23.49.128 80
        Set<String> ipPortSet = new HashSet<String>();
        String content = httpClientUtils.getEntityContent(this.getUrl(),this.getCharset());
        System.out.println("content "+content);
        Document document = Jsoup.parse(content);

        Element ip_list = document.getElementById("ip_list");

        CustomedMethod.printDelimiter();
        System.out.println(ip_list);

        //get the td
        Elements classEmpty = ip_list.select("tr");
        System.out.println("tr.size is: "+classEmpty.size());

        //default value
        String ip="0.0.0.0";
        String port="8888";

        for (Element trEle : classEmpty) {
            System.out.println(trEle.toString());
            Elements tds = trEle.select("td");

            //if it not a efficient ipPort entry
            if(tds.size()<2)    continue;
            ip = tds.get(1).text();
            port = trEle.select("td").get(2).text();
            System.out.println("ipPort: " + ip + " port: "+port);
            ipPortSet.add(ip + " " + port);// add to set
        }
        return ipPortSet;
    }

    public void getFreeIpInQueue() {
        //ipPort port,for example: 13.23.49.128 80
        String content = httpClientUtils.getEntityContent(this.getUrl(),this.getCharset());
        //System.out.println("content "+content);
        Document document = Jsoup.parse(content);

        Element ip_list = document.getElementById("ip_list");

        //CustomedMethod.printDelimiter();
        //System.out.println(ip_list);

        //get the td
        Elements classEmpty = ip_list.select("tr");
        //System.out.println("tr.size is: "+classEmpty.size());

        //default value
        String ip="0.0.0.0";
        String port="8888";

        for (Element trEle : classEmpty) {
            //System.out.println(trEle.toString());
            Elements tds = trEle.select("td");

            //if it not a efficient ipPort entry
            if(tds.size()<2)    continue;
            ip = tds.get(1).text();
            port = trEle.select("td").get(2).text();
            String tempIP = ip + " " + port;
            //System.out.println("ipPort: " + ip + " port: "+port);
            if(ipPort.getIpPortQueue().contains(tempIP)){
                System.out.println("xiciIp: check the repeating ipPort....");
                continue;
            }

            //the synchronized to ipPort
            synchronized (ipPort) {
                if (ipPort.getIpPortQueue().size() >= 20) {
                    System.out.println("xiciIP producer wait...");
                    try {
                        ipPort.wait(); // the wait must in synchronized code
                        System.out.println("xiciIP producer waking...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("xiciIP: before produce "+ ipPort.getIpPortQueue().size());
                if (ipPort.getIpPortQueue().size() < 20) ipPort.getIpPortQueue().add(ip + " " + port);// add to queue
                System.out.println("xiciIP: after produce "+ ipPort.getIpPortQueue().size()+"\n");
                ipPort.notifyAll();
            }
        }
        System.out.println("xiciIP -> IpPortQueue's size is: "+ipPort.getIpPortQueue().size());
        //update the url
        this.setUrl(this.getNextUrl("http://www.xicidaili.com/nn/"));
        //System.out.println("after update: "+this.getUrl());
    }
}
