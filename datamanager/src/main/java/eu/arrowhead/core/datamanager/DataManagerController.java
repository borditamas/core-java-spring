package eu.arrowhead.core.datamanager;

import java.util.*; 
import java.util.Vector;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.MultiValueMap;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.dto.shared.SigML;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import eu.arrowhead.core.datamanager.service.DataManagerService;
import eu.arrowhead.core.datamanager.service.ProxyService;
import eu.arrowhead.core.datamanager.service.ProxyElement;
import eu.arrowhead.core.datamanager.service.HistorianService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.DATAMANAGER_URI)
public class DataManagerController {
	
	//=================================================================================================
	// members
	private final Logger logger = LogManager.getLogger(DataManagerController.class);
	
	@Autowired
	DataManagerService dataManagerService;
	
	@Autowired
	ProxyService proxyService;

	@Autowired
	HistorianService historianService;

	@Autowired
	DataManagerDBService dataManagerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	@ResponseBody public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Test interface to the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE)
	})
	@GetMapping(value= "/historian")
	@ResponseBody public String historianS(
			) {
		Gson gson = new Gson();

		ArrayList<String> systems = HistorianService.getSystems();
		JsonObject answer = new JsonObject();
		JsonElement systemlist = gson.toJsonTree(systems);
		answer.add("systems", systemlist);

		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get all services that s specific system has active in the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/historian/{systemName}")
	@ResponseBody public String historianSystemGet(
		@PathVariable(value="systemName", required=true) String systemName
		) {
		logger.debug("DataManager:GET:Historian/"+systemName);
		return historianSystemPut(systemName, "{\"op\": \"list\"}");
	}

	@PutMapping(value= "/historian/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String historianSystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody String requestBody
		) {
		logger.debug("DataManager:PUT:Historian/"+systemName);

		JsonParser parser= new JsonParser();
		JsonObject obj = null;
		try {
			obj = parser.parse(requestBody).getAsJsonObject();
		} catch(Exception je){
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "malformed request");
		}

		String op = obj.get("op").getAsString();
		if(op.equals("list")) {
			ArrayList<String> services = HistorianService.getServicesFromSystem(systemName);
			Gson gson = new Gson();
			JsonObject answer = new JsonObject();
			JsonElement servicelist = gson.toJsonTree(services);
			answer.add("services", servicelist);
			String jsonStr = gson.toJson(answer);

			return jsonStr;
		} else if(op.equals("create")){
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();

			/* check if service already exists */
			ArrayList<String> services = HistorianService.getServicesFromSystem(systemName);
			for (String srv: services) {
				if(srv.equals(srvName)){
					logger.info("  service:" +srv + " already exists");
					Gson gson = new Gson();
					JsonObject answer = new JsonObject();
					answer.addProperty("createResult", "Already exists");
					String jsonStr = gson.toJson(answer);
					throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, jsonStr);
				}
			}

			/* create the service */
			boolean ret = HistorianService.addServiceForSystem(systemName, srvName, srvType);
			if (ret==true){
				return "{\"x\": 0}"; //Response.status(Status.CREATED).entity("{}").type(MediaType.APPLICATION_JSON).build();
			} else {
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "{\"x\": -1, \"xs\":\"Could not create service\"}");
			}

		}
		return "{\"x\": -1, \"xs\": \"Unknown command\"}"; //make this a real object!
	}

	@GetMapping(value= "/historian/{system}/{service}")//CommonConstants.DM_HISTORIAN_URI)
	@ResponseBody public List<SenML> historianServiceGet(
		@PathVariable(value="system", required=true) String systemName,
		@PathVariable(value="service", required=true) String serviceName,
		@RequestParam MultiValueMap<String, String> params
		) {
		logger.info("DataManager:Get:Historian/"+systemName+"/"+serviceName);

		int statusCode = 0;
		
		long from=-1, to=-1;
		int count = 1;

		Vector<String> signals = new Vector<String>();
		Iterator<String> it = params.keySet().iterator();
		int sigCnt = 0;
		while(it.hasNext()){
			String par = (String)it.next();
			if (par.equals("count")) {
				count = Integer.parseInt(params.getFirst(par));
			} else if (par.equals("sig"+sigCnt)) {
				signals.add(params.getFirst(par));
				sigCnt++;
			} else if (par.equals("from")) {
				from = Long.parseLong(params.getFirst(par));
			} else if (par.equals("to")) {
				to = Long.parseLong(params.getFirst(par));
			}
		}
		logger.info("getData requested with count: " + count);

		List<SenML> ret = null;

		if(signals.size() == 0) {
			ret = HistorianService.fetchEndpoint(serviceName, from, to, count, null);
		} else {
			ret = HistorianService.fetchEndpoint(serviceName, from, to, count, signals);
		}

		return ret;
	}


	@PutMapping(value= "/historian/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)//CommonConstants.DM_HISTORIAN_URI)
	@ResponseBody public SigML historianServicePut(
	@PathVariable(value="systemName", required=true) String systemName,
	@PathVariable(value="serviceName", required=true) String serviceName,
	@RequestBody Vector<SenML> sml
	) {
		logger.debug("DataManager:Put:Historian/"+systemName+"/"+serviceName);

		boolean statusCode = HistorianService.createEndpoint(systemName, serviceName);

		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Invalid SenML");
		}

		SenML head = sml.firstElement();
		if(head.getBt() == null)
			head.setBt((double)System.currentTimeMillis() / 1000.0);

		statusCode = HistorianService.updateEndpoint(serviceName, sml);

		SigML ret = new SigML(0);
		//String jsonret = "{\"x\": 0}";
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Test interface for the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxyServices() {
		Gson gson = new Gson();

		List<String> pes = ProxyService.getAllEndpoints();
		JsonObject answer = new JsonObject();
		JsonElement systemlist = gson.toJsonTree(pes);
		answer.add("systems", systemlist);

		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's all services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxySystemGet(
			@PathVariable(value="systemName", required=true) String systemName
		) {

		List<ProxyElement> pes = ProxyService.getEndpoints(systemName);
		if (pes.size() == 0) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "System not found");
		}

		ArrayList<String> systems= new ArrayList<String>();
		for (ProxyElement pe: pes) {
			systems.add(pe.serviceName);
		}

		Gson gson = new Gson();
		JsonObject answer = new JsonObject();
		JsonElement servicelist = gson.toJsonTree(systems);
		answer.add("services", servicelist);
		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to manage a system's services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxySystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody String requestBody
		) {
		JsonParser parser= new JsonParser();
		JsonObject obj = null;
		try {
			obj = parser.parse(requestBody).getAsJsonObject();
		} catch(Exception je){
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "malformed request");
		}

		String op = obj.get("op").getAsString();
		if(op.equals("list")){
			List<ProxyElement> pes = ProxyService.getEndpoints(systemName);
			if (pes.size() == 0) {
				logger.debug("proxy GET to systemName: " + systemName + " not found");
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "System not found");
			}

			ArrayList<String> systems= new ArrayList<String>();
			for (ProxyElement pe: pes) {
				systems.add(pe.serviceName);
			}

			Gson gson = new Gson();
			JsonObject answer = new JsonObject();
			JsonElement servicelist = gson.toJsonTree(systems);
			answer.add("services", servicelist);
			String jsonStr = gson.toJson(answer);
			return jsonStr;
		} else if(op.equals("create")){
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();
			logger.info("Create Service: "+srvName+" of type: "+srvType+" for: " + systemName);

			/* check if service already exists */
			ArrayList<ProxyElement> services = ProxyService.getEndpoints(systemName);
			for (ProxyElement srv: services) {
				logger.info("PE: " + srv.serviceName);
				if(srv.serviceName.equals(srvName)){
					Gson gson = new Gson();
					JsonObject answer = new JsonObject();
					answer.addProperty("createResult", "Already exists");
					String jsonStr = gson.toJson(answer);
					throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "createResult: Already exists");
				}
			}

			/* create the service */
			boolean ret = ProxyService.addEndpoint(new ProxyElement(systemName, srvName));
			if (ret==true){
				throw new ResponseStatusException(org.springframework.http.HttpStatus.CREATED, "createResult: Created");
			} else { 
				throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "createResult: Already exists");
			}
		} else if(op.equals("delete")){ //NOT SUPPORTED YET
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();
			logger.info("Delete Service: "+srvName+" of type: "+srvType+" for: " + systemName);
		}

		return "";
		}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's last service data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}/{serviceName}")//CommonConstants.DM_PROXY_URI)
	@ResponseBody public String proxyServiceGet(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName
			) {

			int statusCode = 0;
			ProxyElement pe = ProxyService.getEndpoint(serviceName);
			if (pe == null) {
				logger.info("proxy GET to serviceName: " + serviceName + " not found");
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found");
			}

			Iterator i = pe.msg.iterator();
			String senml = "";

			return pe.msg.toString();
			}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to update a system's last service data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}/{serviceName}")//CommonConstants.DM_PROXY_URI)
	@ResponseBody public String proxyPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName,
			@RequestBody Vector<SenML> sml
			) {
		ProxyElement pe = ProxyService.getEndpoint(serviceName);
		if (pe == null) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found");
		}

		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Invalid SenML");
		}

		boolean statusCode = ProxyService.updateEndpoint(systemName, serviceName, sml);

		int ret = 0;
		if (statusCode == false)
			ret = 1;
		String jsonret = "{\"rc\": "+ret+"}";
		return jsonret;
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public boolean validateSenML(final Vector<SenML> sml){

	  /* check that bn, bt and bu are included only once, and in the first object */
	  Iterator entry = sml.iterator();
	  int bnc=0, btc=0, buc=0;
	  while (entry.hasNext()) {
	    SenML o = (SenML)entry.next();
	    if (o.getBn() != null)
	      bnc++;
	    if (o.getBt() != null)
	      btc++;
	    if (o.getBu() != null)
	      buc++;
	  }


	  /* bu can only exist once. bt can only exist one, bu can exist 0 or 1 times */
	  if (bnc != 1 || btc != 1 || buc > 1)
		  return false;

	  /* bn must exist in [0] */
	  SenML o = (SenML)sml.get(0);
	  if (o.getBn() == null)
		  return false;

	  /* bt must exist in [0] */
	  if (o.getBt() == null)
		  return false;

	  /* bu must exist in [0], if it exists */
	  if (o.getBu() == null && buc == 1)
		  return false;

	  /* check that v, bv, sv, etc are included only once per object */
	  entry = sml.iterator();
	  while (entry.hasNext()) {
	    o = (SenML)entry.next();

	    int value_count = 0;
	    if (o.getV() != null)
	      value_count++;
	    if (o.getVs() != null)
	      value_count++;
	    if (o.getVd() != null)
	      value_count++;
	    if (o.getVb() != null)
	      value_count++;

	    if(value_count > 1 && o.getS() == null)
	      return false;
	  } 

	  return true;
	}	
}

