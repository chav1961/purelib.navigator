package chav1961.purelib.navigator.admin;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelibnavigator.admin.Balancer.ResourceRepo;
import chav1961.purelibnavigator.admin.Balancer.ResourceRepoType;
import chav1961.purelibnavigator.admin.Balancer.ResourceType;

public class BalancerTest {
	@Test
	public void basicTest() {
		final ResourceRepo	rr = new ResourceRepo(ResourceRepoType.REPO1);
		
		Assert.assertEquals(ResourceRepoType.REPO1,rr.getType());
		for (ResourceType item : ResourceType.values()) {
			Assert.assertEquals(0, rr.available(item));
		}
		
		for (ResourceType item : ResourceType.values()) {
			rr.add(item, 100);
			Assert.assertEquals(100, rr.available(item));
			rr.add(item, 100);
			Assert.assertEquals(200, rr.available(item));
		}
		
		try{new ResourceRepo(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{rr.add(null, 1);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{rr.add(ResourceType.TYPE1, -1);
			Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void reserveTest() {
		final ResourceRepo	rr = new ResourceRepo(ResourceRepoType.REPO1);
		
		rr.add(ResourceType.TYPE1, 10);
		rr.add(ResourceType.TYPE2, 20);
		rr.add(ResourceType.TYPE3, 30);
		
		Assert.assertEquals(30, rr.reserve(ResourceType.TYPE3, 30));	// present 30 TYPE3
		Assert.assertEquals(30, rr.reserve(ResourceType.TYPE3, 30));	// missing 30 TYPE 3 + present 20 TYPE 2 (produce) + present 10 TYPE 1 (produce)
		Assert.assertEquals(0, rr.reserve(ResourceType.TYPE3, 30));		// missing everything
		
		rr.undoReserve(ResourceType.TYPE3, 30);
		Assert.assertEquals(10, rr.available(ResourceType.TYPE1));
		Assert.assertEquals(20, rr.available(ResourceType.TYPE2));
		Assert.assertEquals(0, rr.available(ResourceType.TYPE3));
		
		rr.undoReserve(ResourceType.TYPE3, 30);
		Assert.assertEquals(10, rr.available(ResourceType.TYPE1));
		Assert.assertEquals(20, rr.available(ResourceType.TYPE2));
		Assert.assertEquals(30, rr.available(ResourceType.TYPE3));
	}
}
