package crawler;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.*;
import org.apache.http.client.*;
import org.apache.*;
import org.apache.http.client.*;
import org.apache.commons.*;
import org.apache.http.protocol.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.*;
import org.apache.http.params.*;
import org.apache.http.impl.client.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;


public class Crawler {

	private FilaIC fila1;
	private Log log;
	private RadixTreeImpl<String> RTree1;
	private static String shost;
	private LinkedList<String> disallow;
	private boolean userobots;
	private LinkedList<String> allow;
	private String extensiondownload;
	private boolean wantdownload;

	public Crawler(){
		wantdownload = true;
		userobots = true;
		log = new Log();
		log.write("Web Crawler IC UFSCar",0);
		log.write("Log is being saved to " + log.getname(),2);
		log.write("Initializing Link Queue... ",4);
		fila1 = new FilaIC();
		log.write("Link Queue Initialized",0);
		log.write("Initializing Radix Tree... ",1);
		RTree1 = new RadixTreeImpl<String>();
		log.write("Radix Tree Initialized",0);
		log.write("Initializing Disallow List... ",1);
		disallow = new LinkedList<String>();
		log.write("Disallow List Initialized",0);
		log.write("Initializing Allow List... ",1);
		allow = new LinkedList<String>();
		log.write("Allow List Initialized",0);
		extensiondownload = "pdf";
		log.write("File Extension Download is " + extensiondownload,2);
	}

	public static void main(String args[]){
		Crawler crawler = new Crawler();
		int i = 1;
		//String urlmain = "http://www.guru3d.com/review/mercury3d.html";
		//String urlmain = "http://www.guru3d.com";
		//String urlmain = "http://www.acm.org";
		//String urlmain = "http://portal.acm.org/citation.cfm?id=1065385.1065424&coll=Portal&dl=ACM&CFID=80042302&CFTOKEN=97533485";
		//String urlmain = "http://portal.acm.org/ft_gateway.cfm?id=1065424&type=pdf&coll=Portal&dl=ACM&CFID=80042302&CFTOKEN=97533485";
		//String urlmain = "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.45.8844&rep=rep1&type=pdf";
		//String urlmain = "http://gmail.com";
		//String urlmain = "http://scholar.google.com.br/";
		String urlmain = "http://scholar.google.com.br/scholar?q=digital+libraries+pdf";
		//String urlmain = "http://www.google.com.br/search?q=digital%20libraries%20pdf";
		//String urlmain = "http://www.uol.com.br";
		//String urlmain = "http://www.acm.org/search?SearchableText=digital+library";
		urlmain = crawler.poebarra(urlmain);
		crawler.log.write("Start URL is " + urlmain,4);
		try { 
			URL host = new URL(urlmain);
			shost = urlmain.substring(0, urlmain.indexOf("/")+2) + host.getHost();
			shost = crawler.poebarra(shost);
		} catch (Exception e){
			crawler.log.printtrace(e);
			e.printStackTrace();
		} 
		crawler.log.write("Host is " + shost,2);		
		crawler.log.write("Initializing robots.txt Fetch Module... ",4);		
		crawler.tryrobots();
		crawler.log.write("Inserting " + urlmain + " into the RTree... ",4);		
		crawler.RTree1.insert(urlmain,urlmain);
		crawler.log.write("Done",0);		
		crawler.log.write("Starting Crawler",4);
		crawler.log.write("Step " + i,2);
		crawler.log.write("Link Queue Size: " + crawler.fila1.size(),2);
		crawler.pegalinks(urlmain);
		i++;
		while (!crawler.fila1.isEmpty()){
			System.out.println("Queue Size: " + crawler.fila1.size());
			System.out.println("Step " + i);
			crawler.log.write("Step " + i,2);
			crawler.log.write("Link Queue Size: " + crawler.fila1.size(),2);
			crawler.explora();
			i++;
		}	
		crawler.RTree1.display();
		crawler.log.write("Link Queue Empty, Program Finished",4);
		crawler.log.finish();		
	}

	public void pegalinks(String urlin){

		try {
			HttpEntity entity = connect(urlin);
			if (entity!=null && !entity.getContentType().toString().contains("text/html")){
				download(urlin,"file." + extensiondownload);
			}
			System.out.println(entity.getContentType().toString());
			System.out.println(entity.getContentLength());
			System.out.println("Searching: " + urlin);
			log.write("Crawling " + urlin,2);		
			EditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();
			doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			kit.read(reader, doc, 0);
			HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
			while (it.isValid()){
				SimpleAttributeSet s = (SimpleAttributeSet)it.getAttributes();
				String href = (String)s.getAttribute(HTML.Attribute.HREF);
				String old = href;
				if ((href!=null) && (href.startsWith("../"))){
					href=pontinhos(href,urlin);
				}
				if ((href!=null) && ((!href.contains("mailto:")) && (!href.contains("javascript:")) && (!href.contains("#")))) {
					if ((href!=null) && (href.startsWith("/"))){
						href = shost + href.substring(1);
					}
					if ((href!=null) && ((!href.startsWith("http://")) && (!href.startsWith("https://")))){
						if (href.startsWith(" ")){
							System.out.println("Link Malfeito: " + href);
						} else { 
							if (!urlin.endsWith("/")){
								href = urlin.substring(0, urlin.lastIndexOf('/')) + "/" + href;
							}
							else {
								href = urlin + href;
							}
						}
					}
					if (href!=null){
						href = poebarra(href);
					}
					int k = 0;
					int j;
					int valid = 1;
					if (userobots==true){
						while (k!=disallow.size()){
							if ((href!=null && (href.startsWith(disallow.get(k))))){
								j = 0;
								valid = 0;
								while (j!=allow.size()){
									if ((href!=null) && href.contains(allow.get(j))){
										valid = 1;
									}
									j++;
								}
							}
							k++;
						}
					}
					if (valid == 0){ 
						System.out.println("Not allowed: " + href);
						log.write("Not allowed: " + href, 1);
					}
					int downloaded = 0;
					if ((href!=null) && (href.endsWith("." + extensiondownload))){
						if (RTree1.find(href)==null){
							String verifydownload = href.substring(href.lastIndexOf("/")+1);
							downloaded=1;
							if (wantdownload==true){
								log.write("Downloading " + href + " ... ",2);
								System.out.println("Downloading " + href);
								download(href,verifydownload);
								RTree1.insert(href, href);
							} else {
								log.write("File " + verifydownload + " not downloaded",2);		
								System.out.println("File " + verifydownload + " not downloaded");
							}
						}
					}

					if ((href!=null) && (downloaded==0) && ((href.startsWith("http://") || (href.startsWith("https://")) )&& (href.contains(shost)) && (valid==1))){
						if (RTree1.find(href)==null){
							if (!fila1.contains(href)){
								System.out.println( href  + " (" + old + ")");
								log.write("Inserting new link into Link Queue: " + href + " (" + old + ")... ",2);		
								fila1.offer(href);
								log.write("Done",0);		
							}
						}
					}
				}
				it.next();
			}
		} catch (Exception e) {
			log.printtrace(e);
			e.printStackTrace();
		}

		try	{
			log.write("Crawling of " + urlin + " finished",2);		
		} catch(Exception e){
			log.printtrace(e);
			e.printStackTrace();
		}
	}

	public void explora(){

		String obj=fila1.poll();
		log.write("Removed " + obj + " from the Link Queue",4);		
		if (RTree1.find(obj)==null){
			log.write("Inserting " + obj + " into RTree... ",2);		
			RTree1.insert(obj, obj);
			log.write("Done",0);		
			pegalinks(obj);
		}
	}

	public String poebarra(String obj){

		if (!obj.endsWith("/")){
			int count = 0;
			int i = 0;
			while (count<3 && i<obj.length()){
				if (obj.charAt(i)=='/'){
					count++;
				}
				i++;
			}
			if (count<3){
				obj = obj + "/"; 
			} else {
				String obj2 = obj.substring(i);
				if ((obj2.contains(".") || obj2.contains("=") || obj2.contains("?"))){
					return obj;
				} else {
					return obj + "/";
				}
			}
		}
		return obj;
	}

	public void tryrobots(){

		try {
			HttpEntity entity = connect(shost + "robots.txt");
			log.write("Done",0);		
			log.write("Adding the following entries to the Disallow and Allow List:",2);
			System.out.println("Checking robots.txt: " + shost + "robots.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
			String robotsfile = "";
			while (null != (robotsfile = br.readLine())){
				if (robotsfile.startsWith("Disallow: /")){
					if ((!robotsfile.substring(robotsfile.indexOf('/')+1).isEmpty()) && ((robotsfile.substring(robotsfile.indexOf('/')+1)!="  "))){
						String temp = shost + robotsfile.substring(robotsfile.indexOf('/')+1);
						temp=poebarra(temp);
						int k = 0;
						int valid = 1;
						while (k!=disallow.size()){
							if (temp.equals(disallow.get(k))){
								valid = 0;
							}
							k++;
						} 
						if (valid == 1){
							disallow.add(temp);
							log.write("Disallow: " + temp,1);		
							System.out.println("Restriction found: " + temp);
						}
					}
				}	
				if (robotsfile.startsWith("Allow: /")){
					if (!robotsfile.substring(robotsfile.indexOf('/')+1).isEmpty()){
						String temp = shost + robotsfile.substring(robotsfile.indexOf('/')+1);
						temp=poebarra(temp);
						int k = 0;
						int valid = 1;
						while (k!=allow.size()){
							if (temp.equals(allow.get(k))){
								valid = 0;
							}
							k++;
						} 
						if (valid == 1){
							allow.add(temp);
							log.write("Allow: " + temp,1);		
							System.out.println("Allow found: " + temp);
						}
					}
				}
			} 
		} catch (Exception e) {
			log.printtrace(e);
			e.printStackTrace();
		}
		log.write("Total of " + disallow.size() + " Entries in disallow list",2);		
		System.out.println(disallow.size() + " Entries in disallow list.");
	}

	public void download(String urlin, String filename){

		int j = 0;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try	{
			HttpEntity entity = connect(urlin);
			long size = entity.getContentLength();
			if (size != -1) {
				System.out.println("File Size: " + size);
				File fd = new File(filename);
				if (fd.exists() && fd.length()==size){
					log.write("Failed, file already exists",0);
					System.out.println(filename + " already exists, not overwriting");
				} else {

					while (fd.exists() && fd.length()!=size){
						j++;
						fd = new File(filename.substring(0, filename.lastIndexOf(".")) + " (" + j + ")" + filename.substring(filename.lastIndexOf(".")));
					}
					bis = new BufferedInputStream(entity.getContent());
					bos = new BufferedOutputStream(new FileOutputStream(fd));
					int i;
					while ((i = bis.read())!= -1){
						bos.write(i);
					}

					log.write("Done",0);
					log.write("File saved as " + filename + " Size is " + size + " Bytes",2);
				}
			} else {
				log.write("Invalid file", 0);
				System.out.println("Invalid");
			}
		} catch (Exception e){
			log.printtrace(e);
			e.printStackTrace();
		}
		finally	{
			if (bis != null)
				try	{
					bis.close();
				}	catch (IOException ioe)	{
					log.printtrace(ioe);
					ioe.printStackTrace();
				}
				if (bos != null)
					try	{
						bos.close();
					}	catch (IOException ioe)	{
						log.printtrace(ioe);
						ioe.printStackTrace();
					}
		}
	}

	public String pontinhos(String href,String urlin){
		System.out.println("                 Entrou na funcao de pontinhos " + urlin + " e " + href);
		if (href.startsWith("../")){
			System.out.println("HREF Before: " + href);
			href = href.substring(href.indexOf("/")+1);
			System.out.println("HREF After: " + href);
		}
		while (href.startsWith("../")){
			System.out.println("URLIN Before: " + urlin);
			urlin = urlin.substring(0, urlin.lastIndexOf("/"));
			urlin = urlin.substring(0, urlin.lastIndexOf("/")+1);
			System.out.println("URLIN After: " + urlin);
			System.out.println("HREF Before: " + href);
			href = href.substring(href.indexOf("/")+1);
			System.out.println("HREF After: " + href);
		}
		System.out.println("                 Saiu da funcao de pontinhos " + urlin + " e " + href);
		return urlin + href;
	}

	public HttpEntity connect(String urlin){

		DefaultHttpClient client;
		HttpGet httpget;
		HttpResponse response;
		HttpEntity entity = null;
		try {
			client = new DefaultHttpClient();
			//CredentialsProvider prov = new BasicCredentialsProvider();
			//prov.setCredentials(new AuthScope("coral.ufscar.br",3128), new UsernamePasswordCredentials("sorocaba","2006sor"));
		//	UsernamePasswordCredentials creds = 
			//client.getCredentialsProvider().setCredentials(new AuthScope("coral.ufscar.br",3128),new UsernamePasswordCredentials("sorocaba","2006sor"));
			
			//HttpHost proxy = new HttpHost(,3128);
		//	client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			httpget = new HttpGet(urlin);
			response = client.execute(httpget);
			entity = response.getEntity();
		} catch (Exception e){
			log.printtrace(e);
			e.printStackTrace();
		}
		return entity;
	}
}


