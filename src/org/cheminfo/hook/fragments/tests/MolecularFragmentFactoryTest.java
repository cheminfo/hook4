package org.cheminfo.hook.fragments.tests;

import org.cheminfo.hook.fragments.MolecularFragmentFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MolecularFragmentFactoryTest {
	MolecularFragmentFactory factory;
	@Before
	public void setUp() throws Exception {
		factory = MolecularFragmentFactory.getInstance();
		factory.addMolecule("AAAA");
		factory.addMolecule("BBBB");
		factory.addMolecule("CCCC");
		factory.addMolecule("DDDD");
		factory.addFragment("A");
		factory.addFragment("B");
		factory.addFragment("C");
		factory.addFragment("D");		
		factory.addFragment("DD");		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetNumberOfMolecules() {
		Assert.assertTrue("number of molecules",factory.getNumberOfMolecule() == 4);
	}

	@Test
	public void testGetNumberOfFragments() {
		Assert.assertTrue("number of molecules",factory.getNumberOfFragment() == 5);
	}
	
	@Test
	public void testGetFragmentId() {
		Assert.assertTrue("get fragment id: DD", factory.getFragmentId("DD") == 5);
	}

	@Test
	public void testGetMoleculeId() {
		Assert.assertTrue("get molecule id: AAAA", factory.getMoleculeId("AAAA") == 1);
	}
	
}
