package cmpe.wings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.search.StatusCode;


@SuppressWarnings("serial")
public class Cmpe202wingsServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String str = req.getPathInfo();
		String kind = null;
		if (str.contains("five")) {
			kind = "five";
		} else if (str.contains("eight")) {
			kind = "eight";
		} else if (str.contains("ten")) {
			kind = "ten";
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(kind);
		PreparedQuery pq = datastore.prepare(q);
		ArrayList<Map<String, Object>> list = new ArrayList<>();
		for (Entity result : pq.asIterable()) {
			Map<String, Object> map = result.getProperties();
			list.add(map);
		}
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			public int compare(Map<String, Object> map1, Map<String, Object> map2) {
				int time1 = Integer.valueOf((String) map1.get("time"));
				int time2 = Integer.valueOf((String)map2.get("time"));
				return time1 - time2;
			}
		});
		PrintWriter output = resp.getWriter();
		JSONArray jsonArray = new JSONArray();
		int count = 0;
		for (Map<String, Object> element : list) {
			if (count >= 5) {
				break;
			}
			JSONObject jsonValue = new JSONObject();
			for (String key : element.keySet()) {
				jsonValue.put(key, element.get(key));
			}
			jsonArray.add(jsonValue);
			//StringWriter out = new StringWriter();
			//jsonValue.writeJSONString(out);
			
			//String jsonText = out.toString();
			//resp.setContentType("application/json");
			//output.print(jsonText);
			count++;
		}
		StringWriter out = new StringWriter();
		jsonArray.writeJSONString(out);
		String jsonText = out.toString();
		resp.setContentType("application/json");
		output.print(jsonText);
		output.flush();		
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String str = req.getPathInfo();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
		String line;
		StringBuffer sb = new StringBuffer();
		while((line = reader.readLine()) != null) {
			sb.append(line);
		}
		System.out.println(sb.toString());
		JSONParser parser = new JSONParser();
		Object obj = null;
		
		try {
			obj = parser.parse(sb.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject map = (JSONObject)obj;
		String kind = null;
		if (((String)map.get("number")).equals("five")) {
			kind = "five";
		} else if (((String)map.get("number")).equals("ten")) {
			kind = "ten";
		} else if (((String)map.get("number")).equals("eight")) {
			kind = "eight";
		}
		Entity newEntity = new Entity(kind);
		for(Object key :map.keySet()) {
			if (((String)key).equals("number")) {
				continue;
			}
			newEntity.setProperty(key.toString(), map.get(key));
		}
		datastore.put(newEntity);
	}
}