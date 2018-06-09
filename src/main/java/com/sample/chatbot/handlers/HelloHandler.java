package com.sample.chatbot.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ai.chatbot.framework.request.LexRequest;
import com.ai.chatbot.framework.response.LexResponse;
import com.ai.chatbot.framework.response.Message;
import com.ai.chatbot.framework.response.action.CloseDialogAction;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.chatbot.framework.handlers.AbstractLexRequestHandler;


public class HelloHandler extends AbstractLexRequestHandler implements RequestStreamHandler{
    
	private Logger logger = LogManager.getLogger(getClass().getName());
	
	/**
	 * 
	 */
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        byte[] requestBytes = IOUtils.toByteArray(inputStream);
        if (logger.isDebugEnabled()) {
            logger.debug("Request Json:\n {}", new String(requestBytes));
        }
        byte[] responseBytes = null;
        try {
            LexRequest lexRequest = LexRequest.fromJson(requestBytes);
            
            if(lexRequest != null)
            {
            	Map<String,String> requestAttributes = null;
            	Map<String,String> sessionAttributes = null;        	
            	
             	// Get session      
            	if(lexRequest.getSessionAttributes() != null)
            		sessionAttributes = new HashMap<>(lexRequest.getSessionAttributes());
            	else
            		sessionAttributes = new HashMap<>();
            	
            	if(lexRequest.getRequestAttributes() != null)
            	{
            		requestAttributes = lexRequest.getRequestAttributes();
            	   	String pageAccessToken = requestAttributes.get("x-amz-lex:facebook-page-id");
            	   	String pageScopeId = requestAttributes.get("x-amz-lex:user-id");
            	   	if(getObjectFromSession(sessionAttributes, "UserID", new TypeReference<String>() {}) == null)
            	   	{
            	   		saveObjectIntoSession(sessionAttributes, "UserID", pageScopeId, new TypeReference<String>() {});
            	   		setUserPublicProfile(sessionAttributes, pageScopeId, pageAccessToken);
            	   	}
            	}
            	
            	// Pull First Name from Session
            	String firstName = getObjectFromSession(sessionAttributes, "FirstName", new TypeReference<String>() {});
            	
            	// Create Response
            	LexResponse lexResponse = new LexResponse(new CloseDialogAction("Fulfilled", new Message("PlainText","Hello "+ firstName + ", Hope your are doing great")),sessionAttributes);
                responseBytes = lexResponse.toJson();
                if (logger.isDebugEnabled()) {
                    logger.debug("Response Json:\n {}", new String(responseBytes));
                }
            }
            else
                throw new RuntimeException("Request is null: ");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        outputStream.write(responseBytes);
    }  
	
	/**
	 * 
	 * @param sessionAttributes
	 * @param id
	 * @param token
	 */
	protected void setUserPublicProfile(Map<String,String> sessionAttributes, String id, String token) 
    {
    	logger.info("id:" + id + "toke:" + token);
    	String url = "https://graph.facebook.com/v3.0/?ids="+id+"&access_token="+token;
		
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);

			HttpResponse response = client.execute(request);
			
			if(response.getStatusLine().getStatusCode() == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String inputLine;
				StringBuffer result = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					result.append(inputLine);
				}
				in.close();
				
				if (logger.isDebugEnabled()) {
                    logger.debug("Result String :\n {}", result.toString());
                }
				
				JsonNode rootNode = new ObjectMapper().readTree(new StringReader(result.toString()));
				JsonNode innerNode = rootNode.get(id); // Get the only element in the root node
			    
				// get an element in that node
			    String first_name = innerNode.get("first_name").asText();
			    String last_name = innerNode.get("last_name").asText();
				logger.info("first_name:" + first_name + "last_name:" + last_name);
				saveObjectIntoSession(sessionAttributes, "FirstName", first_name, new TypeReference<String>() {});
				saveObjectIntoSession(sessionAttributes, "LastName", last_name, new TypeReference<String>() {});
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("Exception : " + e.getMessage());
			throw new RuntimeException(e);
			//return new LexResponse(new CloseDialogAction("Failed", new Message("PlainText",getMessage("error.message"))));
		} 
   	}
}