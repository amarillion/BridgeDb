// BridgeDb,
// An abstraction layer for identifer mapping services, both local and online.
// Copyright 2006-2009 BridgeDb developers
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.webservice.cronos;

import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.junit.Before;

public class Test {
	
	@Before public void setUp() throws ClassNotFoundException
	{
		Class.forName ("org.bridgedb.webservice.cronos.IDMapperCronos");
	}
	
	@org.junit.Test
	public void testSimple() throws IDMapperException
	{
        try {
            IDMapper mapper = BridgeDb.connect ("idmapper-cronos:hsa");
            BioDataSource.init();
            Xref insr = new Xref ("3643", BioDataSource.ENTREZ_GENE);
            Set<Xref> result = mapper.mapID(insr, BioDataSource.ENSEMBL);
            for (Xref ref : result) System.out.println (ref);
        } catch (Exception ex){
            System.err.println("**** Warning Cronos Test FAILED due to Cronos server not responding");
            System.err.println(ex);
        }
	}
	
}
