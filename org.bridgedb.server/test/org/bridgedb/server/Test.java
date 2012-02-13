//BridgeDb,
//An abstraction layer for identifer mapping services, both local and online.
//Copyright 2006-2009 BridgeDb developers
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//
package org.bridgedb.server;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.bridgedb.AttributeMapper;
import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperCapabilities;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class Test {
	private IDMapper mapper;
	private Server server;
	private boolean configExists;
	
	@Before
	protected void setUp() throws Exception {
	    
	   
	    File f= new File(IDMapperService.CONF_GDBS);
	    
	    configExists = f.exists();
	    
	    if (configExists)
	    {
	    
    	    if (server == null)
            {
                server = new Server();
                server.run(8183, null, false);
            }
    		if(mapper == null) {
    		    
    			// Create a client
    			Class.forName("org.bridgedb.webservice.bridgerest.BridgeRest");
    			mapper = BridgeDb.connect("idmapper-bridgerest:http://localhost:8183/Human");
    		}
	    }
	    else
	    {
	        System.err.println("Skipping server tests. No " + IDMapperService.CONF_GDBS + " found.");
	    }
		
	}
	
	

	@After
    protected void tearDown() throws Exception
    {
	    if (server != null)
	    {
	        server.stop();
	    }
    }



    public IDMapper getLocalService() {
		return mapper;
	}
	
	@org.junit.Test
	public void testLocalMapID() throws IDMapperException, ClassNotFoundException {
		
	    if (configExists)
	    {
    	    IDMapper mapper = getLocalService();
    		
    		Xref insr = new Xref ("3643", BioDataSource.ENTREZ_GENE);
    		Xref affy = new Xref ("33162_at", BioDataSource.AFFY);
    		Set<Xref> result = mapper.mapID(insr);
    		Assert.assertTrue (result.contains(affy));
    		
    		Assert.assertTrue(mapper.xrefExists(insr));
	    }
	}
	
	@org.junit.Test
	public void testLocalCapabilities() throws IDMapperException, ClassNotFoundException {
		
	    if (configExists)
	    {
    	    IDMapper mapper = getLocalService();
    		
    		IDMapperCapabilities cap = mapper.getCapabilities();
    		
    		Set<DataSource> supported = cap.getSupportedSrcDataSources();
    		Assert.assertTrue (supported.contains(DataSource.getBySystemCode("L")));
    
    		String val = cap.getProperty("SCHEMAVERSION");
    		Assert.assertNotNull(val);
    		
    		Set<DataSource> srcDs = cap.getSupportedSrcDataSources();
    		Assert.assertTrue(srcDs.size() > 0);
    		
    		Assert.assertTrue(cap.isFreeSearchSupported());
    		
    		Assert.assertTrue(cap.isMappingSupported(BioDataSource.UNIPROT, BioDataSource.ENTREZ_GENE));
    		
    		Assert.assertFalse(cap.isMappingSupported(
    				DataSource.getBySystemCode("??"), DataSource.getBySystemCode("!!")));
	    }
	}
	
	@org.junit.Test
	public void testLocalSearch() throws IDMapperException, ClassNotFoundException {
		
	    if (configExists)
        {
    	    IDMapper mapper = getLocalService();
    		
    		Set<Xref> result = mapper.freeSearch("1234", 100);
    		System.out.println(result);
    		Assert.assertTrue(result.size() > 0);
        }
	}
	
	@org.junit.Test
	public void testLocalAttributes() throws ClassNotFoundException, IDMapperException {
		
	    if (configExists)
        {
    	    AttributeMapper mapper = (AttributeMapper)getLocalService();
    		
    		Xref insr = new Xref("3643", BioDataSource.ENTREZ_GENE);
    		Map<String, Set<String>> attrMap = mapper.getAttributes(insr);
    		Assert.assertNotNull(attrMap.get("Symbol"));
    		Assert.assertTrue(attrMap.get("Symbol").size() == 2);
    		
    		Set<String> attrValues = mapper.getAttributes(insr, "Symbol");
    		Assert.assertTrue(attrValues.size() == 2);
    		
    		Map<Xref, String> xrefMap = mapper.freeAttributeSearch("INSR", "Symbol", 1);
    		Assert.assertTrue(xrefMap.size() == 1);
    
    		xrefMap = mapper.freeAttributeSearch("INSR", "Symbol", 100);
    		Assert.assertTrue(xrefMap.containsKey(insr));
    		Assert.assertTrue(xrefMap.size() > 1);
    		
    		Set<String> attrs = mapper.getAttributeSet();
    		Assert.assertTrue(attrs.size() > 0);
        }
	}



}
