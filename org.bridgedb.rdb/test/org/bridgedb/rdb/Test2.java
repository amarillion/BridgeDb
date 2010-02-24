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
package org.bridgedb.rdb;

import buildsystem.Measure;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.rdb.DataDerby;
import org.bridgedb.rdb.SimpleGdb;
import org.bridgedb.rdb.SimpleGdbFactory;

public class Test2 extends TestCase 
{
	//TODO
	private static final String GDB_HUMAN = 
		System.getProperty ("user.home") + File.separator + 
		"PathVisio-Data/gene databases/Hs_Derby_20081119.pgdb";
	private static final String GDB_RAT = 
		System.getProperty ("user.home") + File.separator + 
		"PathVisio-Data/gene databases/Rn_Derby_20081119.pgdb";
	private static final String GDB_CE_V3 = 
		System.getProperty ("user.home") + File.separator + 
		"/PathVisio-Data/gene databases/Ce_Derby_20090720.bridge";
	
	private Measure measure;
	
	@Override public void setUp() throws ClassNotFoundException
	{
		measure = new Measure("bridgedb_timing.txt");
		Class.forName ("org.bridgedb.rdb.IDMapperRdb");
	}
	
	public void testGdbConnect() throws IDMapperException
	{
		assertTrue (new File (GDB_HUMAN).exists()); // if gdb can't be found, rest of test doesn't make sense. 

		long start, end, delta;
		start = System.currentTimeMillis();		
		IDMapper gdb = BridgeDb.connect ("idmapper-pgdb:" + GDB_HUMAN);		
		end = System.currentTimeMillis();
		delta = end - start;
		measure.add ("timing::idmapper-pgdb connect to database", "" + delta, "msec");
		
		gdb.close();
	}
	
	/**
	 * From schema v2 to v3 there was a change in how the backpage was stored.
	 * In schema v2 there was a backpage column in the datanode table
	 * In schema v3 the backpage is split in several attributes.
	 * For backwards compatibility, SimpleGdbImpl2 fakes these new attributes. 
	 * This is tested here. 
	 * @throws IDMapperException should be considered a failed test
	 */
	public void testGdbAttributes() throws IDMapperException
	{
		// test special attributes that are grabbed from backpage
		// since this is a Schema v2 database
		SimpleGdb gdb = SimpleGdbFactory.createInstance (GDB_HUMAN, new DataDerby(), 0);
		Xref ref = new Xref ("26873", DataSource.getBySystemCode("L"));
		assertTrue (gdb.getAttributes(ref, "Synonyms").contains ("5-Opase|DKFZP434H244|OPLA"));
		assertTrue (gdb.getAttributes(ref, "Description").contains ("5-oxoprolinase (EC 3.5.2.9) (5-oxo-L-prolinase) (5-OPase) (Pyroglutamase) [Source:UniProtKB/Swiss-Prot.Acc:O14841]"));
		assertTrue (gdb.getAttributes(ref, "Chromosome").contains ("8"));
		assertTrue (gdb.getAttributes(ref, "Symbol").contains ("OPLAH"));
		
		Set<String> allExpectedAttributes = new HashSet<String>();
		allExpectedAttributes.add ("26873");
	}
	
	/**
	 * Tests the capability properties of a Schema v3 database.
	 * @throws IDMapperException should be considered a failed test
	 */
	public void testGdbProperties() throws IDMapperException 
	{
		IDMapper gdb = BridgeDb.connect ("idmapper-pgdb:" + GDB_CE_V3);		
		for (String key : gdb.getCapabilities().getKeys())
		{
			System.out.println (key + " -> " + gdb.getCapabilities().getProperty(key));
		}
		assertEquals ("Caenorhabditis elegans", gdb.getCapabilities().getProperty("SPECIES"));
		assertEquals ("3", gdb.getCapabilities().getProperty("SCHEMAVERSION"));
		assertEquals ("Ensembl", gdb.getCapabilities().getProperty("DATASOURCENAME"));
		assertEquals ("20090720", gdb.getCapabilities().getProperty("BUILDDATE"));
		
		IDMapper gdb2 = BridgeDb.connect ("idmapper-pgdb:" + GDB_HUMAN);
		for (String key : gdb2.getCapabilities().getKeys())
		{
			System.out.println (key + " -> " + gdb2.getCapabilities().getProperty(key));
		}
		assertEquals ("2", gdb2.getCapabilities().getProperty("SCHEMAVERSION"));
		assertEquals ("20081119", gdb2.getCapabilities().getProperty("BUILDDATE"));
	}
	
	public void testRegisterDataSource()
	{
		DataSource.register("@@", "ZiZaZo");
		
		DataSource ds2 = DataSource.getBySystemCode ("@@");
		DataSource ds3 = DataSource.getByFullName ("ZiZaZo");
		assertEquals (ds2, ds3);
		
		// assert that you can refer to 
		// undeclared systemcodes if necessary.
		assertNotNull (DataSource.getBySystemCode ("##"));		
	}
}